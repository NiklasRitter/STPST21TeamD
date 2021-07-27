package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AttentionScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Object objectToDelete;

    private Button btnDiscard;
    private Button btnDelete;
    private Label lblError, lblAreYouSure;

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
        lblError = (Label) this.view.lookup("#lblError");
        lblAreYouSure = (Label) this.view.lookup("#lblAreYouSure");
        btnDiscard = (Button) this.view.lookup("#btnDiscard");
        btnDelete = (Button) this.view.lookup("#btnDelete");

        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).sizeToScene();
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).centerOnScreen();
        loadCorrectLabelText(objectToDelete);

        this.lblError.setVisible(false);

        addActionListener();
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
        if (objectToDelete instanceof Server) {
            this.lblAreYouSure.setText(LanguageResolver.getString("SURE_TO_DELETE_SERVER"));
        } else if (objectToDelete instanceof Channel) {
            this.lblAreYouSure.setText(LanguageResolver.getString("SURE_TO_DELETE_CHANNEL"));
        } else if (objectToDelete instanceof Category) {
            this.lblAreYouSure.setText(LanguageResolver.getString("SURE_TO_DELETE_CATEGORY"));
        } else if (objectToDelete instanceof Message) {
            this.lblAreYouSure.setText(LanguageResolver.getString("SURE_TO_DELETE_MESSAGE"));
        }
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
            this.editor.getStageManager().initView(ControllerEnum.EDIT_SERVER_SCREEN, objectToDelete, null);
        } else if (objectToDelete.getClass().equals(Channel.class)) {
            this.editor.getStageManager().initView(ControllerEnum.EDIT_CHANNEL_SCREEN, objectToDelete, null);
        } else if (objectToDelete.getClass().equals(Category.class)) {
            this.editor.getStageManager().initView(ControllerEnum.EDIT_CATEGORY_SCREEN, objectToDelete, null);
        } else if (objectToDelete.getClass().equals(Message.class)) {
            this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
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
            Platform.runLater(() -> {
                this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN,null,null);
                Platform.runLater(() -> editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close());
            });
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

    /**
     * handles the deletion of a message
     *
     * @param status status which says whether a deletion was successful
     */
    public void handleDeleteMessage(boolean status) {
        if (status) {
            Message message = (Message) objectToDelete;
            message.setChannel(null);
            Stage stage = (Stage) view.getScene().getWindow();
            Platform.runLater(stage::close);
            stop();
        } else {
            Platform.runLater(() -> {
                lblError.setText("Error. Delete Message was not successful!");
                lblError.setVisible(true);
            });
        }
    }
}
