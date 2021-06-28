package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.EMOJI_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_CHATS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.EMOJIPICKERSTAGE;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class PrivateMessageServerScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final User memberToWrite;

    private TextField tfMessage;
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
        this.tfMessage = (TextField) view.lookup("#tfMessage");
        this.btnShowChat = (Button) view.lookup("#btnShowChat");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");

        lblTitle.setText(LanguageResolver.getString("SEND_MESSAGE_TO") +" " + memberToWrite.getName());
        this.setCorrectPromptText(memberToWrite.isOnlineStatus());
        this.btnShowChat.setText(LanguageResolver.getString("SHOW_CHAT"));

        this.tfMessage.setOnAction(this::tfMessageOnEnter);
        this.btnShowChat.setOnAction(this::btnShowChatOnClick);
        this.btnEmoji.setOnAction(this::btnEmojiOnClicked);

        this.memberToWrite.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.onlineListener);
    }

    @Override
    public void stop() {
        this.tfMessage.setOnAction(null);
        this.btnShowChat.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.memberToWrite.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.onlineListener);
    }

    /**
     * send message in textfield after enter button pressed
     *
     * @param actionEvent occurs when enter button is pressed
     */
    private void tfMessageOnEnter(ActionEvent actionEvent) {
        String message = this.tfMessage.getText();
        this.tfMessage.clear();

        if (memberToWrite.isOnlineStatus()) {
            if (message != null && !message.isEmpty()) {
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
        this.editor.getStageManager().initView(STAGE, "Private Chats", "PrivateChatsScreen", PRIVATE_CHATS_SCREEN_CONTROLLER, true, null, null);
        Map<String, Controller> controllerMap = this.editor.getStageManager().getControllerMap();
        PrivateChatsScreenController privateChatsScreenController = (PrivateChatsScreenController) controllerMap.get(PRIVATE_CHATS_SCREEN_CONTROLLER);
        privateChatsScreenController.setSelectedUser(memberToWrite);
        privateChatsScreenController.initPrivateChatView(memberToWrite);
        privateChatsScreenController.getLwOnlineUsers().getSelectionModel().select(memberToWrite);
        privateChatsScreenController.setTfPrivateChatText(this.tfMessage.getText());
    }

    private void btnEmojiOnClicked(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        if (memberToWrite.isOnlineStatus()) {
            Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
            this.editor.getStageManager().initView(EMOJIPICKERSTAGE, "Emoji Picker", "EmojiScreen", EMOJI_SCREEN_CONTROLLER, false, this.tfMessage, pos);
        }
    }

    /**
     * Is called when the online status of the selected User changes. Changes the textfield properties correctly
     */
    private void onOnlineChanged(PropertyChangeEvent propertyChangeEvent) {
        this.setCorrectPromptText((Boolean) propertyChangeEvent.getNewValue());
    }

    // additional helper Methods

    /**
     * A small helper Method that sets the correct promptText
     *
     * @param onlineStatus online status of the selected User
     */
    private void setCorrectPromptText(boolean onlineStatus) {
        this.tfMessage.setEditable(onlineStatus);
        if (!onlineStatus) {
            this.tfMessage.clear();
            this.tfMessage.setPromptText(memberToWrite.getName() + " " + LanguageResolver.getString("IS_OFFLINE"));
        } else {
            this.tfMessage.setPromptText(LanguageResolver.getString("SEND_MESSAGE_TO") + " " + memberToWrite.getName());
        }
    }

}
