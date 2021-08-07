package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.util.Recorder;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class VoiceController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private Button btnTestSetup;
    private Slider sliderOutputVolume,sliderInputVolume, sliderInputSensitivity;
    private ChoiceBox<String> choiceBoxInputDevice, choiceBoxOutputDevice;
    private ProgressBar progressBarTest, progressBarTestBot;
    private Recorder recorder;

    public VoiceController(Parent view, Options model, Editor editor){
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.btnTestSetup = (Button) view.lookup("#btnTestSetup");
        this.sliderOutputVolume = (Slider) view.lookup("#sliderOutputVolume");
        this.sliderInputVolume = (Slider) view.lookup("#sliderInputVolume");
        this.sliderInputSensitivity = (Slider) view.lookup("#sliderInputSensitivity");
        this.choiceBoxInputDevice = (ChoiceBox) view.lookup("#choiceBoxInputDevice");
        this.choiceBoxOutputDevice = (ChoiceBox) view.lookup("#choiceBoxOutputDevice");
        this.progressBarTest = (ProgressBar) view.lookup("#prgBarSetupTest");
        this.progressBarTestBot = (ProgressBar) view.lookup("#progressBarTestBot");

        createOutputInputChoiceBox();

        this.sliderOutputVolume.setOnMouseReleased(this::outputVolumeSliderOnChange);

        this.btnTestSetup.setOnAction(this::btnAudioTest);

        progressBarTest.progressProperty().bind(sliderInputSensitivity.valueProperty());
        sliderInputSensitivity.valueProperty().addListener((e, old, n) -> editor.getAccordClient().getOptions().setAudioRootMeanSquare(n.doubleValue()));

    }

    @Override
    public void stop() {
        choiceBoxOutputDevice.setOnAction(null);
        choiceBoxInputDevice.setOnAction(null);
        sliderOutputVolume.setOnMouseReleased(null);
        btnTestSetup.setOnAction(null);

        if (recorder != null) {
            recorder.stop();
            recorder = null;
        }
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

    private void outputVolumeSliderOnChange(MouseEvent e) {
        editor.getAccordClient().getOptions().setSystemVolume((float) sliderOutputVolume.getValue());
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
}
