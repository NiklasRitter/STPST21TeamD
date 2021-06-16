package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.MemberListSubViewController;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class EditChannelScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Channel channel;
    private TextField tfChannelName;
    private Button btnSave;
    private CheckBox checkBoxPrivileged;
    private Button btnDeleteChannel;
    private Label errorLabel, lblMembers;
    private HBox hBoxLblMembers;
    private VBox vBoxMain, vBoxMemberNameAndCheckBox;
    private ArrayList<MemberListSubViewController> memberListSubViewControllers;
    private List<String> userList;
    private Boolean isPrivilegedUser = false;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public EditChannelScreenController(Parent view, LocalUser model, Editor editor, Channel channel) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.channel = channel;
        this.memberListSubViewControllers = new ArrayList<>();
        this.userList = new LinkedList<>();
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {
        // Load all view references
        this.btnSave = (Button) view.lookup("#btnSave");
        this.btnDeleteChannel = (Button) view.lookup("#btnDeleteChannel");
        this.tfChannelName = (TextField) view.lookup("#tfChannelName");
        this.checkBoxPrivileged = (CheckBox) view.lookup("#checkBoxPrivileged");
        this.errorLabel = (Label) view.lookup("#lblError");

        this.vBoxMemberNameAndCheckBox = (VBox) view.lookup("#vBoxMemberNameAndCheckBox");
        this.vBoxMain = (VBox) view.lookup("#vBoxMain");
        this.hBoxLblMembers = (HBox) view.lookup("#hBoxLblMembers");
        this.lblMembers = (Label) view.lookup("#lblMembers");

        if (channel.isPrivileged()) {
            this.checkBoxPrivileged.setSelected(true);
            for (User user : channel.getMembers()) {
                userList.add(user.getId());
            }
        }

        checkIfIsPrivileged();

        tfChannelName.setText(channel.getName());

        // Add action listeners
        this.btnSave.setOnAction(this::btnSaveOnClick);
        this.btnDeleteChannel.setOnAction(this::deleteChannelButtonOnClick);
        this.checkBoxPrivileged.setOnAction(this::checkBoxPrivilegedOnClick);

    }


    private void checkBoxPrivilegedOnClick(ActionEvent actionEvent) {
        checkIfIsPrivileged();
        //Adjusts the size of the stage to its dynamically added content
        StageManager.getPopupStage().sizeToScene();
    }

    private void checkIfIsPrivileged() {
        if (this.checkBoxPrivileged.isSelected()) {
            channel.setPrivileged(true);
            initSubViewMemberList();
            lblMembers.setVisible(true);
        } else {
            this.vBoxMemberNameAndCheckBox.getChildren().clear();
            channel.setPrivileged(false);
            lblMembers.setVisible(false);
        }
    }

    private void initSubViewMemberList() {
        this.vBoxMemberNameAndCheckBox.getChildren().clear();
        for (User user : this.editor.getCurrentServer().getMembers()) {
            try {
                if (!user.getId().equals(this.localUser.getId())) {
                    if (this.channel.isPrivileged() && userList.contains(user.getId())) {
                        isPrivilegedUser = true;
                    } else {
                        isPrivilegedUser = false;
                    }
                    Parent view = FXMLLoader.load(StageManager.class.getResource("view/subview/MemberListSubView.fxml"));
                    MemberListSubViewController memberListSubViewController = new MemberListSubViewController(user, view, this, isPrivilegedUser);
                    memberListSubViewController.init();

                    this.vBoxMemberNameAndCheckBox.getChildren().add(view);
                    this.memberListSubViewControllers.add(memberListSubViewController);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnSave.setOnAction(null);
        btnDeleteChannel.setOnAction(null);
    }


    /**
     * After pressing "Save", the channel changes will be saved and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnSaveOnClick(ActionEvent actionEvent) {
        if (tfChannelName.getText().length() < 1 || tfChannelName.getText() == null) {
            tfChannelName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            if (!checkBoxPrivileged.isSelected()) {
                editor.getRestManager().updateChannel(editor.getCurrentServer(), channel.getCategory(), channel, tfChannelName.getText(), checkBoxPrivileged.isSelected(), null, this);
            } else if (checkBoxPrivileged.isSelected()) {
                if (userList.size() <= 0) {
                    userList.add(this.localUser.getId());
                    editor.getRestManager().updateChannel(editor.getCurrentServer(), channel.getCategory(), channel, tfChannelName.getText(), checkBoxPrivileged.isSelected(), userList, this);
                } else {
                    editor.getRestManager().updateChannel(editor.getCurrentServer(), channel.getCategory(), channel, tfChannelName.getText(), checkBoxPrivileged.isSelected(), userList, this);
                }
            }
        }

    }

    public void handleEditChannel(Channel channel) {
        if (channel != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            tfChannelName.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Something went wrong while updating the channel"));
        }
    }

    /**
     * After pressing "Delete", you get redirected to the deleteHandler
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteChannelButtonOnClick(ActionEvent actionEvent) {
        StageManager.showAttentionScreen(channel);
    }

    public void addToUserList(User user) {
        if (!userList.contains(user.getId())) {
            userList.add(user.getId());
        }
    }

    public void removeFromUserList(User user) {
        if (userList.contains(user.getId())) {
            userList.remove(user.getId());
        }
    }
}
