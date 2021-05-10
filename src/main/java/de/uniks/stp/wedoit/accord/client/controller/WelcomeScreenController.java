package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.PrivateMessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.WelcomeScreenOnlineUsersCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class WelcomeScreenController {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Chat currentChat;

    private final RestClient restClient;
    private ListView<User> lwOnlineUsers;
    private TextField tfPrivateChat;
    private ListView<PrivateMessage> lwPrivateChat;
    private WelcomeScreenOnlineUsersCellFactory usersListViewCellFactory;
    private PrivateMessageCellFactory chatCellFactory;
    private final PropertyChangeListener usersListListener = this::usersListViewChanged;
    private final PropertyChangeListener chatListener = this::newMessage;
    private WebSocketClient websocket;

    public WelcomeScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    public void init() {

        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");

        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");

        this.btnHome.setOnAction(this::btnHomeOnClicked);
        this.btnLogout.setOnAction(this::btnLogoutOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);

        this.initOnlineUsersList();

        try {
            this.websocket = new WebSocketClient(editor, new URI(SYSTEM_SOCKET_URL), this::handleSystemMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error while setting up Websocket connection to system channel");
            e.printStackTrace();
        }

        try {
            this.websocket = new WebSocketClient(editor, new URI(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName()), this::handleChatMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error while setting up Websocket connection to private message channel");
            e.printStackTrace();
        }

    }

    public void stop() {
        this.btnHome.setOnAction(null);
        this.btnLogout.setOnAction(null);
        this.btnOptions.setOnAction(null);

        this.btnOptions = null;
        this.btnHome = null;
        this.btnLogout = null;

        this.websocket.stop();
    }

    /**
     * redirect to Main Screen
     *
     * @param actionEvent
     */
    private void btnHomeOnClicked(ActionEvent actionEvent) {
        StageManager.showMainScreen(restClient);
    }

    /**
     * logout current LocalUser and redirect to the LoginScreen
     *
     * @param actionEvent
     */
    private void btnLogoutOnClicked(ActionEvent actionEvent) {
        String userKey = this.localUser.getUserKey();

        if (userKey != null && !userKey.isEmpty()) {
            restClient.logout(userKey, response -> {
                //if response status is successful
                if (response.getBody().getObject().getString("status").equals("success")) {
                    Platform.runLater(() -> StageManager.showLoginScreen(restClient));
                } else {
                    System.err.println("Error while logging out");
                }
            });
        }
    }

    /**
     * redirect to Options Menu
     *
     * @param actionEvent
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    private void initOnlineUsersList() {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray("data");

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString("name");
                String id = getServersResponse.getJSONObject(index).getString("id");
                editor.haveUser(id, name);
            }

            // load list view
            usersListViewCellFactory = new WelcomeScreenOnlineUsersCellFactory();
            lwOnlineUsers.setCellFactory(usersListViewCellFactory);
            List<User> availableUser = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                    .collect(Collectors.toList());

            Platform.runLater(() -> this.lwOnlineUsers.setItems(FXCollections.observableList(availableUser)));

            // Add listener for the loaded listView
            this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersListListener);
        });
    }

    private void usersListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            lwOnlineUsers.getItems().removeAll();
            List<User> availableUser = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.lwOnlineUsers.setItems(FXCollections.observableList(availableUser)));
            lwOnlineUsers.refresh();
        }
    }

    private void initPrivateChat(User user) {
        if (user.getPrivateChat() == null) {
            user.setPrivateChat(new Chat());
        }
        this.currentChat = user.getPrivateChat();

        // load list view
        chatCellFactory = new PrivateMessageCellFactory();
        lwPrivateChat.setCellFactory(chatCellFactory);
        List<PrivateMessage> messages = currentChat.getMessages().stream().sorted(Comparator.comparing(PrivateMessage::getTimestamp))
                .collect(Collectors.toList());

        this.lwPrivateChat.setItems(FXCollections.observableList(messages));

        // Add listener for the loaded listView
        currentChat.listeners().addPropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
    }

    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            lwPrivateChat.getItems().removeAll();
            List<PrivateMessage> messages = currentChat.getMessages().stream().sorted(Comparator.comparing(PrivateMessage::getTimestamp))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.lwPrivateChat.setItems(FXCollections.observableList(messages)));
            lwPrivateChat.refresh();
        }
    }

    public void handleSystemMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        JsonObject data = jsonObject.getJsonObject("data");

        if (jsonObject.getString(COM_ACTION).equals("userJoined")) {
            this.editor.haveUser(data.getString("id"), data.getString("name"));

        } else if (jsonObject.getString(COM_ACTION).equals("userLeft")) {
            this.editor.userLeft(data.getString("id"));
        }
    }

    private void handleChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        jsonObject.getString(COM_CHANNEL).equals("private");
        PrivateMessage message = new PrivateMessage();
        message.setTimestamp(jsonObject.getInt(COM_TIMESTAMP));
        message.setText(jsonObject.getString(COM_MESSAGE));
        message.setFrom(jsonObject.getString(COM_FROM));
        message.setTo(jsonObject.getString(COM_TO));

        if (jsonObject.getString(COM_FROM).equals(editor.getLocalUser().getName())){
            this.editor.getUser(message.getTo()).getPrivateChat().withMessages(message);
        }
        else {
            this.editor.getUser(message.getFrom()).getPrivateChat().withMessages(message);
        }
    }

    private void tfPrivateChatOnEnter(ActionEvent actionEvent) {
        String message = this.tfPrivateChat.getText();
        this.tfPrivateChat.clear();

        if (message != null && !message.isEmpty() && currentChat != null) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
            this.websocket.sendMessage(jsonMsg.toString());
        }
    }

    private void onOnlineUserListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            User user = lwOnlineUsers.getSelectionModel().getSelectedItem();
            if (user != null) {
                this.initPrivateChat(user);
            }
        }
    }
}
