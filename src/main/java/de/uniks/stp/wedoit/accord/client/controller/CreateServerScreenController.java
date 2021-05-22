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
import javafx.scene.control.TextField;

public class CreateServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private TextField tfServerName;
    private Button btnCreateServer;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view       The view this Controller belongs to
     * @param model      The model this Controller belongs to
     * @param editor     The editor of the Application
     */
    public CreateServerScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
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
        this.errorLabel = (Label) view.lookup("#lblError");

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
            tfServerName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 1 symbols long"));
        } else {
            editor.getNetworkController().createServer(tfServerName.getText(), this);
        }
    }

    public void handleCreateServer(Server server) {
        if (server != null) {
            stop();
            Platform.runLater(() -> StageManager.showServerScreen(server));
        } else {
            tfServerName.getStyleClass().add("error");

            Platform.runLater(() -> errorLabel.setText("Something went wrong while creating the server"));
        }
    }
}
