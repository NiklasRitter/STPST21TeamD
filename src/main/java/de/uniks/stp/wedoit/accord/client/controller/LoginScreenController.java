package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;

import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.MAIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.OPTIONS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.POPUP_STAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class LoginScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final AccordClient model;

    private Button btnLogin;
//    private Button btnRegister;
    private Button btnOptions;
    private Button btnGuestLogin;
    private CheckBox btnRememberMe;
    private TextField tfUserName;
    private TextField pwUserPw;
    private Label lblError, lblEnterUserName, lblEnterPw, lblRememberMe, lblUserValid, lblGuestPassword;

    private String guestUserPassword;
    private String errorLabelText = "";
    private Button btnSwitchRegister;
//    private Button btnGuestRegister;
    private Pane pnlSignUp;
    private Pane pnlSignIn;
    private Label lblSignIn;
    private TextField pwConfirmPW;
    private boolean isLogin;
    //    private TextField tfUserNameRegister;
//    private TextField pwUserPwRegister;
//    private Button btnSwitchLogin;
//    private Label lblErrorRegister;
//    private Label lblRememberMeRegister;
//    private CheckBox btnRememberMeRegister;
//    private Button btnOptionsRegister;
//    private Label lblUserValidRegister;
//    private Label lblGuestPasswordRegister;

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
        /*this.pnlSignUp = (Pane) view.lookup("#pnlSignUp");
        this.pnlSignIn = (Pane) view.lookup("#pnlSignIn");
        this.pnlSignIn.toFront();*/

        this.tfUserName = (TextField) view.lookup("#tfUserName");
        this.pwUserPw = (TextField) view.lookup("#pwUserPw");

        this.pwConfirmPW = (TextField) view.lookup("#pwConfirmPW");
        this.btnLogin = (Button) view.lookup("#btnLogin");
        this.btnSwitchRegister = (Button) view.lookup("#btnSwitchRegister");
        this.btnGuestLogin = (Button) view.lookup("#btnGuestLogin");
        this.lblError = (Label) view.lookup("#lblError");

        this.lblRememberMe = (Label) view.lookup("#lblRememberMe");
        this.btnRememberMe = (CheckBox) view.lookup("#btnRememberMe");
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.lblUserValid = (Label) view.lookup("#lblUserValid");
        this.lblGuestPassword = (Label) view.lookup("#lblGuestPassword");

        editor.getStageManager().getStage(StageEnum.STAGE).setWidth(655);
        editor.getStageManager().getStage(StageEnum.STAGE).setHeight(499);
        editor.getStageManager().getStage(StageEnum.STAGE).centerOnScreen();
        editor.getStageManager().getStage(StageEnum.STAGE).setResizable(false);

        this.lblSignIn = (Label) view.lookup("#lblSignIn");

        this.tfUserName.setOnKeyPressed(this::tfUserNameOnEnter);
        this.pwUserPw.setOnKeyPressed(this::pwUserPwOnEnter);
        this.pwConfirmPW.setOnKeyPressed(this::pwConfirmPWPwOnEnter);

        this.tfUserName.requestFocus();

        this.isLogin = true;
        this.setComponentsTextSignIn();
//        this.btnRegister = (Button) view.lookup("#btnRegister");


//        this.tfUserNameRegister = (TextField) view.lookup("#tfUserNameRegister");
//        this.pwUserPwRegister = (TextField) view.lookup("#pwUserPwRegister");
//        this.btnSwitchLogin = (Button) view.lookup("#btnSwitchLogin");
//        this.btnGuestRegister = (Button) view.lookup("#btnGuestRegister");
//        this.lblErrorRegister = (Label) view.lookup("#lblErrorRegister");
//        this.lblRememberMeRegister = (Label) view.lookup("#lblRememberMeRegister");
//        this.btnRememberMeRegister = (CheckBox) view.lookup("#btnRememberMeRegister");
//        this.btnOptionsRegister = (Button) view.lookup("#btnOptionsRegister");
//        this.lblUserValidRegister = (Label) view.lookup("#lblUserValidRegister");
//        this.lblGuestPasswordRegister = (Label) view.lookup("#lblGuestPasswordRegister");

//        this.lblEnterUserName = (Label) view.lookup("#lblEnterUserName");
//        this.lblEnterPw = (Label) view.lookup("#lblEnterPw");




        // Add necessary action listeners
        this.btnLogin.setOnAction(this::loginButtonAction);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.btnRememberMe.setOnAction(this::btnRememberMeOnClick);
        this.btnGuestLogin.setOnAction(this::btnGuestLoginOnClick);

