package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.MemberListSubViewController;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;

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

public class CreateChannelScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Category category;
    private TextField tfChannelName;
    private Button btnCreateChannel;
    private CheckBox checkBoxPrivileged;
    private Label errorLabel, lblMembers;
    private HBox hBoxLblMembers;
    private VBox vBoxMain, vBoxMemberNameAndCheckBox;
    private ArrayList<MemberListSubViewController> memberListSubViewControllers;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public CreateChannelScreenController(Parent view, LocalUser model, Editor editor, Category category) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.category = category;
        this.memberListSubViewControllers = new ArrayList<>();
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
        this.btnCreateChannel = (Button) view.lookup("#btnCreateChannel");
        this.tfChannelName = (TextField) view.lookup("#tfChannelName");
        this.checkBoxPrivileged = (CheckBox) view.lookup("#checkBoxPrivileged");
        this.errorLabel = (Label) view.lookup("#lblError");

        this.vBoxMain = (VBox) view.lookup("#vBoxMain");
        this.vBoxMemberNameAndCheckBox = (VBox) view.lookup("#vBoxMemberNameAndCheckBox");
        this.hBoxLblMembers = (HBox) view.lookup("#hBoxLblMembers");
        this.lblMembers = (Label) view.lookup("#lblMembers");

        // Add action listeners
        this.btnCreateChannel.setOnAction(this::createChannelButtonOnClick);
        this.checkBoxPrivileged.setOnAction(this::checkBoxPrivilegedOnClick);
    }

    private void checkBoxPrivilegedOnClick(ActionEvent actionEvent) {
        if (this.checkBoxPrivileged.isSelected()) {
            initSubViewMemberList();
        } else {
            this.vBoxMemberNameAndCheckBox.getChildren().clear();
        }
    }

    private void initSubViewMemberList() {
        this.vBoxMemberNameAndCheckBox.getChildren().clear();
        for (User user : this.editor.getCurrentServer().getMembers()) {
            try {
                Parent view = FXMLLoader.load(StageManager.class.getResource("view/subview/MemberListSubView.fxml"));
                MemberListSubViewController memberListSubViewController = new MemberListSubViewController(user, view, this.editor, this.category);
                memberListSubViewController.init();

                this.vBoxMemberNameAndCheckBox.getChildren().add(view);
                this.memberListSubViewControllers.add(memberListSubViewController);

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
        btnCreateChannel.setOnAction(null);
        checkBoxPrivileged.setOnAction(null);
        for (MemberListSubViewController controller : this.memberListSubViewControllers) {
            controller.stop();
        }
    }


    /**
     * After pressing "Create", the channel will be created with the name in the text field and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createChannelButtonOnClick(ActionEvent actionEvent) {
        if (tfChannelName.getText().length() < 1 || tfChannelName.getText() == null) {
            tfChannelName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            if (!checkBoxPrivileged.isSelected()) {
                editor.getNetworkController().createChannel(editor.getCurrentServer(), category, tfChannelName.getText(), TEXT, checkBoxPrivileged.isSelected(), null, this);
            } else {
                List<String> userList = new LinkedList<>();
                userList.add(this.localUser.getId());
                editor.getNetworkController().createChannel(editor.getCurrentServer(), category, tfChannelName.getText(), TEXT, checkBoxPrivileged.isSelected(), userList, this);
            }
        }
    }

    public void handleCreateChannel(Channel channel) {
        if (channel != null) {
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            tfChannelName.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Something went wrong while creating the channel"));
        }
    }
}
