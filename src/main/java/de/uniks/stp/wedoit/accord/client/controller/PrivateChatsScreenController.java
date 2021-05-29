package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.PrivateChatsScreenOnlineUsersCellFactory;
import de.uniks.stp.wedoit.accord.client.view.PrivateMessageCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PrivateChatsScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private Button btnOptions,btnPlay;
    private Button btnHome;
    private Button btnLogout;
    private Chat currentChat;
    private ListView<User> lwOnlineUsers;
    private final PropertyChangeListener usersListListener = this::usersListViewChanged;
    private final PropertyChangeListener newUsersListener = this::newUser;
    private TextField tfPrivateChat;
    private ListView<PrivateMessage> lwPrivateChat;
    private final PropertyChangeListener chatListener = this::newMessage;
    private PrivateChatsScreenOnlineUsersCellFactory usersListViewCellFactory;
    private ObservableList<PrivateMessage> privateMessageObservableList;
    private ObservableList<User> onlineUserObservableList;
    private List<User> availableUsers = new ArrayList<>();
    private Label lblSelectedUser;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public PrivateChatsScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }


    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     */
    public void init() {

        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnPlay = (Button) view.lookup("#btnPlay");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");
        this.lblSelectedUser = (Label) view.lookup("#lblSelectedUser");

        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");


        this.btnHome.setOnAction(this::btnHomeOnClicked);
        this.btnPlay.setOnAction(this::btnPlayOnClicked);
        this.btnLogout.setOnAction(this::btnLogoutOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);

        this.initTooltips();

        this.initOnlineUsersList();
    }

    /**
     * Initializes the Tooltips for the Buttons
     */
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

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        if (this.currentChat != null) {
            this.currentChat.listeners().removePropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
        }
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersListListener);

        for (User user : availableUsers) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersListListener);
        }
        this.btnHome.setOnAction(null);
        this.btnPlay.setOnAction(null);
        this.btnLogout.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.tfPrivateChat.setOnAction(null);
        this.lwOnlineUsers.setOnMouseReleased(null);
    }

    /**
     * redirect to Main Screen
     *
     * @param actionEvent occurs when Home Button is clicked
     */
    private void btnHomeOnClicked(ActionEvent actionEvent) {
        StageManager.showMainScreen();
    }

    /**
     * //TODO
     *
     * @param actionEvent occurs when Play Button is clicked
     */
    private void btnPlayOnClicked(ActionEvent actionEvent){
        if(currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Play")){
            String message = "###game### Invites you to Rock - Paper - Scissors!";
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
            editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
        }else if(currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Accept")){
            //when Accept button was pressed
            String message = "###game### Accepts!";
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
            editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
            StageManager.showGameScreen(currentChat.getUser());
        }

    }

    /**
     * logout current LocalUser and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fire
     */
    private void btnLogoutOnClicked(ActionEvent actionEvent) {
        editor.logoutUser(localUser.getUserKey());
    }

    /**
     * redirect to Options Menu
     *
     * @param actionEvent occurs when Options Button is clicked
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
        editor.getNetworkController().getOnlineUsers(localUser, this);
    }

    public void handleGetOnlineUsers() {
        // load list view
        usersListViewCellFactory = new PrivateChatsScreenOnlineUsersCellFactory();
        lwOnlineUsers.setCellFactory(usersListViewCellFactory);
        availableUsers = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList());

        // Add listener for the loaded listView
        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_USERS, this.newUsersListener);
        this.onlineUserObservableList = FXCollections.observableList(availableUsers.stream().filter(User::isOnlineStatus).collect(Collectors.toList()));

        Platform.runLater(() -> this.lwOnlineUsers.setItems(onlineUserObservableList));

        for (User user : availableUsers) {
            user.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersListListener);
        }
    }

    /**
     * update automatically the listView when goes offline or online
     *
     * @param propertyChangeEvent event occurs when a users online status changes
     */
    private void usersListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        if (!user.isOnlineStatus()) {
            Platform.runLater(() -> this.onlineUserObservableList.remove(user));
        } else {
            Platform.runLater(() -> {
                this.onlineUserObservableList.add(user);
                this.onlineUserObservableList.sort(Comparator.comparing(User::getName));
            });
        }
    }

    /**
     * update the listView automatically when a new user joined
     *
     * @param propertyChangeEvent event occurs when a user joined
     */
    private void newUser(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            User newUser = (User) propertyChangeEvent.getNewValue();
            newUser.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersListListener);
            this.availableUsers.add(newUser);
            Platform.runLater(() -> {
                this.onlineUserObservableList.add(newUser);
                this.onlineUserObservableList.sort(Comparator.comparing(User::getName));
                this.lwOnlineUsers.refresh();
            });
        }
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
        PrivateMessageCellFactory chatCellFactory = new PrivateMessageCellFactory();
        lwPrivateChat.setCellFactory(chatCellFactory);
        this.privateMessageObservableList = FXCollections.observableList(currentChat.getMessages().stream().sorted(Comparator.comparing(PrivateMessage::getTimestamp))
                .collect(Collectors.toList()));

        this.lwPrivateChat.setItems(privateMessageObservableList);

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
            PrivateMessage message = (PrivateMessage) propertyChangeEvent.getNewValue();
            if(localUser.getGameInvites().contains(editor.getUser(message.getFrom()))){
                Platform.runLater(() -> btnPlay.setText("Accept"));
            }

            if(message.getText().equals("###game### Accepts!")) {
                message.setText(message.getText().substring(10));
                Platform.runLater(() -> StageManager.showGameScreen(editor.getUser(message.getFrom())));
            }

            Platform.runLater(() -> this.privateMessageObservableList.add(message));
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
            User selectedUser = lwOnlineUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                btnPlay.setText(localUser.getGameInvites().contains(selectedUser) ? "Accept" : "Play");
                this.initPrivateChat(selectedUser);
            }
        }
    }
}
