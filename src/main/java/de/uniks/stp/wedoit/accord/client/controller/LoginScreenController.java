package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class LoginScreenController {

    private LocalUser localUser;
    private Editor editor;
    private Parent view;

    public LoginScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
    }

    public void stop() {
    }

}
