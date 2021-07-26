package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CreateServerScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private TextField tfServerName;
    private Button btnCreateServer;
    private Label lblError, lblEnterServerName;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param editor The editor of the Application
     */
    public CreateServerScreenController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
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
        this.btnCreateServer = (Button) view.lookup("#btnCreateServer");
        this.tfServerName = (TextField) view.lookup("#tfServerName");
        this.lblError = (Label) view.lookup("#lblError");
        this.lblEnterServerName = (Label) view.lookup("#lblEnterServerName");

        this.view.requestFocus();

        // Add action listeners
        this.btnCreateServer.setOnAction(this::createServerButtonOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnCreateServer.setOnAction(null);
    }


    /**
     * After pressing "Create Server", the server will be created with the name in the text field and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void createServerButtonOnClick(ActionEvent actionEvent) {

        if (tfServerName.getText().length() < 1 || tfServerName.getText() == null) {
            tfServerName.getStyleClass().add(LanguageResolver.getString("ERROR"));

            Platform.runLater(() -> lblError.setText(LanguageResolver.getString("NAME_HAST_BE_1_SYMBOL")));
        } else {
            editor.getRestManager().createServer(tfServerName.getText(), this);
        }
    }

    /**
     * handles the creation of a channel.
     *
     * @param server server which is created if creation was successful
     */
    public void handleCreateServer(Server server) {
        if (server != null) {
            stop();
            Platform.runLater(() -> {
                this.editor.getStageManager().initView(ControllerEnum.SERVER_SCREEN, server, null);
                this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
            });
        } else {
            tfServerName.getStyleClass().add(LanguageResolver.getString("ERROR"));

            Platform.runLater(() -> lblError.setText(LanguageResolver.getString("SOMETHING_WORNG_WHILE_CREATING_SERVER")));
        }
    }
}
