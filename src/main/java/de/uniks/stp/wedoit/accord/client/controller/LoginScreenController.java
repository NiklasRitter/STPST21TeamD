package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONObject;

public class LoginScreenController {

    private LocalUser model;
    private Editor editor;
    private Parent view;
    private Button btnLogin;
    private TextField tfUserName;
    private TextField pwUserPw;
    private Label errorLabel;

    public LoginScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.model = model;
        this.editor = editor;
    }

    public void init() {
        //Load all view references
        tfUserName = (TextField) view.lookup("#tfUserName");
        pwUserPw = (TextField) view.lookup("#pwUserPw");
        errorLabel = (Label) view.lookup("#lblError");

        btnLogin = (Button) view.lookup("#btnLogin");

        btnLogin.setOnAction(this::loginButtonAction);

    }

    private void loginButtonAction(ActionEvent actionEvent) {
        RestClient.login(tfUserName.getText(), pwUserPw.getText(), (response) -> {
            if (response.getStatus() != 200) {
                tfUserName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");
                pwUserPw.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");

                Platform.runLater(() -> errorLabel.setText("Username or password is wrong."));
            } else {
                JSONObject loginAnswer = response.getBody().getObject().getJSONObject("data");
                String userKey = loginAnswer.getString("userKey");

                this.model.setUserKey(userKey);
                StageManager.showMainScreen();
            }
        });
    }

    public void stop() {
        btnLogin.setOnAction(null);

        tfUserName = null;
        pwUserPw = null;
        btnLogin = null;
    }
}
