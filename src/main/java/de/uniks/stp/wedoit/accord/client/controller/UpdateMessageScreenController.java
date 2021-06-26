package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateMessageScreenController implements Controller {

    private final Parent view;
    private final Editor editor;
    private final Message message;
    private TextField tfUpdateMessage;
    private Button btnDiscard;
    private Button btnUpdateMessage;
    private Label errorLabel;
    private Button btnEmoji;

    public UpdateMessageScreenController(Parent view, Editor editor, Message message) {
        this.view = view;
        this.editor = editor;
        this.message = message;
    }

    @Override
    public void init() {
        tfUpdateMessage = (TextField) view.lookup("#tfUpdateMessage");
        btnEmoji = (Button) view.lookup("#btnEmoji");
        btnDiscard = (Button) view.lookup("#btnDiscard");
        btnUpdateMessage = (Button) view.lookup("#btnUpdateMessage");
        errorLabel = (Label) view.lookup("#lblError");

        tfUpdateMessage.setText(message.getText());

        btnDiscard.setOnAction(this::discardChanges);
        btnUpdateMessage.setOnAction(this::updateMessage);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);
    }

    private void updateMessage(ActionEvent actionEvent) {
        String newMessage = tfUpdateMessage.getText();

        if (newMessage.equals(message.getText())) {
            this.editor.getStageManager().getPopupStage().close();
        } else if (newMessage.length() >= 1) {
            editor.getRestManager().updateMessage(editor.getLocalUser(), newMessage, message, this);
        } else {
            Platform.runLater(() -> errorLabel.setText("Updated message needs at least 1 character!"));
        }
    }

    private void discardChanges(ActionEvent actionEvent) {
        this.editor.getStageManager().getPopupStage().close();
    }

    public void handleUpdateMessage(Boolean status) {
        if (status) {
            Platform.runLater(editor.getStageManager().getPopupStage()::close);
        } else {
            Platform.runLater(() -> errorLabel.setText("An error occurred, please try again later!"));
        }
    }

    /**
     * open the EmojiScreen
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        this.editor.getStageManager().showEmojiScreen(tfUpdateMessage, pos);
    }

    @Override
    public void stop() {
        btnDiscard.setOnAction(null);
        btnUpdateMessage.setOnAction(null);
        btnEmoji.setOnAction(null);

        btnEmoji = null;
        tfUpdateMessage = null;
        btnDiscard = null;
        btnUpdateMessage = null;
        errorLabel = null;
    }

}
