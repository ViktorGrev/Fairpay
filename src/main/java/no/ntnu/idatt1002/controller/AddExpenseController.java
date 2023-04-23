package no.ntnu.idatt1002.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import no.ntnu.idatt1002.data.Group;
import no.ntnu.idatt1002.data.User;
import no.ntnu.idatt1002.data.economy.Expense;
import no.ntnu.idatt1002.data.economy.ExpenseType;
import no.ntnu.idatt1002.scene.SceneSwitcher;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ResourceBundle;

public final class AddExpenseController extends MenuController implements Initializable {

    @FXML private ComboBox<String> typeField;
    @FXML private Label nameText;
    @FXML private TextField nameField;
    @FXML private TextField amountField;
    @FXML private DatePicker dateField;

    @FXML
    private void addExpenseClick() {
        ExpenseType type = ExpenseType.fromName(typeField.getValue());
        String name = nameField.getText().isBlank() ? null : nameField.getText();
        BigDecimal amount = BigDecimal.valueOf(Long.parseLong(amountField.getText()));
        LocalDate localDate = dateField.getValue();
        Date date = java.sql.Date.valueOf(localDate);
        Group group = groupDAO.find(Group.CURRENT);
        Expense expense = expenseDAO.create(User.CURRENT, type, name, amount, date, group.getMembers().size());
        groupDAO.addExpense(group.getId(), expense.getExpenseId());
        group.addExpense(expense.getExpenseId());
        SceneSwitcher.setView("expense");
    }

    @FXML
    private void goBackClick() {
        SceneSwitcher.setView("expense");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        typeField.setOnAction(e -> {
            if(typeField.getValue().equals("Other")) {
                nameText.setVisible(true);
                nameField.setVisible(true);
            } else {
                nameText.setVisible(false);
                nameField.setVisible(false);
            }
        });

        for(ExpenseType type : ExpenseType.values()) {
            typeField.getItems().add(type.getCategoryName());
        }

        typeField.setValue(ExpenseType.FOOD.getCategoryName());

        dateField.setValue(LocalDate.now());
    }
}
