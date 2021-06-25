package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.subcontroller.PrivateChatController;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.PRIVATE_CHATS_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

public class PrivateMessageServerScreenController implements Controller {

    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final User memberToWrite;

    private TextField tfMessage;
    private Button btnShowChat;

    public PrivateMessageServerScreenController(Parent root, Editor editor, Server server, User memberToWrite) {
        this.view = root;
        this.editor = editor;
        this.server = server;
        this.memberToWrite = memberToWrite;
    }

    @Override
    public void init() {
        this.tfMessage = (TextField) view.lookup("#tfMessage");
        this.btnShowChat = (Button) view.lookup("#btnShowChat");

        this.tfMessage.setPromptText("Send Message to @" + memberToWrite.getName());
        btnShowChat.setOnAction(this::btnShowChatOnClick);
    }

    @Override
    public void stop() {
        this.btnShowChat.setOnAction(null);
    }

    private void btnShowChatOnClick(ActionEvent actionEvent) {
        this.editor.getStageManager().initView(STAGE, "Private Chats", "PrivateChatsScreen", PRIVATE_CHATS_SCREEN_CONTROLLER, true, null, null);
        Map<String, Controller> controllerMap = this.editor.getStageManager().getControllerMap();
        PrivateChatsScreenController privateChatsScreenController = (PrivateChatsScreenController) controllerMap.get(PRIVATE_CHATS_SCREEN_CONTROLLER);
        privateChatsScreenController.setSelectedUser(memberToWrite);
        privateChatsScreenController.doStuff(memberToWrite);
    }
}
