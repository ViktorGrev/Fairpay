package no.ntnu.idatt1002.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.ntnu.idatt1002.client.view.Page;
import no.ntnu.idatt1002.model.Group;
import no.ntnu.idatt1002.model.User;

/**
 * Controller class for the login page.
 */
public final class LoginController extends Controller {

  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private Text loginFeedback;

  /**
   * Attempts to log in a user with the specified username
   * and password. If the login is successful, and the user is not
   * in a group, they will be sent to the page for creating a group.
   * Otherwise they will be sent to the homepage.
   */
  @FXML
  private void loginButtonClick() {
    try {
      String username = usernameField.getText();
      String password = passwordField.getText();
      User user = userDAO.authenticate(username, password);
      User.setCurrent(user.getId());
      Group group = groupDAO.findByUser(user.getId());
      if (group == null) {
        viewPage(Page.JOIN_CREATE);
      } else {
        Group.setCurrent(group.getId());
        viewPage(Page.HOMEPAGE);
      }
    } catch (Exception e) {
      loginFeedback.setText(e.getLocalizedMessage());
    }
  }

  /**
   * Sends the user to the signup page.
   */
  @FXML
  private void signupButtonClick() {
    viewPage(Page.SIGNUP);
  }
}