//        this.btnRegister.setOnAction(this::btnRegisterOnClicked);
//        this.btnOptionsRegister.setOnAction(this::btnOptionsOnClicked);
//        this.btnRememberMeRegister.setOnAction(this::btnRememberMeOnClick);
//        this.btnGuestRegister.setOnAction(this::btnGuestLoginOnClick);
//
//        this.btnSwitchLogin.setOnAction();
        this.btnSwitchRegister.setOnAction(this::btnSwitchRegisterOnClick);

        this.initTooltips();

        this.refreshStage();
    }

    /**
     * Sets texts of all GUI components like buttons, labels etc. in the selected language.
     */
    private void setComponentsTextSignIn() {
        this.lblSignIn.setText(LanguageResolver.getString("LOGIN"));
        this.lblSignIn.setTooltip(new Tooltip(LanguageResolver.getString("LOGIN")));
        this.tfUserName.setPromptText(LanguageResolver.getString("USERNAME"));
        this.pwUserPw.setPromptText(LanguageResolver.getString("PASSWORD"));
        this.pwConfirmPW.setVisible(false);
        this.btnGuestLogin.setVisible(true);

        Objects.requireNonNull(tfUserName).getStyleClass().remove("error");
        Objects.requireNonNull(pwUserPw).getStyleClass().remove("error");
        Objects.requireNonNull(pwConfirmPW).getStyleClass().remove("error");


//        this.lblEnterUserName.setText(LanguageResolver.getString("ENTER_YOUR_USERNAME"));
//        this.lblEnterPw.setText(LanguageResolver.getString("ENTER_YOUR_PASSWORD"));
        this.lblRememberMe.setText(LanguageResolver.getString("REMEMBER_ME"));
        this.btnLogin.setText(LanguageResolver.getString("LOGIN"));

        this.btnSwitchRegister.setText(LanguageResolver.getString("NO_ACCOUNT_YET_REGISTER"));
//        this.btnRegister.setText(LanguageResolver.getString("REGISTER"));
        this.btnGuestLogin.setText(LanguageResolver.getString("GUEST_LOGIN"));

        if (guestUserPassword != null) {
            this.setGuestUserDataLabel();
        }

        this.lblError.setText(LanguageResolver.getString(errorLabelText));

//        this.btnLogin.prefWidthProperty().bind(this.btnRegister.widthProperty());
    }

    private void setComponentsTextSignUp() {
        this.lblSignIn.setText(LanguageResolver.getString("REGISTER"));
        this.lblSignIn.setTooltip(new Tooltip(LanguageResolver.getString("REGISTER")));
        this.tfUserName.setPromptText(LanguageResolver.getString("USERNAME"));
        this.pwUserPw.setPromptText(LanguageResolver.getString("PASSWORD"));
        this.pwConfirmPW.setPromptText(LanguageResolver.getString("CONFIRM_PASSWORD"));
        this.pwConfirmPW.setVisible(true);
        this.btnGuestLogin.setVisible(false);

        Objects.requireNonNull(tfUserName).getStyleClass().remove("error");
        Objects.requireNonNull(pwUserPw).getStyleClass().remove("error");
        Objects.requireNonNull(pwConfirmPW).getStyleClass().remove("error");

//        this.lblEnterUserName.setText(LanguageResolver.getString("ENTER_YOUR_USERNAME"));
//        this.lblEnterPw.setText(LanguageResolver.getString("ENTER_YOUR_PASSWORD"));
        this.lblRememberMe.setText(LanguageResolver.getString("REMEMBER_ME"));
        this.btnLogin.setText(LanguageResolver.getString("REGISTER"));

        this.btnSwitchRegister.setText(LanguageResolver.getString("ALREADY_ACCOUNT_LOGIN"));
//        this.btnRegister.setText(LanguageResolver.getString("REGISTER"));
        this.btnGuestLogin.setText(LanguageResolver.getString("GUEST_LOGIN"));

        if (guestUserPassword != null) {
            this.setGuestUserDataLabel();
        }
        this.lblError.setText(LanguageResolver.getString(errorLabelText));

        //TODO what does that?
//        this.btnLogin.prefWidthProperty().bind(this.btnRegister.widthProperty());
    }
    /**
     * set focus to pwUserPw when enter is pressed
     *
     * @param keyEvent occurs when key is pressed when text area is focused
     */
    private void tfUserNameOnEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            keyEvent.consume();
            this.pwUserPw.requestFocus();
        }
    }

    /**
     * set focus to pwConfirmPW or press login when enter is pressed
     *
     * @param keyEvent occurs when key is pressed when text area is focused
     */
    private void pwUserPwOnEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            keyEvent.consume();
            if (isLogin) {
                loginButtonAction(new ActionEvent());
            }
            else if (lblSignIn.getText().equals(LanguageResolver.getString("REGISTER"))) {
                this.pwConfirmPW.requestFocus();
            }
        }
    }

    /**
     * set focus to pwConfirmPW or press login when enter is pressed
     *
     * @param keyEvent occurs when key is pressed when text area is focused
     */
    private void pwConfirmPWPwOnEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            keyEvent.consume();
            if (lblSignIn.getText().equals(LanguageResolver.getString("REGISTER"))) {
                loginButtonAction(new ActionEvent());
            }
        }
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
//        this.btnOptionsRegister.setTooltip(optionsButton);

        Tooltip loginButton = new Tooltip();
        loginButton.setText(LanguageResolver.getString("LOGIN"));
        this.btnLogin.setTooltip(loginButton);

        Tooltip registerButton = new Tooltip();
        registerButton.setText(LanguageResolver.getString("REGISTER"));
