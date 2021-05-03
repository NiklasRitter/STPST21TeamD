package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.scene.Parent;

public class WelcomeScreenController {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;

    public WelcomeScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
    }

    public void stop() {
    }

}
