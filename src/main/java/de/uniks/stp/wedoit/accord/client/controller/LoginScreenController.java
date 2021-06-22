package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.Objects;

public class LoginScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final AccordClient model;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnOptions;
    private CheckBox btnRememberMe;
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
    public LoginScreenController(Parent view, AccordClient model, Editor editor) {
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
        this.btnRememberMe = (CheckBox) view.lookup("#btnRememberMe");


        // Add necessary action listeners
        this.btnLogin.setOnAction(this::loginButtonAction);
        this.btnRegister.setOnAction(this::btnRegisterOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.btnRememberMe.setOnAction(this::btnRememberMeOnClick);

        this.initTooltips();
    }

    private void initTooltips() {
        Tooltip optionsButton = new Tooltip();
        optionsButton.setText(LanguageResolver.getString("OPTIONS"));
        this.btnOptions.setTooltip(optionsButton);

        Tooltip loginButton = new Tooltip();
        loginButton.setText("Login");
        this.btnLogin.setTooltip(loginButton);

        Tooltip registerButton = new Tooltip();
        registerButton.setText("Register");
        this.btnRegister.setTooltip(registerButton);
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
        btnRememberMe.setOnAction(null);
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
        try {
            String name = this.tfUserName.getText();
            String password = this.pwUserPw.getText();

            if (tfUserName == null || name.isEmpty() || pwUserPw == null || password.isEmpty()) {
                Objects.requireNonNull(tfUserName).getStyleClass().add("error");
                Objects.requireNonNull(pwUserPw).getStyleClass().add("error");
                errorLabel.setText("Username or password is missing");
            } else {
                editor.getRestManager().loginUser(name, password, this);
            }
        } catch (Exception e) {
            errorLabel.setText("An error has been encountered while logging in. Please try again.");
            System.err.println("Error while logging user in!");
            e.printStackTrace();
        }
    }

    /**
     * handles a login and redirect to the correct screen
     *
     * @param success success of the login as boolean
     */
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
        try {
            String name = this.tfUserName.getText();
            String password = this.pwUserPw.getText();

            if (tfUserName == null || name.isEmpty() || pwUserPw == null || password.isEmpty()) {
                //reset name and password fields
                Objects.requireNonNull(tfUserName).getStyleClass().add("error");
                Objects.requireNonNull(pwUserPw).getStyleClass().add("error");
                errorLabel.setText("Please type in username and password.");
            } else {
                editor.getRestManager().registerUser(name, password, this);
            }
        } catch (Exception e) {
            errorLabel.setText("An error has been encountered while registering. Please try again.");
            System.err.println("Error while registering user!");
            e.printStackTrace();
        }
    }

    /**
     * handles a register and sets label correct
     *
     * @param success success of the register as boolean
     */
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
     * Change the remember me preference to the value of the CheckBox.
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnRememberMeOnClick(ActionEvent actionEvent) {
        model.getOptions().setRememberMe(btnRememberMe.isSelected());
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