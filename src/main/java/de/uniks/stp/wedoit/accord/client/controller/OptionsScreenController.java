package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController.*;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.network.spotify.SpotifyIntegration;
import de.uniks.stp.wedoit.accord.client.util.Recorder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class OptionsScreenController implements Controller {

    private final Options options;
    private final Editor editor;
    private final Parent view;

    private HBox hBoxOuter;
    private Controller currentController;
    private ControllerEnum currentControllerEnum;
    private Parent loadedOptions;
    private Button btnLogout, btnBack, btnAppearance, btnLanguage, btnConnections, btnVoice, btnDescription;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public OptionsScreenController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init(){
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.hBoxOuter = (HBox) view.lookup("#hBoxOuter");
        this.btnBack = (Button) view.lookup("#btnBack");
        this.btnAppearance = (Button) view.lookup("#btnAppearance");
        this.btnLanguage = (Button) view.lookup("#btnLanguage");
        this.btnConnections = (Button) view.lookup("#btnConnections");
        this.btnVoice = (Button) view.lookup("#btnVoice");
        this.btnDescription = (Button) view.lookup("#btnDescription");

        changeIfLoginScreen();

        selectSubController(ControllerEnum.APPEARANCE_OPTIONS_SCREEN);

        this.btnBack.setOnAction(this::btnBackToChats);
        this.btnAppearance.setOnAction(this::btnAppearanceOptions);
        this.btnLanguage.setOnAction(this::btnLanguageOptions);
        this.btnConnections.setOnAction(this::btnConnectionsOptions);
        this.btnVoice.setOnAction(this::btnVoiceOptions);
        this.btnDescription.setOnAction(this::btnDescriptionOptions);
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop(){
        btnBack.setOnAction(null);
        btnLogout.setOnAction(null);
        btnAppearance.setOnAction(null);
        btnConnections.setOnAction(null);
        btnDescription.setOnAction(null);
        btnLanguage.setOnAction(null);
        btnVoice.setOnAction(null);
    }

    private void btnAppearanceOptions(ActionEvent actionEvent) {
        selectSubController(ControllerEnum.APPEARANCE_OPTIONS_SCREEN);
    }

    private void btnLanguageOptions(ActionEvent actionEvent) {
        selectSubController(ControllerEnum.LANGUAGE_OPTIONS_SCREEN);
    }

    private void btnConnectionsOptions(ActionEvent actionEvent) {
        selectSubController(ControllerEnum.CONNECTIONS_OPTIONS_SCREEN);
    }

    private void btnVoiceOptions(ActionEvent actionEvent) {
        selectSubController(ControllerEnum.VOICE_OPTIONS_SCREEN);
    }

    private void btnDescriptionOptions(ActionEvent actionEvent) {
        selectSubController(ControllerEnum.DESCRIPTION_OPTION_SCREEN);
    }

    public void selectSubController(ControllerEnum e){
        if(e != currentControllerEnum || e == ControllerEnum.LANGUAGE_OPTIONS_SCREEN){
            currentControllerEnum = e;
            if(currentController != null) currentController.stop();
            hBoxOuter.getChildren().remove(loadedOptions);
            loadedOptions = e.loadSubOptionScreen();
            hBoxOuter.getChildren().add(loadedOptions);
            String stageTitle = LanguageResolver.getString("OPTIONS");
            switch (e){
                case APPEARANCE_OPTIONS_SCREEN:
                    currentController = new AppearanceController(loadedOptions, options, editor);
                    stageTitle += " - " + LanguageResolver.getString("APPEARANCE");
                    break;
                case LANGUAGE_OPTIONS_SCREEN:
                    currentController = new LanguageController(loadedOptions, options, editor, this);
                    stageTitle += " - " + LanguageResolver.getString("LANGUAGE");
                    btnBack.setText(LanguageResolver.getString("BACK"));
                    btnAppearance.setText(LanguageResolver.getString("APPEARANCE"));
                    btnLanguage.setText(LanguageResolver.getString("LANGUAGE"));
                    btnConnections.setText(LanguageResolver.getString("CONNECTIONS"));
                    btnVoice.setText(LanguageResolver.getString("SOUND"));
                    btnDescription.setText(LanguageResolver.getString("DESCRIPTION"));
                    btnLogout.setText(LanguageResolver.getString("LOGOUT"));
                    break;
                case CONNECTIONS_OPTIONS_SCREEN:
                    currentController = new ConnectionsController(loadedOptions, options, editor);
                    stageTitle += " - " + LanguageResolver.getString("CONNECTIONS");
                    break;
                case VOICE_OPTIONS_SCREEN:
                    currentController = new VoiceController(loadedOptions, options, editor);
                    stageTitle += " - " + LanguageResolver.getString("SOUND");
                    break;
                case DESCRIPTION_OPTION_SCREEN:
                    currentController = new DescriptionController(loadedOptions, options, editor);
                    stageTitle += " - " + LanguageResolver.getString("DESCRIPTION");
                    break;
            }
            editor.getStageManager().getStage(StageEnum.STAGE).setTitle(stageTitle);
            currentController.init();
        }

    }

    /**
     * sends user form options screen back to the private chats
     * @param actionEvent occurs when Back button is pressed
     */
    private void btnBackToChats(ActionEvent actionEvent) {
        if(editor.getLocalUser().getUserKey() != null) editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN,null,null);
        else editor.getStageManager().initView(ControllerEnum.LOGIN_SCREEN,true, null);
    }

    private void changeIfLoginScreen() {
        if (editor.getLocalUser().getUserKey() == null) {
            VBox vBoxButtons = (VBox) view.lookup("#vBoxButtons");
            vBoxButtons.getChildren().removeAll(btnConnections, btnVoice, btnLogout, btnDescription);
        }
    }
    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        editor.logoutUser(editor.getLocalUser().getUserKey());
    }
}
