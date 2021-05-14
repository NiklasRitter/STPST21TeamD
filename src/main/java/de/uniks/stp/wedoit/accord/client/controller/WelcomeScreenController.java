package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.PrivateMessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.WelcomeScreenOnlineUsersCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class WelcomeScreenController {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private final RestClient restClient;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Chat currentChat;
    private ListView<User> lwOnlineUsers;
    private final PropertyChangeListener usersListListener = this::usersListViewChanged;
    private TextField tfPrivateChat;
    private ListView<PrivateMessage> lwPrivateChat;
    private final PropertyChangeListener chatListener = this::newMessage;
    private WelcomeScreenOnlineUsersCellFactory usersListViewCellFactory;
    private PrivateMessageCellFactory chatCellFactory;

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
        this.lblSelectedUser = (Label) view.lookup("#lblSelectedUser");

        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");

        this.btnHome.setOnAction(this::btnHomeOnClicked);
        this.btnLogout.setOnAction(this::btnLogoutOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);

        initTooltips();

        this.initOnlineUsersList();
    private void initTooltips() {
        Tooltip homeButton = new Tooltip();
        homeButton.setText("home");
        btnHome.setTooltip(homeButton);

        Tooltip logoutButton = new Tooltip();
        logoutButton.setText("logout");
        btnLogout.setTooltip(logoutButton);

        Tooltip optionsButton = new Tooltip();
        optionsButton.setText("options");
        btnOptions.setTooltip(optionsButton);
    }

    public void stop() {
        if (this.currentChat != null) {
            this.currentChat.listeners().removePropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
        }
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersListListener);
        this.btnHome.setOnAction(null);
        this.btnLogout.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.tfPrivateChat.setOnAction(null);
        this.lwOnlineUsers.setOnMouseReleased(null);

        this.btnOptions = null;
        this.btnHome = null;
        this.btnLogout = null;
        this.lwOnlineUsers = null;
        this.tfPrivateChat = null;
        this.lwPrivateChat = null;
        this.lblSelectedUser = null;
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
                    editor.getNetworkController().stop();
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

    /**
     * initialize onlineUsers list
     * <p>
     * Load online users from server and add them to the data model.
     * Set CellFactory and build lwOnlineUsers.
     */
    private void initOnlineUsersList() {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray(COM_DATA);

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString(COM_NAME);
                String id = getServersResponse.getJSONObject(index).getString(COM_ID);
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

    /**
     * update automatically the listView when a user joined or left
     *
     * @param propertyChangeEvent event occurs when a user left or joined
     */
    private void usersListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        lwOnlineUsers.getItems().removeAll();
        List<User> availableUser = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());
        Platform.runLater(() -> this.lwOnlineUsers.setItems(FXCollections.observableList(availableUser)));
        lwOnlineUsers.refresh();
    }

    /**
     * initialize private Chat with user
     * <p>
     * Load online users from server and add them to the data model.
     * Set CellFactory and build lwOnlineUsers.
     *
     * @param user selected online user in lwOnlineUsers
     */
    private void initPrivateChat(User user) {
        if (this.currentChat != null) {
            this.currentChat.listeners().removePropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
        }

        if (user.getPrivateChat() == null) {
            user.setPrivateChat(new Chat());
        }
        this.currentChat = user.getPrivateChat();
        this.lblSelectedUser.setText(this.currentChat.getUser().getName());

        // load list view
        chatCellFactory = new PrivateMessageCellFactory();
        lwPrivateChat.setCellFactory(chatCellFactory);
        List<PrivateMessage> messages = currentChat.getMessages().stream().sorted(Comparator.comparing(PrivateMessage::getTimestamp))
                .collect(Collectors.toList());

        this.lwPrivateChat.setItems(FXCollections.observableList(messages));

        // Add listener for the loaded listView
        this.currentChat.listeners().addPropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
    }

    /**
     * update the chat when a new message arrived
     *
     * @param propertyChangeEvent event occurs when a new private message arrives
     */
    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            lwPrivateChat.getItems().removeAll();
            List<PrivateMessage> messages = currentChat.getMessages().stream().sorted(Comparator.comparing(PrivateMessage::getTimestamp))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.lwPrivateChat.setItems(FXCollections.observableList(messages)));
            lwPrivateChat.refresh();
        }
    }

    /**
     * send message in textfield after enter button pressed
     *
     * @param actionEvent occurs when enter button is pressed
     */
    private void tfPrivateChatOnEnter(ActionEvent actionEvent) {
        String message = this.tfPrivateChat.getText();
        this.tfPrivateChat.clear();

        if (message != null && !message.isEmpty() && currentChat != null) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
            editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
        }
    }

    /**
     * initPrivateChat when item of userList is clicked twice
     *
     * @param mouseEvent occurs when a listitem is clicked
     */
    private void onOnlineUserListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            User user = lwOnlineUsers.getSelectionModel().getSelectedItem();
            if (user != null) {
                this.initPrivateChat(user);
            }
        }
    }
}
