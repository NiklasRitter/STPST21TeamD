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

    public OptionsScreenController(Parent view, Options model, Editor editor) {
        this.view = view;
        this.options = model;
        this.editor = editor;
    }

    public void init() {
        btnDarkmode = (CheckBox) view.lookup("#btnDarkmode");

        btnDarkmode.setSelected(options.isDarkmode());

        btnDarkmode.setOnAction(this::btnDarkmodeOnClick);
    }

    public void stop() {
        btnDarkmode.setOnAction(null);
    }

    private void btnDarkmodeOnClick(ActionEvent actionEvent) {
        options.setDarkmode(btnDarkmode.isSelected());
    }
}
