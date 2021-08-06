package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguagePreferences;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.util.Recorder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Locale;

public class OptionsScreenController implements Controller {

    private final Options options;
    private final Editor editor;
    private Parent view;
    private CheckBox btnDarkMode;
    private Button btnLogout, btnTestSetup, btnSpotify, btnSteam;
    private ChoiceBox<String> choiceBoxLanguage, choiceBoxOutputDevice, choiceBoxInputDevice;
    private Slider sliderFontSize, sliderOutputVolume, sliderInputVolume, sliderInputSensitivity;
    private ProgressBar progressBarTest, progressBarTestBot;
    private VBox vBoxSoundSettings, vBoxExtraSettings;
    private Recorder recorder;
    private Slider sliderZoomLevel;

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
        this.sliderFontSize = (Slider) view.lookup("#sliderFontSize");
        this.sliderZoomLevel = (Slider) view.lookup("#sliderZoomLevel");
        this.sliderOutputVolume = (Slider) view.lookup("#sliderOutputVolume");
        this.sliderInputVolume = (Slider) view.lookup("#sliderInputVolume");
        this.sliderInputSensitivity = (Slider) view.lookup("#sliderInputSensitivity");
        this.choiceBoxLanguage = (ChoiceBox) view.lookup("#choiceBoxLanguage");
        this.choiceBoxInputDevice = (ChoiceBox) view.lookup("#choiceBoxInputDevice");
        this.choiceBoxOutputDevice = (ChoiceBox) view.lookup("#choiceBoxOutputDevice");
        this.progressBarTest = (ProgressBar) view.lookup("#prgBarSetupTest");
        this.progressBarTestBot = (ProgressBar) view.lookup("#progressBarTestBot");

        vBoxSoundSettings = (VBox) view.lookup("#vBoxSoundSettings");
        vBoxExtraSettings = (VBox) view.lookup("#vBoxExtraSettings");

        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).setTitle(LanguageResolver.getString("OPTIONS"));

        createChoiceBoxItems();
        createOutputInputChoiceBox();

        this.btnDarkMode.setSelected(options.isDarkmode());

        changeIfLoginScreen();

        this.btnDarkMode.setOnAction(this::btnDarkModeOnClick);
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.sliderFontSize.setOnMouseReleased(this::fontSizeSliderOnChange);
        this.sliderZoomLevel.setOnMouseReleased(this::zoomLevelSliderOnChange);
        this.sliderOutputVolume.setOnMouseReleased(this::outputVolumeSliderOnChange);
        this.btnTestSetup.setOnAction(this::btnAudioTest);
        progressBarTest.progressProperty().bind(sliderInputSensitivity.valueProperty());
        sliderInputSensitivity.valueProperty().addListener((e, old, n) -> editor.getAccordClient().getOptions().setAudioRootMeanSquare(n.doubleValue()));
        this.btnSteam.setOnAction(this::btnSteamOnClick);
    }

    private void fontSizeSliderOnChange(MouseEvent e) {
        options.setChatFontSize((int) sliderFontSize.getValue());
    }

    private void zoomLevelSliderOnChange(MouseEvent e) {
        options.setZoomLevel((int) sliderFontSize.getValue());
    }

    private void outputVolumeSliderOnChange(MouseEvent e) {
        editor.getAccordClient().getOptions().setSystemVolume((float) sliderOutputVolume.getValue());
    }

    private void createOutputInputChoiceBox() {
        for (Mixer.Info m : AudioSystem.getMixerInfo()) {
            if (m.getDescription().equals("Direct Audio Device: DirectSound Playback")) {
                this.choiceBoxOutputDevice.getItems().add(m.getName());
            } else if (m.getDescription().equals("Direct Audio Device: DirectSound Capture")) {
                this.choiceBoxInputDevice.getItems().add(m.getName());
            }
        }
        if (this.options.getOutputDevice() != null) {
            this.choiceBoxOutputDevice.getSelectionModel().select(this.options.getOutputDevice().getName());
        } else {
            this.choiceBoxOutputDevice.getSelectionModel().select(0);
        }
        if (this.options.getInputDevice() != null) {
            this.choiceBoxInputDevice.getSelectionModel().select(this.options.getInputDevice().getName());
        } else {
            this.choiceBoxInputDevice.getSelectionModel().select(0);
        }

        this.choiceBoxOutputDevice.setOnAction(this::choiceBoxOutputInputSelected);
        this.choiceBoxInputDevice.setOnAction(this::choiceBoxOutputInputSelected);
    }

    private void choiceBoxOutputInputSelected(Event actionEvent) {
        String description = "Direct Audio Device: DirectSound Playback";
        String info = this.choiceBoxOutputDevice.getSelectionModel().getSelectedItem();
        if (actionEvent.getSource() == this.choiceBoxInputDevice) {
            description = "Direct Audio Device: DirectSound Capture";
            info = this.choiceBoxInputDevice.getSelectionModel().getSelectedItem();
        }
        for (Mixer.Info m : AudioSystem.getMixerInfo()) {
            if (m.getName().equals(info) && m.getDescription().equals(description)) {
                if (actionEvent.getSource() == choiceBoxOutputDevice) {
                    this.options.setOutputDevice(m);
                    this.editor.getStageManager().getPrefManager().saveOutputDevice(m.getName());
                } else {
                    this.options.setInputDevice(m);
                    this.editor.getStageManager().getPrefManager().saveInputDevice(m.getName());
                }
                break;
            }
        }
    }

    private void createChoiceBoxItems() {
        this.choiceBoxLanguage.getItems().addAll("English", "Deutsch", "فارسی");

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

    private void changeIfLoginScreen() {
        // If current stage is LoginScreen, than OptionScreen should not show logout button
        if (editor.getLocalUser().getUserKey() == null) {
            vBoxSoundSettings.getChildren().removeAll(vBoxSoundSettings.getChildren());
            vBoxExtraSettings.getChildren().removeAll(vBoxExtraSettings.getChildren());
            this.view.autosize();
            this.view.getScene().getWindow().sizeToScene();
        } else {
            sliderInputSensitivity.setValue(editor.getAccordClient().getOptions().getAudioRootMeanSquare());
            sliderFontSize.setValue(options.getChatFontSize());
            sliderOutputVolume.setValue(editor.getAccordClient().getOptions().getSystemVolume());
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
        btnTestSetup.setOnAction(null);
        sliderFontSize.setOnMouseReleased(null);
        sliderZoomLevel.setOnMouseReleased(null);
        sliderOutputVolume.setOnMouseReleased(null);
        btnTestSetup.setOnAction(null);
        progressBarTest.progressProperty().unbind();
        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
        btnSteam.setOnAction(null);
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


    private void btnAudioTest(ActionEvent actionEvent) {
        if (recorder == null) {
            recorder = new Recorder(progressBarTestBot, editor);
        }
        if (btnTestSetup.getText().equals(LanguageResolver.getString("TEST_SETUP"))) {
            btnTestSetup.setText("STOP");
            recorder.start();
        } else {
            recorder.stop();
            btnTestSetup.setText(LanguageResolver.getString("TEST_SETUP"));
            recorder = null;
        }
    }

    private void btnSteamOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.CONNECT_TO_STEAM_SCREEN, null, null);
    }

}
