package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;

public class OptionsScreenController {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;

    private CheckBox btnDarkmode;

    public OptionsScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
        btnDarkmode = (CheckBox) view.lookup("#btnDarkmode");

        btnDarkmode.setSelected(localUser.getOptions().isDarkmode());

        btnDarkmode.setOnAction(this::btnDarkmodeOnClick);
    }

    public void stop() {
        btnDarkmode = null;
    }

    private void btnDarkmodeOnClick(ActionEvent actionEvent) {
        localUser.getOptions().setDarkmode(btnDarkmode.isSelected());
    }

}
