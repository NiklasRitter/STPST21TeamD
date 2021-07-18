package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.MarkingController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class UpdateMessageScreenController implements Controller {

    private final Parent view;
    private final Editor editor;
    private final Message message;
    private final Object stage;
    private TextArea tfUpdateMessage;
    private Button btnDiscard;
    private Button btnUpdateMessage;
    private Label lblError;
    private Button btnEmoji;
    private MarkingController markingController;
    private VBox vboxMarkingSelection;

    public UpdateMessageScreenController(Parent view, Editor editor, Message message, Stage stage) {
        this.view = view;
        this.editor = editor;
        this.message = message;
        this.stage = stage;
    }

    @Override
    public void init() {
        tfUpdateMessage = (TextArea) view.lookup("#tfUpdateMessage");
        btnEmoji = (Button) view.lookup("#btnEmoji");
        btnDiscard = (Button) view.lookup("#btnDiscard");
        btnUpdateMessage = (Button) view.lookup("#btnUpdateMessage");
        lblError = (Label) view.lookup("#lblError");
        vboxMarkingSelection = (VBox) view.lookup("#vboxMarkingSelection");

        String messageText;
        if (editor.getMessageManager().isQuote(message)) {
            messageText = editor.getMessageManager().cleanQuoteMessage(message);
        } else {
            messageText = message.getText();
        }
        tfUpdateMessage.setText(messageText);

        setComponentsText();

        btnDiscard.setOnAction(this::discardChanges);
        btnUpdateMessage.setOnAction(this::updateMessage);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);

        this.markingController = new MarkingController(tfUpdateMessage, message.getChannel(), vboxMarkingSelection);
        this.markingController.init();
    }

    private void setComponentsText() {
        this.btnUpdateMessage.setText(LanguageResolver.getString("SAVE"));
        this.btnDiscard.setText(LanguageResolver.getString("DISCARD"));
    }

    private void updateMessage(ActionEvent actionEvent) {
        String newMessage = tfUpdateMessage.getText();

        if (newMessage.equals(message.getText())) {
            this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
        } else if (newMessage.length() >= 1) {
            if (editor.getMessageManager().isQuote(message)) {
                newMessage = QUOTE_PREFIX + editor.getMessageManager().cleanQuote(message)
                        + QUOTE_MESSAGE + newMessage + QUOTE_SUFFIX;
            }
            editor.getRestManager().updateMessage(editor.getLocalUser(), newMessage, message, this);
        } else {
            Platform.runLater(() -> lblError.setText(LanguageResolver.getString("ERROR_UPDATE_MESSAGE_CHAR_COUNT")));
        }
    }

    private void discardChanges(ActionEvent actionEvent) {
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
    }

    public void handleUpdateMessage(Boolean status) {
        if (status) {
            Platform.runLater(editor.getStageManager().getStage(StageEnum.POPUP_STAGE)::close);
        } else {
            Platform.runLater(() -> lblError.setText(LanguageResolver.getString("ERROR_UPDATE_MESSAGE")));
        }
    }

    /**
     * open the EmojiScreen
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        this.editor.getStageManager().initView(ControllerEnum.EMOJI_PICKER_SCREEN, tfUpdateMessage, pos);
    }

    @Override
    public void stop() {
        btnDiscard.setOnAction(null);
        btnUpdateMessage.setOnAction(null);
        btnEmoji.setOnAction(null);
        markingController.stop();

        btnEmoji = null;
        tfUpdateMessage = null;
        btnDiscard = null;
        btnUpdateMessage = null;
        lblError = null;
    }

}
