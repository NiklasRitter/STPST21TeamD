package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class CreateServerScreenController implements Controller{

    private final RestClient restClient;
    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private TextField tfServerName;
    private Button btnCreateServer;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view The view this Controller belongs to
     * @param model The model this Controller belongs to
     * @param editor The editor of the Application
     * @param restClient The RestClient of the Application
     */
    public CreateServerScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     *
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
     *
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

        if (tfServerName.getText().length() < 2) {
            tfServerName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");

            Platform.runLater(() -> errorLabel.setText("Name has to be at least 2 symbols long"));
        } else {
            restClient.createServer(tfServerName.getText(), localUser.getUserKey(), (response) -> {
                if (response.getBody().getObject().getString("status").equals("success")) {
                    JSONObject createServerAnswer = response.getBody().getObject().getJSONObject("data");
                    String serverId = createServerAnswer.getString("id");
                    String serverName = createServerAnswer.getString("name");

                    Server server = editor.haveServer(localUser, serverId, serverName);
                    stop();
                    Platform.runLater(() -> StageManager.showServerScreen(server, restClient));
                } else {
                    tfServerName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");

                    Platform.runLater(() -> errorLabel.setText("Something went wrong while creating the server"));
                }
            });
        }
    }
}
