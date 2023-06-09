package no.ntnu.idatt1002.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import no.ntnu.idatt1002.model.Group;
import no.ntnu.idatt1002.model.Invite;
import no.ntnu.idatt1002.model.User;
import no.ntnu.idatt1002.client.view.Page;
import no.ntnu.idatt1002.util.TableEditor;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller class for the group invites page.
 */
public final class GroupInvitesController extends Controller implements Initializable {

  @FXML private TableView<Invite> inviteTable;

  /**
   * Send the user back to the join or create group menu.
   */
  @FXML
  private void backButtonClick() {
    viewPage(Page.JOIN_CREATE);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    TableEditor<Invite> inviteTableEditor = new TableEditor<>(inviteTable)
            .setPlaceholder("No invites")
            .addColumn("Group", invite -> getGroup(invite.getGroupId()).getName())
            .addColumn(this::createAcceptButton)
            .addColumn(this::createDenyButton);

    List<Invite> invites = groupDAO.getInvitesByUser(User.CURRENT);
    if(!invites.isEmpty()) {
      List<Long> groupIds = invites.stream().map(Invite::getGroupId).toList();
      getGroups(groupIds);
      inviteTableEditor.addRows(invites);
    }
  }

  /**
   * Creates an accept invitation button for an invitation.
   * @param   invite the invite
   * @return  a button
   */
  private Button createAcceptButton(Invite invite) {
    Button button = new Button("Accept");
    button.getStyleClass().add("button2");
    button.setOnMouseClicked(event -> {
      groupDAO.removeInvite(invite.getGroupId(), invite.getTargetId());
      groupDAO.addMember(invite.getGroupId(), invite.getTargetId());
      Group g = groupDAO.find(invite.getGroupId());
      Group.setCurrent(g.getId());
      viewPage(Page.HOMEPAGE);
    });
    return button;
  }

  /**
   * Creates a deny invitation button for an invitation.
   * @param   invite the invite
   * @return  a button
   */
  private Button createDenyButton(Invite invite) {
    Button button = new Button("Deny");
    button.getStyleClass().add("button2");
    button.setOnMouseClicked(event -> {
      groupDAO.removeInvite(invite.getGroupId(), invite.getTargetId());
      inviteTable.getItems().removeIf(i ->
              i.getGroupId() == invite.getGroupId() && i.getTargetId() == invite.getTargetId());
    });
    return button;
  }
}
