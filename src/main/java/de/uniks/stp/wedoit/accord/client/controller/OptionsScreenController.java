package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Options;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;

public class OptionsScreenController implements Controller {

    private final Parent view;
    private final Options options;
    private final Editor editor;

    private CheckBox btnDarkmode;

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
        btnDarkmode = (CheckBox) view.lookup("#btnDarkmode");

        btnDarkmode.setSelected(options.isDarkmode());

        btnDarkmode.setOnAction(this::btnDarkmodeOnClick);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        btnDarkmode = null;
    }

    /**
     * Change the darkmode to the value of the CheckBox
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void btnDarkmodeOnClick(ActionEvent actionEvent) {
        options.setDarkmode(btnDarkmode.isSelected());
    }
}
