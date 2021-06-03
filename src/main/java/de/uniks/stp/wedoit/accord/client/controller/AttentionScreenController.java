package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
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

    private Label lblObjectToDelete;
    private Button btnDiscard;
    private Button btnDelete;
    private Label lblError;

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
        btnDiscard = (Button) this.view.lookup("#btnDiscard");
        btnDelete = (Button) this.view.lookup("#btnDelete");

        this.lblError.setVisible(false);
        loadCorrectLabelText(objectToDelete);

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
        Stage stage = (Stage) view.getScene().getWindow();
        Platform.runLater(stage::close);
    }

    private void loadCorrectLabelText(Object objectToDelete) {
        String[] strings = objectToDelete.getClass().toString().split("\\.");
        String objectName = strings[strings.length - 1];
        this.lblObjectToDelete.setText(objectName);
    }

    private void deleteOnClick(ActionEvent actionEvent) {
        this.editor.getNetworkController().deleteObject(this.localUser, this.objectToDelete, this);
    }

    private void discardOnClick(ActionEvent actionEvent) {
        if (objectToDelete.getClass().equals(Server.class)) {
            StageManager.showEditServerScreen((Server) objectToDelete);
        }
    }

    public void handleDeleteServer(boolean status) {
        if (status) {
            Platform.runLater(StageManager::showMainScreen);
        } else {
            Platform.runLater(() -> {
                lblError.setText("Error. Delete Server was not successful!");
                lblError.setVisible(true);
            });
        }
    }
}
