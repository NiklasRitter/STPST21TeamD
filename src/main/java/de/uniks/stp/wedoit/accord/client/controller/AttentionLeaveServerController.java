package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class AttentionLeaveServerController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final LocalUser localUser;
    private final Server server;

    private Button btnLeave;
    private Button btnCancel;

    public AttentionLeaveServerController(Parent view, Editor editor, LocalUser localUser, Server server) {
        this.editor = editor;
        this.view = view;
        this.localUser = localUser;
        this.server = server;
    }

    public void init() {
        this.btnLeave = (Button) view.lookup("#btnLeave");
        this.btnCancel = (Button) view.lookup("#btnCancel");

        this.btnLeave.setOnAction(this::btnLeaveOnClick);
        this.btnCancel.setOnAction(this::btnCancelOnClick);
    }


    private void btnLeaveOnClick(ActionEvent actionEvent) {
        this.editor.getLocalUser().withoutServers(this.server);
        this.server.setLocalUser(null);
        this.editor.leaveServer(this.editor.getLocalUser().getUserKey(), this.server.getId());
        StageManager.showMainScreen();
    }

    private void btnCancelOnClick(ActionEvent actionEvent) {
        StageManager.getPopupStage().close();
    }


    @Override
    public void stop() {
        this.btnCancel.setOnAction(null);
        this.btnLeave.setOnAction(null);
    }
}
