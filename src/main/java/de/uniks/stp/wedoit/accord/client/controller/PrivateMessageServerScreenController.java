package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.richtext.RichTextArea;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_CHATS_SCREEN_CONTROLLER;

public class PrivateMessageServerScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final User memberToWrite;

    private RichTextArea taMessage;
    private Button btnShowChat;
    private Button btnEmoji;

    private final PropertyChangeListener onlineListener = this::onOnlineChanged;

    public PrivateMessageServerScreenController(Parent root, Editor editor, Server server, User memberToWrite) {
        this.view = root;
        this.editor = editor;
        this.server = server;
        this.memberToWrite = memberToWrite;
    }

    @Override
    public void init() {
        Label lblTitle = (Label) view.lookup("#lblTitle");
        HBox hBoxTextField = (HBox) view.lookup("#hBoxTextField");
        this.taMessage = new RichTextArea();
        taMessage.setId("tfMessage");
        taMessage.getStyleClass().add("textAreaInput");
        hBoxTextField.getChildren().add(0, taMessage);
        this.btnShowChat = (Button) view.lookup("#btnShowChat");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");

        lblTitle.setText(LanguageResolver.getString("SEND_MESSAGE_TO") + " " + memberToWrite.getName());
        this.setCorrectPromptText(memberToWrite.isOnlineStatus());
        this.btnShowChat.setText(LanguageResolver.getString("SHOW_CHAT"));

        this.taMessage.setOnKeyPressed(this::tfMessageOnEnter);
        this.btnShowChat.setOnAction(this::btnShowChatOnClick);
        this.btnEmoji.setOnAction(this::btnEmojiOnClicked);

        this.memberToWrite.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.onlineListener);
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).setTitle(memberToWrite.getName());
    }

    @Override
    public void stop() {
        this.taMessage.setOnKeyPressed(null);
        this.btnShowChat.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.memberToWrite.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.onlineListener);
        this.editor.getStageManager().getStage(StageEnum.POPUP_STAGE).close();
    }

    /**
     * send message in textArea after enter button pressed
     * or
     * enter linebreak when SHIFT + enter is pressed
     *
     * @param keyEvent occurs when key is pressed when text area is focused
     */
    private void tfMessageOnEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            if (keyEvent.isShiftDown()) {
                taMessage.appendText(System.getProperty("line.separator"));
            } else {
                sendMessage(this.taMessage.getText());
            }
        }

    }

    /**
     * helper methode for sending messages to the current chat
     *
     * @param message to be send to currentChat
     */
    private void sendMessage(String message) {
        this.taMessage.clear();

        if (memberToWrite.isOnlineStatus()) {
            if (message != null && !message.isEmpty()) {
                message = message.trim();
                JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(memberToWrite.getName(), message);
                editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
            }
        }
    }

    /**
     * Redirects User to the private Chat of the selected Member
     *
     * @param actionEvent expected actionEvent
     */
    private void btnShowChatOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null);
        Map<String, Controller> controllerMap = this.editor.getStageManager().getControllerMap();
        PrivateChatsScreenController privateChatsScreenController = (PrivateChatsScreenController) controllerMap.get(PRIVATE_CHATS_SCREEN_CONTROLLER);
        privateChatsScreenController.setSelectedUser(memberToWrite);
        privateChatsScreenController.initPrivateChatView(memberToWrite);
        privateChatsScreenController.getLwOnlineUsers().getSelectionModel().select(memberToWrite);
        privateChatsScreenController.setTfPrivateChatText(this.taMessage.getText());
        stop();
    }

    /**
     * gets the position of Emoji Button and pass it to showEmojiScreen
     * @param actionEvent is triggered when emoji button is clicked
     */
    private void btnEmojiOnClicked(ActionEvent actionEvent) {
        if (memberToWrite.isOnlineStatus()) {
            Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
            this.editor.getStageManager().initView(ControllerEnum.EMOJI_PICKER_SCREEN, taMessage, pos);
        }
    }

    /**
     * Is called when the online status of the selected User changes. Changes the textfield properties correctly
     */
    private void onOnlineChanged(PropertyChangeEvent propertyChangeEvent) {
        this.setCorrectPromptText((Boolean) propertyChangeEvent.getNewValue());
    }

    /**
     * A small helper Method that sets the correct promptText
     *
     * @param onlineStatus online status of the selected User
     */
    private void setCorrectPromptText(boolean onlineStatus) {
        this.taMessage.setEditable(onlineStatus);
        if (!onlineStatus) {
            this.taMessage.clear();
            this.taMessage.setPlaceholder(new Text(memberToWrite.getName() + " " + LanguageResolver.getString("IS_OFFLINE")));
        } else {
            this.taMessage.setPlaceholder(new Text(LanguageResolver.getString("SEND_MESSAGE_TO") + " " + memberToWrite.getName()));
        }
    }

}
