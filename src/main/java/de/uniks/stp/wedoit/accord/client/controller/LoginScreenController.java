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

import javax.json.JsonStructure;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class LoginScreenController {

    private LocalUser model;
    private Editor editor;
    private Parent view;

    private Button btnLogin;
    private Button btnRegister;
    private Button btnOptions;
    private TextField tfUserName;
    private TextField pwUserPw;
    private Label errorLabel;

    private RestClient restClient;

    public LoginScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.model = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    public void init() {
        //Load all view references
        this.tfUserName = (TextField) view.lookup("#tfUserName");
        this.pwUserPw = (TextField) view.lookup("#pwUserPw");
        this.errorLabel = (Label) view.lookup("#lblError");

        this.btnLogin = (Button) view.lookup("#btnLogin");
        this.btnRegister = (Button) view.lookup("#btnRegister");
        this.btnOptions = (Button) view.lookup("#btnOptions");


        this.btnLogin.setOnAction(this::loginButtonAction);
        this.btnRegister.setOnAction(this::btnRegisterOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
    }

    public void stop() {
        btnLogin.setOnAction(null);
        btnRegister.setOnAction(null);
        btnOptions.setOnAction(null);

        tfUserName = null;
        pwUserPw = null;
        btnLogin = null;
        btnRegister = null;

        errorLabel = null;

        btnOptions = null;
    }

    /**
     * login user to server and redirect to MainScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void loginButtonAction(ActionEvent actionEvent) {
        login();
    }

    public void login() {
        if (tfUserName == null || tfUserName.getText().isEmpty() || pwUserPw == null || pwUserPw.getText().isEmpty()) {

            tfUserName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");
            pwUserPw.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            errorLabel.setText("Username or password is missing");
        } else {
            restClient.login(tfUserName.getText(), pwUserPw.getText(), (response) -> {

                if (!response.getBody().getObject().getString("status").equals("success")) {

                    tfUserName.setStyle("-fx-border-color: #ff0000 ; -fx-border-width: 2px ;");
                    pwUserPw.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
                    Platform.runLater(() -> errorLabel.setText("Username or password is wrong."));

                } else {

                    JSONObject loginAnswer = response.getBody().getObject().getJSONObject(COM_DATA);
                    String userKey = loginAnswer.getString(COM_USER_KEY);
                    editor.haveLocalUser(tfUserName.getText(), userKey);
                    editor.getNetworkController().start();
                    Platform.runLater(() -> StageManager.showMainScreen(restClient));
                }
            });
        }
    }

    /**
     * register user to server and login, redirect to MainScreen
     *
     * @param actionEvent
     */
    private void btnRegisterOnClicked(ActionEvent actionEvent) {
        String name = this.tfUserName.getText();
        String password = this.pwUserPw.getText();

        if (name != null && !name.isEmpty() && password != null && !password.isEmpty()) {
            restClient.register(name, password, registerResponse -> {
                // if user successful registered
                if (registerResponse.getBody().getObject().getString("status").equals("success")) {

                    //login the user
                    login();
                } else {
                    //reset name and password fields
                    this.tfUserName.setText("");
                    this.pwUserPw.setText("");
                    tfUserName.setStyle("-fx-border-color: #ff0000; -fx-border-width: 2px;");
                    pwUserPw.setStyle("-fx-border-color: #ff0000; -fx-border-width: 2px;");
                    Platform.runLater(() -> errorLabel.setText("Username already taken."));
                }
            });
        } else {
            tfUserName.setStyle("-fx-border-color: #ff0000; -fx-border-width: 2px;");
            pwUserPw.setStyle("-fx-border-color: #ff0000; -fx-border-width: 2px;");
            Platform.runLater(() -> errorLabel.setText("Please type in username and password."));
        }
    }

    /**
     * open Optionsmenu
     *
     * @param actionEvent
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }
}