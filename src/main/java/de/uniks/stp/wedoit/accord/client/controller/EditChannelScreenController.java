package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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
        System.out.println(this.channel);
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

    }

    public void handleEditChannel(Server server) {

    }

    /**
     * After pressing "Delete", you get redirected to the deleteHandler
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void deleteChannelButtonOnClick(ActionEvent actionEvent) {

    }
}
