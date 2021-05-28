package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import javafx.scene.Parent;

public class GameScreenController implements Controller {

    private Parent view;
    private LocalUser model;
    private Editor editor;

    public GameScreenController(Parent view, LocalUser model, Editor editor){
        this.view = view;
        this.model = model;
        this.editor = editor;

    }

    @Override
    public void init() {

    }

    @Override
    public void stop() {

    }
}
