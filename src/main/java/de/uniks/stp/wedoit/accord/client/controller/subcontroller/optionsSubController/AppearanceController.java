package de.uniks.stp.wedoit.accord.client.controller.subcontroller.optionsSubController;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeEvent;


public class AppearanceController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkMode;
    private Slider sliderFontSize;
    private Slider sliderZoomLevel;

    public AppearanceController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.btnDarkMode = (CheckBox) view.lookup("#btnDarkMode");
        this.sliderFontSize = (Slider) view.lookup("#sliderFontSize");
        this.sliderZoomLevel = (Slider) view.lookup("#sliderZoomLevel");

        changeIfLoginScreen();

        this.btnDarkMode.setSelected(options.isDarkmode());
        this.sliderFontSize.setValue(editor.getStageManager().getPrefManager().loadChatFontSize());
        this.sliderZoomLevel.setValue(editor.getStageManager().getPrefManager().loadZoomLevel());

        this.sliderFontSize.setOnMouseReleased(this::fontSizeSliderOnChange);
        this.sliderZoomLevel.setOnMouseReleased(this::zoomLevelSliderOnChange);
        this.btnDarkMode.setOnAction(this::btnDarkModeOnClick);
        this.options.listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, this::darkModeChanged);
        this.options.listeners().addPropertyChangeListener(Options.PROPERTY_ZOOM_LEVEL, this::zoomLevelChanged);
    }

    @Override
    public void stop() {
        btnDarkMode.setOnAction(null);
        sliderFontSize.setOnMouseReleased(null);
        sliderZoomLevel.setOnMouseReleased(null);
        this.options.listeners().removePropertyChangeListener(Options.PROPERTY_DARKMODE, this::darkModeChanged);
        this.options.listeners().removePropertyChangeListener(Options.PROPERTY_ZOOM_LEVEL, this::zoomLevelChanged);
    }

    private void darkModeChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> btnDarkMode.setSelected(options.isDarkmode()));
    }

    private void zoomLevelChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> sliderZoomLevel.setValue(options.getZoomLevel()));
    }

    private void changeIfLoginScreen(){
        if (editor.getLocalUser().getUserKey() == null) {
            VBox vBoxAppearance = (VBox) view.lookup("#vBoxAppearance");
            HBox hBoxTextSize = (HBox) view.lookup("#hBoxTextSize");
            HBox hBoxZoomLevel = (HBox) view.lookup("#hBoxZoomLevel");
            vBoxAppearance.getChildren().removeAll(hBoxTextSize, hBoxZoomLevel);
        }
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

    private void zoomLevelSliderOnChange(MouseEvent e) {
        options.setZoomLevel((int) sliderZoomLevel.getValue());
    }
}
