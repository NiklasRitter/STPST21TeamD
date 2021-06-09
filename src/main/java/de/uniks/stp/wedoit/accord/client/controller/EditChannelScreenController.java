package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.TEXT;

public class EditChannelScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Channel channel;
    private TextField tfChannelName;
    private Button btnCreateChannel;
    private CheckBox checkBoxPrivileged;
    private Button btnDeleteChannel;
    private Label errorLabel;

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
        this.btnCreateChannel = (Button) view.lookup("#btnEditChannel");
        this.btnDeleteChannel = (Button) view.lookup("#btnDeleteChannel");
        this.tfChannelName = (TextField) view.lookup("#tfChannelName");
        this.checkBoxPrivileged = (CheckBox) view.lookup("#checkBoxPrivileged");
        this.errorLabel = (Label) view.lookup("#lblError");

        // Add action listeners
        this.btnCreateChannel.setOnAction(this::editChannelButtonOnClick);
        this.btnDeleteChannel.setOnAction(this::deleteChannelButtonOnClick);

        if (channel.isPrivileged()) {
            this.checkBoxPrivileged.setSelected(true);
        }
        tfChannelName.setText(channel.getName());
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnCreateChannel.setOnAction(null);
        btnDeleteChannel.setOnAction(null);
    }


    /**
     * After pressing "Save", the channel changes will be saved and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editChannelButtonOnClick(ActionEvent actionEvent) {
        if (tfChannelName.getText().length() < 1 || tfChannelName.getText() == null) {
            tfChannelName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            if (!checkBoxPrivileged.isSelected()) {
                editor.getNetworkController().updateChannel(editor.getCurrentServer(), channel.getCategory(), channel, tfChannelName.getText(), checkBoxPrivileged.isSelected(), null, this);
            } else {
                List<String> userList = new LinkedList<>();
                userList.add(this.localUser.getId());
                editor.getNetworkController().updateChannel(editor.getCurrentServer(), channel.getCategory(), channel, tfChannelName.getText(), checkBoxPrivileged.isSelected(), userList, this);
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
}
