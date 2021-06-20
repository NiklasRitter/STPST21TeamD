package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class JoinServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private TextField tfInvitationLink;
    private Button btnJoinServer;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public JoinServerScreenController(Parent view, LocalUser model, Editor editor) {
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
        this.btnJoinServer = (Button) view.lookup("#btnJoinServer");
        this.tfInvitationLink = (TextField) view.lookup("#tfInvitationLink");
        this.errorLabel = (Label) view.lookup("#lblError");

        // Add action listeners
        this.btnJoinServer.setOnAction(this::joinServerButtonOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnJoinServer.setOnAction(null);
    }


    /**
     * After pressing "Join Server", the server will be created with the name in the text field and you get
     * redirected to the Screen for the Server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void joinServerButtonOnClick(ActionEvent actionEvent) {

        if (tfInvitationLink.getText().contains(REST_SERVER_URL + API_PREFIX + SERVER_PATH) && tfInvitationLink.getText().contains(INVITES) && !tfInvitationLink.getText().contains(" ")) {
            errorLabel.setText("Try to join server...");
            editor.getRestManager().joinServer(localUser, tfInvitationLink.getText(), this);
        } else {
            errorLabel.setText("Please insert a valid invitation link");
        }
    }

    /**
     * handles a response of a invitation request
     *
     * @param server          the new server which is entered
     * @param responseMessage responseMessage of the response
     */
    public void handleInvitation(Server server, String responseMessage) {
        if (server != null) {
            Platform.runLater(() -> this.editor.getStageManager().showServerScreen(server));
        } else {
            if (responseMessage.equals("MainScreen")) {
                Platform.runLater(() -> this.editor.getStageManager().showMainScreen());
            }

            Platform.runLater(() -> errorLabel.setText(responseMessage));
        }
    }
}
