package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class AttentionLeaveServerController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final Server server;

    private Button btnLeave;
    private Button btnCancel;

    public AttentionLeaveServerController(Parent view, Editor editor, Server server) {
        this.editor = editor;
        this.view = view;
        this.server = server;
    }

    public void init() {
        this.btnLeave = (Button) view.lookup("#btnLeave");
        this.btnCancel = (Button) view.lookup("#btnCancel");

        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).sizeToScene();
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).centerOnScreen();

        this.btnLeave.setOnAction(this::btnLeaveOnClick);
        this.btnCancel.setOnAction(this::btnCancelOnClick);
    }

    /**
     * leaves a server
     *
     * @param actionEvent actionEvent such a when a button is fired
     */
    private void btnLeaveOnClick(ActionEvent actionEvent) {
        this.editor.leaveServer(this.editor.getLocalUser().getUserKey(), this.server);
        this.editor.getLocalUser().withoutServers(this.server);
        this.server.setLocalUser(null);
        Platform.runLater(() -> editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close());
    }

    /**
     * cancels leaving a server
     *
     * @param actionEvent actionEvent such a when a button is fired
     */
    private void btnCancelOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
    }


    @Override
    public void stop() {
        this.btnCancel.setOnAction(null);
        this.btnLeave.setOnAction(null);
    }
}
