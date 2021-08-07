package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;


public class AppearanceController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkMode;
    private Slider sliderFontSize;

    public AppearanceController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.btnDarkMode = (CheckBox) view.lookup("#btnDarkMode");
        this.sliderFontSize = (Slider) view.lookup("#sliderFontSize");

        this.btnDarkMode.setSelected(options.isDarkmode());
        this.sliderFontSize.setValue(editor.getStageManager().getPrefManager().loadChatFontSize());

        this.sliderFontSize.setOnMouseReleased(this::fontSizeSliderOnChange);
        this.btnDarkMode.setOnAction(this::btnDarkModeOnClick);
    }

    @Override
    public void stop() {
        btnDarkMode.setOnAction(null);
        sliderFontSize.setOnMouseReleased(null);
    }

    /**
     * Change the dark mode to the value of the CheckBox
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnDarkModeOnClick(ActionEvent actionEvent) {
        options.setDarkmode(btnDarkMode.isSelected());
    }

    private void fontSizeSliderOnChange(MouseEvent e) {
        options.setChatFontSize((int) sliderFontSize.getValue());
    }
}
