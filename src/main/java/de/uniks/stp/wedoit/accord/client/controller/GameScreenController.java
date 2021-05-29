package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.scene.Parent;

public class GameScreenController implements Controller {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;
    private User opponent;

    public GameScreenController(Parent view, LocalUser model, User opponent, Editor editor){
        this.view = view;
        this.localUser = model;
        this.opponent = opponent;
        this.editor = editor;

    }

    @Override
    public void init() {

        System.out.println("localUser:" + localUser + ", opponent:" + opponent);

    }

    @Override
    public void stop() {

    }
}