//        this.btnRegister.setTooltip(registerButton);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {

        this.tfUserName.setOnKeyPressed(null);
        this.pwUserPw.setOnKeyPressed(null);
        this.pwConfirmPW.setOnKeyPressed(null);

        // Remove all action listeners
        btnLogin.setOnAction(null);
//        btnRegister.setOnAction(null);
        btnOptions.setOnAction(null);
        btnRememberMe.setOnAction(null);
        btnOptions.setOnAction(null);
        btnSwitchRegister.setOnAction(null);
    }


    /**
     * login user to server and redirect to MainScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void loginButtonAction(ActionEvent actionEvent) {
        if (isLogin) {
            login();
        } else {
            register();
        }
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
            pwConfirmPW.getStyleClass().add("error");
            Platform.runLater(() -> {
                errorLabelText = "USERNAME_PASSWORD_WRONG";
                refreshErrLabelText(errorLabelText);
                this.lblUserValid.setText("");
                this.lblGuestPassword.setText("");
            });
        } else {
            Platform.runLater(() -> {
                //editor.getStageManager().getStage().setResizable(true);
                //editor.getStageManager().getStage().setMaximized(true);
                this.editor.getStageManager().initView(ControllerEnum.MAIN_SCREEN, null, null);

            });
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
            pwConfirmPW.getStyleClass().add("error");
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
     */
    private void register() {
        try {
            String name = this.tfUserName.getText();
            String password = this.pwUserPw.getText();
            String confirmedPassword = this.pwConfirmPW.getText();

            if (tfUserName == null || name.isEmpty() || pwUserPw == null || password.isEmpty() || !password.equals(confirmedPassword)) {
                //reset name and password fields
                Objects.requireNonNull(tfUserName).getStyleClass().add("error");
                Objects.requireNonNull(pwUserPw).getStyleClass().add("error");
                Objects.requireNonNull(pwConfirmPW).getStyleClass().add("error");
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
            this.pwConfirmPW.setText("");
            tfUserName.getStyleClass().add("error");
            pwUserPw.getStyleClass().add("error");
            pwConfirmPW.getStyleClass().add("error");
            Platform.runLater(() -> {
                errorLabelText = "USERNAME_ALREADY_TAKEN";
                refreshErrLabelText(errorLabelText);
                this.lblUserValid.setText("");
                this.lblGuestPassword.setText("");
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
        this.editor.getStageManager().initView(ControllerEnum.OPTION_SCREEN, null, null);
    }

    /**
     * Sends a request to the server and gets name and password for a guest user
     *
     * @param actionEvent occurs when clicking the Guest login button
     */
    private void btnGuestLoginOnClick(ActionEvent actionEvent) {
        editor.getRestManager().guestLogin(this);
        Platform.runLater(() -> {
            errorLabelText = "";
            refreshErrLabelText(errorLabelText);
            tfUserName.getStyleClass().remove("error");
            pwUserPw.getStyleClass().remove("error");
        });
    }

    private void btnSwitchRegisterOnClick(ActionEvent actionEvent) {
        if (isLogin) {
            setComponentsTextSignUp();
            isLogin = false;
        } else {
            setComponentsTextSignIn();
            isLogin = true;
        }
    }

    /**
     * Refreshes the stage after closing the option screen,
     * so that the component texts are displayed in the correct language.
     */
    private void refreshStage() {
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if (isLogin) {
                    setComponentsTextSignIn();
                } else {
                    setComponentsTextSignUp();
                }
                editor.getStageManager().getStage(StageEnum.STAGE).setTitle(LanguageResolver.getString("LOGIN"));
                initTooltips();
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
        lblError.setText(LanguageResolver.getString(errorLabelText));
    }
}