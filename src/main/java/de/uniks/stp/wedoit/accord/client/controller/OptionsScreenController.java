package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController.*;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class OptionsScreenController implements Controller {

    private final Options options;
    private final Editor editor;
    private Parent view;

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

        selectSubController(ControllerEnum.APPEARANCE_OPTIONS_SCREEN);

        changeIfLoginScreen();

        this.btnBack.setOnAction(this::btnBackToChats);
        this.btnAppearance.setOnAction(this::btnAppearanceOptions);
        this.btnLanguage.setOnAction(this::btnLanguageOptions);
        this.btnConnections.setOnAction(this::btnConnectionsOptions);
        this.btnVoice.setOnAction(this::btnVoiceOptions);
        this.btnDescription.setOnAction(this::btnDescriptionOptions);

        this.btnLogout.setOnAction(this::logoutButtonOnClick);

    }

    /**
     * sends user form options screen back to the private chats
     * @param actionEvent occurs when Back button is pressed
     */
    private void btnBackToChats(ActionEvent actionEvent) {
        editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN,null,null);
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

    private void selectSubController(ControllerEnum e){
        if(e != currentControllerEnum){
            currentControllerEnum = e;
            if(currentController != null) currentController.stop();
            hBoxOuter.getChildren().remove(loadedOptions);
            loadedOptions = e.loadSubOptionScreen();
            hBoxOuter.getChildren().add(loadedOptions);
            String stageTitle = LanguageResolver.getString("OPTIONS");
            switch (e){
                case APPEARANCE_OPTIONS_SCREEN:
                    currentController = new AppearanceController(loadedOptions, options, editor);
                    stageTitle += " - Appearance";
                    break;
                case LANGUAGE_OPTIONS_SCREEN:
                    currentController = new LanguageController(loadedOptions, options, editor);
                    stageTitle += " - Language";
                    break;
                case CONNECTIONS_OPTIONS_SCREEN:
                    currentController = new ConnectionsController(loadedOptions, options, editor);
                    stageTitle += " - Connections";
                    break;
                case VOICE_OPTIONS_SCREEN:
                    currentController = new VoiceController(loadedOptions, options, editor);
                    stageTitle += " - Voice";
                    break;
                case DESCRIPTION_OPTION_SCREEN:
                    currentController = new DescriptionController(loadedOptions, options, editor);
                    stageTitle += " - Description";
                    break;
            }
            editor.getStageManager().getStage(StageEnum.STAGE).setTitle(stageTitle);
            currentController.init();
        }

    }



    private void changeIfLoginScreen() {
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




    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        editor.logoutUser(editor.getLocalUser().getUserKey());
    }

}
