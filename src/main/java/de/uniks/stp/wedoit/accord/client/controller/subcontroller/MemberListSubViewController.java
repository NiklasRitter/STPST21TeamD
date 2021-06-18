package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.CreateChannelScreenController;
import de.uniks.stp.wedoit.accord.client.controller.EditChannelScreenController;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;


public class MemberListSubViewController implements Controller {

    private final User user;
    private final Parent view;
    private final Controller controller;
    private VBox vBoxMemberName, vBoxCheckBox;
    private CheckBox checkBoxPrivilegedMember;
    private Boolean isPrivilegedUser;

    public MemberListSubViewController(User user, Parent view, Controller controller, Boolean isPrivilegedUser) {
        this.user = user;
        this.view = view;
        this.controller = controller;
        this.isPrivilegedUser = isPrivilegedUser;
    }

    @Override
    public void init() {
        this.vBoxMemberName = (VBox) this.view.lookup("#vBoxMemberName");
        this.vBoxCheckBox = (VBox) this.view.lookup("#vBoxCheckBox");

        this.checkBoxPrivilegedMember = new CheckBox();
        this.checkBoxPrivilegedMember.setSelected(isPrivilegedUser);
        this.checkBoxPrivilegedMember.setOnAction(this::checkBoxOnClick);
        this.vBoxMemberName.getChildren().add(new Label(user.getName()));
        this.vBoxCheckBox.getChildren().add(checkBoxPrivilegedMember);
    }

    /**
     * Checks which controller calls the class.
     * Adds the selected user to the user list of the corresponding controller.
     * If you deselect a user, he will be removed from the list.
     */
    private void checkBoxOnClick(ActionEvent actionEvent) {
        if (controller.getClass().equals(CreateChannelScreenController.class)) {
            CreateChannelScreenController createChannelScreenController = (CreateChannelScreenController) controller;
            if (checkBoxPrivilegedMember.isSelected()) {
                createChannelScreenController.addToUserList(user);
            } else if (!checkBoxPrivilegedMember.isSelected()) {
                createChannelScreenController.removeFromUserList(user);
            }
        } else if (controller.getClass().equals(EditChannelScreenController.class)) {
            EditChannelScreenController editChannelScreenController = (EditChannelScreenController) controller;
            if (checkBoxPrivilegedMember.isSelected()) {
                editChannelScreenController.addToUserList(user);
            } else if (!checkBoxPrivilegedMember.isSelected()) {
                editChannelScreenController.removeFromUserList(user);
            }
        }
    }

    @Override
    public void stop() {
        this.checkBoxPrivilegedMember.setOnAction(null);
    }
}
