package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class AttentionScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Object objectToDelete;

    private Label lblObjectToDelete;
    private Button btnDiscard;
    private Button btnDelete;
    private Label lblError,lblAreYouSure, lblAttention;

    /**
     * Create a new Controller
     *
     * @param view           The view this Controller belongs to
     * @param model          The model this Controller belongs to
     * @param editor         The editor of the Application
     * @param objectToDelete The Object(Server, Channel or Category) that has to be deleted
     */
    public AttentionScreenController(Parent view, LocalUser model, Editor editor, Object objectToDelete) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.objectToDelete = objectToDelete;
    }

    @Override
    public void init() {
        lblObjectToDelete = (Label) this.view.lookup("#lblObjectToDelete");
        lblError = (Label) this.view.lookup("#lblError");
        lblAttention = (Label) this.view.lookup("#lblAttention");
        lblAreYouSure = (Label) this.view.lookup("#lblAreYouSure");
        btnDiscard = (Button) this.view.lookup("#btnDiscard");
        btnDelete = (Button) this.view.lookup("#btnDelete");

        this.setComponentsText();

        this.lblError.setVisible(false);
        loadCorrectLabelText(objectToDelete);

        addActionListener();
    }

    private void setComponentsText() {
        this.lblAttention.setText(LanguageResolver.getString("ATTENTION"));
        this.lblAreYouSure.setText(LanguageResolver.getString("ATTENTION_DELETE"));
        this.btnDelete.setText(LanguageResolver.getString("DELETE"));
        this.btnDiscard.setText(LanguageResolver.getString("DISCARD"));
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    private void addActionListener() {
        this.btnDiscard.setOnAction(this::discardOnClick);
        this.btnDelete.setOnAction(this::deleteOnClick);
    }

    @Override
    public void stop() {
        this.btnDiscard.setOnAction(null);
        this.btnDelete.setOnAction(null);
    }

    /**
     * shows the correct text of to be deleted object
     *
     * @param objectToDelete object which should deleted
     */
    private void loadCorrectLabelText(Object objectToDelete) {
        String[] strings = objectToDelete.getClass().toString().split("\\.");
        String objectName = strings[strings.length - 1];
        this.lblObjectToDelete.setText(objectName);
    }


    /**
     * deletes objectToDelete
     *
     * @param actionEvent actionEvent such a when a button is fired
     */
    private void deleteOnClick(ActionEvent actionEvent) {
        this.editor.getRestManager().deleteObject(this.localUser, this.objectToDelete, this);
    }

    /**
     * shows error message if delete server was not successful
     */
    private void showError() {
        Platform.runLater(() -> {
            lblError.setText(LanguageResolver.getString("ERROR_DELETE_SERVER"));
            lblError.setVisible(true);
        });
    }

    /**
     * cancels a deletion and show the correct screen
     *
     * @param actionEvent actionEvent such a when a button is fired
     */
    private void discardOnClick(ActionEvent actionEvent) {
        if (objectToDelete.getClass().equals(Server.class)) {
            this.editor.getStageManager().initView(POPUPSTAGE, "Edit Server", "EditServerScreen", EDIT_SERVER_SCREEN_CONTROLLER, false, objectToDelete, null);
        } else if (objectToDelete.getClass().equals(Channel.class)) {
            this.editor.getStageManager().initView(POPUPSTAGE, "Edit Channel", "EditChannelScreen", EDIT_CHANNEL_SCREEN_CONTROLLER, true, objectToDelete, null);
        } else if (objectToDelete.getClass().equals(Category.class)) {
            this.editor.getStageManager().initView(POPUPSTAGE, "Edit Category", "EditCategoryScreen", EDIT_CATEGORY_SCREEN_CONTROLLER, false, objectToDelete, null);
        }
    }

    /**
     * handles the deletion of a server
     *
     * @param status status which says whether a deletion was successful
     */
    public void handleDeleteServer(boolean status) {
        if (status) {
            localUser.withoutServers((Server) objectToDelete);
            Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, "Main", "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
            stop();
        } else {
            showError();
        }
    }

    /**
     * handles the deletion of a channel
     *
     * @param status status which says whether a deletion was successful
     */
    public void handleDeleteChannel(boolean status) {
        if (status) {
            Channel channel = (Channel) objectToDelete;
            channel.setCategory(null);
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            showError();
        }
    }

    /**
     * handles the deletion of a category
     *
     * @param status status which says whether a deletion was successful
     */
    public void handleDeleteCategory(boolean status) {
        if (status) {
            Category category = (Category) objectToDelete;
            category.setServer(null);
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            showError();
        }
    }
}
