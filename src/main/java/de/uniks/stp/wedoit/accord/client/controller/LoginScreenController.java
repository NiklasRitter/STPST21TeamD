package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.WindowEvent;

import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.MAIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.OPTIONS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUPSTAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class LoginScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final AccordClient model;

    private Button btnLogin;
    private Button btnRegister;
    private Button btnOptions;
    private Button btnGuestLogin;
    private CheckBox btnRememberMe;
    private TextField tfUserName;
    private TextField pwUserPw;
    private Label errorLabel, lblEnterUserName, lblEnterPw, lblRememberMe, lblUserValid, lblGuestPassword;

    private String guestUserPassword;
    private String errorLabelText = "";

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
        this.lblEnterUserName = (Label) view.lookup("#lblEnterUserName");
        this.lblEnterPw = (Label) view.lookup("#lblEnterPw");
        this.lblRememberMe = (Label) view.lookup("#lblRememberMe");
        this.lblUserValid = (Label) view.lookup("#lblUserValid");
        this.lblGuestPassword = (Label) view.lookup("#lblGuestPassword");

        this.btnLogin = (Button) view.lookup("#btnLogin");
        this.btnRegister = (Button) view.lookup("#btnRegister");
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnRememberMe = (CheckBox) view.lookup("#btnRememberMe");
        this.btnGuestLogin = (Button) view.lookup("#btnGuestLogin");

        this.view.requestFocus();
        this.setComponentsText();

        // Add necessary action listeners
        this.btnLogin.setOnAction(this::loginButtonAction);
        this.btnRegister.setOnAction(this::btnRegisterOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.btnRememberMe.setOnAction(this::btnRememberMeOnClick);
        this.btnGuestLogin.setOnAction(this::btnGuestLoginOnClick);

        this.initTooltips();

        this.refreshStage();
    }

    /**
     * Sets texts of all GUI components like buttons, labels etc. in the selected language.
     */
    private void setComponentsText() {
        this.tfUserName.setPromptText(LanguageResolver.getString("YOUR_USERNAME"));
        this.lblEnterUserName.setText(LanguageResolver.getString("ENTER_YOUR_USERNAME"));
        this.lblEnterPw.setText(LanguageResolver.getString("ENTER_YOUR_PASSWORD"));
        this.lblRememberMe.setText(LanguageResolver.getString("REMEMBER_ME"));
        this.btnLogin.setText(LanguageResolver.getString("LOGIN"));
        this.btnRegister.setText(LanguageResolver.getString("REGISTER"));
        this.btnGuestLogin.setText(LanguageResolver.getString("GUEST_LOGIN"));
        if (guestUserPassword != null) {
            this.setGuestUserDataLabel();
        }
        this.errorLabel.setText(LanguageResolver.getString(errorLabelText));
    }

    /**
     * Sets texts of labels for guest user in the selected language with correct data.
     */
    private void setGuestUserDataLabel() {
        Platform.runLater(() -> {
            this.lblUserValid.setText(LanguageResolver.getString("USER_VALID_FOR_24H"));
            this.lblGuestPassword.setText(LanguageResolver.getString("GUEST_USER_PASSWORD") + " " + guestUserPassword);
        });
    }

    private void initTooltips() {
        Tooltip optionsButton = new Tooltip();
        optionsButton.setText(LanguageResolver.getString("OPTIONS"));
        this.btnOptions.setTooltip(optionsButton);

        Tooltip loginButton = new Tooltip();
        loginButton.setText(LanguageResolver.getString("LOGIN"));
        this.btnLogin.setTooltip(loginButton);

        Tooltip registerButton = new Tooltip();
        registerButton.setText(LanguageResolver.getString("REGISTER"));
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
                errorLabelText = "USERNAME_PASSWORD_MISSING";
                refreshErrLabelText(errorLabelText);
            } else {
                editor.getRestManager().loginUser(name, password, this);
            }
        } catch (Exception e) {
            errorLabelText = "ERROR_HAS_BEEN_ENCOUNTERED";
            refreshErrLabelText(errorLabelText);
            System.err.println(LanguageResolver.getString("ERROR_WHILE_LOGIN_USER"));
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
            Platform.runLater(() -> {
                errorLabelText = "USERNAME_PASSWORD_WRONG";
                refreshErrLabelText(errorLabelText);
            });
        } else {
            Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
        }
    }

    /**
     * handles a guest login
     *
     * @param success success of the login as boolean
     */
    public void handleGuestLogin(String userName, String password, boolean success) {
        if (!success) {
            tfUserName.getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            Platform.runLater(() -> {
                errorLabelText = "USERNAME_PASSWORD_WRONG";
                refreshErrLabelText(errorLabelText);
            });
        } else {
            guestUserPassword = password;
            this.tfUserName.setText(userName);
            this.pwUserPw.setText(password);
            setGuestUserDataLabel();

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
                errorLabelText = "PLEASE_TYPE_USERNAME_PASSWORD";
                refreshErrLabelText(errorLabelText);
            } else {
                editor.getRestManager().registerUser(name, password, this);
            }
        } catch (Exception e) {
            errorLabelText = "ERROR_WHILE_REGISTERING";
            refreshErrLabelText(errorLabelText);
            System.err.println(LanguageResolver.getString("ERROR_WHILE_REGISTER_USER"));
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
            Platform.runLater(() -> {
                errorLabelText = "USERNAME_ALREADY_TAKEN";
                refreshErrLabelText(errorLabelText);
            });
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
        this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("OPTIONS"), "OptionsScreen", OPTIONS_SCREEN_CONTROLLER, false, null, null);
    }

    /**
     * Sends a request to the server and gets name and password for a guest user
     *
     * @param actionEvent occurs when clicking the Guest login button
     */
    private void btnGuestLoginOnClick(ActionEvent actionEvent) {
        editor.getRestManager().guestLogin(this);
        errorLabelText = "";
        refreshErrLabelText(errorLabelText);
        tfUserName.getStyleClass().remove("error");
        pwUserPw.getStyleClass().remove("error");
    }

    /**
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage() {
        this.editor.getStageManager().getPopupStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                setComponentsText();
                initTooltips();
                editor.getStageManager().getStage().setTitle(LanguageResolver.getString("LOGIN"));
            }
        });
    }

    /**
     * Refreshes the errorLabel after closing the option screen,
     * so that the component text are displayed in the correct language.
     *
     * @param errorLabelText is the current text of the error label
     */
    private void refreshErrLabelText(String errorLabelText) {
        errorLabel.setText(LanguageResolver.getString(errorLabelText));
    }
}