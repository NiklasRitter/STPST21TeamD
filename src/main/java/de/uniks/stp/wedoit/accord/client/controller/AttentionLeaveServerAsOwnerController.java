package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
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

        this.setComponentsText();

        this.btnCancel.setOnAction(this::btnCancelOnClick);
    }

    private void setComponentsText() {
        lblYourAreOwner.setText(LanguageResolver.getString("YOU_ARE_SERVER_OWNER"));
        lblLeaveServerError.setText(LanguageResolver.getString("CAN_NO_LEAVE_SERVER_AS_OWNER"));
        btnCancel.setText(LanguageResolver.getString("CANCEL"));
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
