package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class AttentionLeaveServerAsOwnerController implements Controller {

    private final Parent view;
    private final Editor editor;
    private Label lblYourAreOwner, lblLeaveServerError;
    private Button btnCancel;


    public AttentionLeaveServerAsOwnerController(Parent view, Editor editor) {
        this.view = view;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.lblYourAreOwner = (Label) view.lookup("#lblYourAreOwner");
        this.lblLeaveServerError = (Label) view.lookup("#lblLeaveServerError");
        this.btnCancel = (Button) view.lookup("#btnCancel");

        this.btnCancel.setOnAction(this::btnCancelOnClick);
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
    }
}
