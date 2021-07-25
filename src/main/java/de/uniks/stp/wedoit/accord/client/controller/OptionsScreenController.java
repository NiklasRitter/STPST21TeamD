package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Locale;

public class OptionsScreenController implements Controller {

    private Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkMode;
    private Button btnLogout, btnTestSetup, btnSpotify, btnSteam;
    private ChoiceBox<String> choiceBoxLanguage, choiceBoxOutputDevice, choiceBoxInputDevice;
    private Slider sliderTextSize, sliderOutputVolume, sliderInputVolume, sliderInputSensitivity;
    private ProgressBar progressBarTest;
    private VBox vBoxSoundSettings, vBoxExtraSettings;

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
    public void init() {
        this.btnDarkMode = (CheckBox) view.lookup("#btnDarkMode");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.btnSpotify = (Button) view.lookup("#btnSpotify");
        this.btnSteam = (Button) view.lookup("#btnSteam");
        this.btnTestSetup = (Button) view.lookup("#btnTestSetup");
        this.sliderTextSize = (Slider) view.lookup("#sliderTextSize");
        this.sliderOutputVolume = (Slider) view.lookup("#sliderOutputVolume");
        this.sliderInputVolume = (Slider) view.lookup("#sliderInputVolume");
        this.sliderInputSensitivity = (Slider) view.lookup("#sliderInputSensitivity");
        this.choiceBoxLanguage = (ChoiceBox<String>) view.lookup("#choiceBoxLanguage");
        this.choiceBoxInputDevice = (ChoiceBox<String>) view.lookup("#choiceBoxInputDevice");
        this.choiceBoxOutputDevice = (ChoiceBox<String>) view.lookup("#choiceBoxOutputDevice");
        this.progressBarTest = (ProgressBar) view.lookup("prgBarSetupTest");

        vBoxSoundSettings = (VBox) view.lookup("#vBoxSoundSettings");
        vBoxExtraSettings = (VBox) view.lookup("#vBoxExtraSettings");

        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).setTitle(LanguageResolver.getString("OPTIONS"));

        createChoiceBoxItems();
        createOutputInputChoiceBox();

        this.btnDarkMode.setSelected(options.isDarkmode());

        changeIfLoginScreen();

        this.btnDarkMode.setOnAction(this::btnDarkModeOnClick);
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.sliderTextSize.setOnMouseReleased(this::sliderOnChange);
    }

    private void createOutputInputChoiceBox() {
        for(Mixer.Info m : AudioSystem.getMixerInfo()) {
            if(m.getDescription().equals("Direct Audio Device: DirectSound Playback")) {
                this.choiceBoxOutputDevice.getItems().add(m.getName());
            }
            else if(m.getDescription().equals("Direct Audio Device: DirectSound Capture")) {
                this.choiceBoxInputDevice.getItems().add(m.getName());
            }
        }
        if(this.options.getOutputDevice() != null) {
            this.choiceBoxOutputDevice.getSelectionModel().select(this.options.getOutputDevice().getName());
        }
        else {
            this.choiceBoxOutputDevice.getSelectionModel().select(0);
        }
        if(this.options.getInputDevice() != null) {
            this.choiceBoxInputDevice.getSelectionModel().select(this.options.getInputDevice().getName());
        }
        else {
            this.choiceBoxInputDevice.getSelectionModel().select(0);
        }

        this.choiceBoxOutputDevice.setOnAction(this::choiceBoxOutputInputSelected);
        this.choiceBoxInputDevice.setOnAction(this::choiceBoxOutputInputSelected);
    }

    private void choiceBoxOutputInputSelected(ActionEvent actionEvent) {
        String description = "Direct Audio Device: DirectSound Playback";
        String info = this.choiceBoxOutputDevice.getSelectionModel().getSelectedItem();
        if(actionEvent.getSource() == this.choiceBoxInputDevice) {
            description = "Direct Audio Device: DirectSound Capture";
            info = this.choiceBoxInputDevice.getSelectionModel().getSelectedItem();
        }
        for(Mixer.Info m : AudioSystem.getMixerInfo()) {
            if(m.getName().equals(info) && m.getDescription().equals(description)) {
                if(actionEvent.getSource() == choiceBoxOutputDevice) {
                    this.options.setOutputDevice(m);
                    this.editor.getStageManager().getPrefManager().saveOutputDevice(m.getName());
                }
                else {
                    this.options.setInputDevice(m);
                    this.editor.getStageManager().getPrefManager().saveInputDevice(m.getName());
                }
                break;
            }
        }
    }

    private void sliderOnChange(MouseEvent e) {
        editor.saveFontSize((int) sliderTextSize.getValue());
    }


    private void createChoiceBoxItems() {
        this.choiceBoxLanguage.getItems().addAll("English","Deutsch","فارسی");

        switch (Locale.getDefault().getLanguage()) {
            case "fa_ir":
                this.choiceBoxLanguage.getSelectionModel().select(2);
                break;
            case "de_de":
                this.choiceBoxLanguage.getSelectionModel().select(1);
                break;
            case "en_gb":
                this.choiceBoxLanguage.getSelectionModel().select(0);
                break;
        }
        this.choiceBoxLanguage.setOnAction(this::choiceBoxLanguageOnClick);
    }

    private void choiceBoxLanguageOnClick(Event event) {
        Object selectedItem = this.choiceBoxLanguage.getSelectionModel().getSelectedItem();

        switch (selectedItem.toString()) {
            case "English":
                setLanguage("language/Language");
                options.setLanguage("en_GB");
                break;
            case "Deutsch":
                setLanguage("language/Language_de_DE");
                options.setLanguage("de_DE");
                break;
            case "فارسی":
                setLanguage("language/Language_fa_IR");
                options.setLanguage("fa_IR");
                break;
        }
        Scene scene = this.view.getScene();
        this.view = ControllerEnum.OPTION_SCREEN.loadScreen();
        scene.setRoot(this.view);
        this.init();
    }

    private void setLanguage(String languageURL) {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale(languageURL));
        LanguageResolver.load();
        LanguagePreferences.getLanguagePreferences().setLanguage(languageURL);
    }

    private void changeIfLoginScreen(){
        // If current stage is LoginScreen, than OptionScreen should not show logout button
        if (editor.getLocalUser().getUserKey() == null) {
            vBoxSoundSettings.getChildren().removeAll(vBoxSoundSettings.getChildren());
            vBoxExtraSettings.getChildren().removeAll(vBoxExtraSettings.getChildren());
            this.view.autosize();
            this.view.getScene().getWindow().sizeToScene();
        } else {
            sliderTextSize.setValue(editor.getChatFontSizeProperty().getValue());
        }
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        btnDarkMode.setOnAction(null);
        btnLogout.setOnAction(null);
    }

    /**
     * Change the dark mode to the value of the CheckBox
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnDarkModeOnClick(ActionEvent actionEvent) {
        Platform.runLater(() -> options.setDarkmode(btnDarkMode.isSelected()));
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
