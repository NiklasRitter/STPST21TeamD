package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.MemberListSubViewController;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;

public class CreateChannelScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Category category;
    private TextField tfChannelName;
    private Button btnCreateChannel;
    private CheckBox checkBoxPrivileged;
    private Label errorLabel, lblMembers;
    private VBox vBoxMemberNameAndCheckBox;
    private final ArrayList<MemberListSubViewController> memberListSubViewControllers;
    private final List<String> userList = new LinkedList<>();
    private RadioButton radioBtnText;
    private RadioButton radioBtnVoice;

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
        this.btnCreateChannel = (Button) view.lookup("#btnSave");
        Button btnDeleteChannel = (Button) view.lookup("#btnDeleteChannel");
        this.tfChannelName = (TextField) view.lookup("#tfChannelName");
        this.checkBoxPrivileged = (CheckBox) view.lookup("#checkBoxPrivileged");
        this.errorLabel = (Label) view.lookup("#lblError");

        this.radioBtnText = (RadioButton) view.lookup("#radioBtnText");
        this.radioBtnVoice = (RadioButton) view.lookup("#radioBtnVoice");

        this.vBoxMemberNameAndCheckBox = (VBox) view.lookup("#vBoxMemberNameAndCheckBox");
        this.lblMembers = (Label) view.lookup("#lblMembers");

        checkIfIsPrivileged();
        initTextVoiceOption();

        this.btnCreateChannel.setText("Create");
        btnDeleteChannel.setVisible(false);

        // Add action listeners
        this.btnCreateChannel.setOnAction(this::createChannelButtonOnClick);
        this.checkBoxPrivileged.setOnAction(this::checkBoxPrivilegedOnClick);
    }


    private void initTextVoiceOption() {
        ToggleGroup toggleGroup = new ToggleGroup();
        this.radioBtnText.setToggleGroup(toggleGroup);
        this.radioBtnVoice.setToggleGroup(toggleGroup);

        radioBtnText.setSelected(true);
        //TODO only implement this if also in editChannelScreen
        /*
        String channelType = this.editor.getChannelType();

        if (channelType.equals(TEXT)) {
            radioBtnText.setSelected(true);
        } else {
            radioBtnVoice.setSelected(true);
        }
        */
    }

    /**
     * loads member and set size of the screen correct.
     *
     * @param actionEvent actionEvent such a when a button is fired
     */
    private void checkBoxPrivilegedOnClick(ActionEvent actionEvent) {
        checkIfIsPrivileged();
        //Adjusts the size of the stage to its dynamically added content
        this.editor.getStageManager().getPopupStage().sizeToScene();
    }

    /**
     * shows members in sub view members list
     */
    private void checkIfIsPrivileged() {
        if (this.checkBoxPrivileged.isSelected()) {
            initSubViewMemberList();
            lblMembers.setVisible(true);
        } else {
            this.vBoxMemberNameAndCheckBox.getChildren().clear();
            lblMembers.setVisible(false);
        }
    }

    /**
     * If channel is privileged, then the lister of all users is dynamically added to CreateChannelScreen.
     * then calls MemberListSubViewController:
     * You can then add users (except the local user) to the privileged channel.
     */
    private void initSubViewMemberList() {
        this.vBoxMemberNameAndCheckBox.getChildren().clear();
        for (User user : this.editor.getCurrentServer().getMembers()) {
            try {
                if (!user.getId().equals(this.localUser.getId())) {
                    Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/subview/MemberListSubView.fxml")));
                    MemberListSubViewController memberListSubViewController = new MemberListSubViewController(user, view, this, false);
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
            String channelType = this.radioBtnText.isSelected() ? TEXT: AUDIO;

            if (!checkBoxPrivileged.isSelected()) {
                editor.getRestManager().createChannel(editor.getCurrentServer(), category, tfChannelName.getText(),
                        channelType, checkBoxPrivileged.isSelected(), null, this);
            } else if (checkBoxPrivileged.isSelected()) {
                if (userList.size() <= 0) {
                    userList.add(this.localUser.getId());
                }
                editor.getRestManager().createChannel(editor.getCurrentServer(), category, tfChannelName.getText(),
                        channelType, checkBoxPrivileged.isSelected(), userList, this);
            }
        }
    }

    /**
     * handles the creation of a channel.
     *
     * @param channel channel which is created if creation was successful
     */
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

    /**
     * adds a user to the user list.
     *
     * @param user user which should be added
     */
    public void addToUserList(User user) {
        userList.add(user.getId());
    }

    /**
     * removes a user from the user list.
     *
     * @param user user which should be removed
     */
    public void removeFromUserList(User user) {
        userList.remove(user.getId());
    }
}
