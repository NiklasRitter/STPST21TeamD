package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Objects;

public class LoginScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final LocalUser model;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnOptions;
    private TextField tfUserName;
    private TextField pwUserPw;
    private Label errorLabel;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public LoginScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.model = model;
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
        //Load all view references
        this.tfUserName = (TextField) view.lookup("#tfUserName");
        this.pwUserPw = (TextField) view.lookup("#pwUserPw");
        this.errorLabel = (Label) view.lookup("#lblError");

        this.btnLogin = (Button) view.lookup("#btnLogin");
        this.btnRegister = (Button) view.lookup("#btnRegister");
        this.btnOptions = (Button) view.lookup("#btnOptions");


        // Add necessary action listeners
        this.btnLogin.setOnAction(this::loginButtonAction);
        this.btnRegister.setOnAction(this::btnRegisterOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        // Remove all action listeners
        btnLogin.setOnAction(null);
        btnRegister.setOnAction(null);
        btnOptions.setOnAction(null);
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
        String name = this.tfUserName.getText();
        String password = this.pwUserPw.getText();
        if (tfUserName == null || name.isEmpty() || pwUserPw == null || password.isEmpty()) {

            Objects.requireNonNull(tfUserName).getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            errorLabel.setText("Username or password is missing");
        } else {
            editor.getRestManager().loginUser(name, password, this);
        }
    }

    public void handleLogin(boolean success) {
        if (!success) {
            tfUserName.getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Username or password is wrong."));
        } else {
            Platform.runLater(() -> this.editor.getStageManager().showMainScreen());
        }
    }


    /**
     * register user to server and login, redirect to MainScreen
     *
     * @param actionEvent occurs when clicking the register button
     */
    private void btnRegisterOnClicked(ActionEvent actionEvent) {
        String name = this.tfUserName.getText();
        String password = this.pwUserPw.getText();

        if (name != null && !name.isEmpty() && password != null && !password.isEmpty()) {
            editor.getRestManager().registerUser(name, password, this);
        } else {
            //reset name and password fields
            tfUserName.getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Please type in username and password."));
        }
    }

    public void handleRegister(boolean success) {
        if (!success) {
            //reset name and password fields
            this.tfUserName.setText("");
            this.pwUserPw.setText("");
            tfUserName.getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            Platform.runLater(() -> errorLabel.setText("Username already taken."));
        } else {
            //login the user
            login();
        }
    }


    /**
     * open Optionsmenu
     *
     * @param actionEvent occurs when clicking the options button
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        this.editor.getStageManager().showOptionsScreen();
    }
}