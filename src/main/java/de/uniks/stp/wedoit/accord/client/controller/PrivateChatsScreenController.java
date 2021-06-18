package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
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
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class PrivateChatsScreenController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private Button btnOptions, btnPlay;
    private Button btnHome;
    private Button btnEmoji;
    private Chat currentChat;
    private ListView<User> lwOnlineUsers;
    private final PropertyChangeListener usersMessageListListener = this::usersMessageListViewChanged;
    private TextField tfPrivateChat;
    private ListView<PrivateMessage> lwPrivateChat;
    private PrivateChatsScreenOnlineUsersCellFactory usersListViewCellFactory;
    private ObservableList<PrivateMessage> privateMessageObservableList;
    private final PropertyChangeListener chatListener = this::newMessage;
    private ObservableList<User> onlineUserObservableList;
    private final PropertyChangeListener usersOnlineListListener = this::usersOnlineListViewChanged;
    private List<User> availableUsers = new ArrayList<>();
    private final PropertyChangeListener newUsersListener = this::newUser;
    private Label lblSelectedUser;
    private ContextMenu messageContextMenu;
    private HBox quoteVisible;
    private Label lblQuote;
    private Button btnCancelQuote;

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
        this.btnEmoji = (Button) view.lookup("#btnEmoji");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");
        this.lblSelectedUser = (Label) view.lookup("#lblSelectedUser");
        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");
        this.quoteVisible = (HBox) view.lookup("#quoteVisible");
        this.btnCancelQuote = (Button) view.lookup("#btnCancelQuote");
        this.lblQuote = (Label) view.lookup("#lblQuote");

        this.btnHome.setOnAction(this::btnHomeOnClicked);
        this.btnPlay.setOnAction(this::btnPlayOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);
        this.btnEmoji.setOnAction(this::btnEmojiOnClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.lwOnlineUsers.setOnMouseReleased(this::onOnlineUserListViewClicked);
        this.lwPrivateChat.setOnMouseClicked(this::onLwPrivatChatClicked);
        this.btnCancelQuote.setOnAction(this::cancelQuote);
        quoteVisible.getChildren().clear();
        addMessageContextMenu();

        this.initTooltips();

        this.initOnlineUsersList();

        this.tfPrivateChat.setPromptText("select a User");
        this.tfPrivateChat.setEditable(false);
        this.btnPlay.setVisible(false);
    }


    private void addMessageContextMenu() {
        MenuItem quote = new MenuItem("- quote");
        messageContextMenu = new ContextMenu();
        messageContextMenu.setId("messageContextMenu");
        messageContextMenu.getItems().add(quote);
        quote.setOnAction((event) -> {
            handleContextMenuClicked(QUOTE, lwPrivateChat.getSelectionModel().getSelectedItem());
        });
    }


    /**
     * Initializes the Tooltips for the Buttons
     */
    private void initTooltips() {
        Tooltip homeButton = new Tooltip();
        homeButton.setText("home");
        btnHome.setTooltip(homeButton);

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
        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersOnlineListListener);

        for (User user : availableUsers) {
            user.listeners().removePropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().removePropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
        }
        this.btnHome.setOnAction(null);
        this.btnPlay.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.tfPrivateChat.setOnAction(null);
        this.lwOnlineUsers.setOnMouseReleased(null);
        this.btnEmoji.setOnAction(null);
        for (MenuItem item : messageContextMenu.getItems()) {
            item.setOnAction(null);
        }
        messageContextMenu = null;
        btnCancelQuote.setOnAction(null);
    }

    /**
     * redirect to Main Screen
     *
     * @param actionEvent occurs when Home Button is clicked
     */
    private void btnHomeOnClicked(ActionEvent actionEvent) {
        this.editor.getStageManager().showMainScreen();
    }

    /**
     * Send a game request or accept a pending invite, if invite accepted redirect to GameScreen
     *
     * @param actionEvent occurs when Play Button is clicked
     */
    private void btnPlayOnClicked(ActionEvent actionEvent) {
        if (currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Play")) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), GAMEINVITE);
            editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
        } else if (currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Accept")) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), GAMEACCEPT);
            editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
            btnPlay.setText("Play");
            this.editor.getStageManager().showGameScreen(currentChat.getUser());
        }

    }

    /**
     * redirect to Options Menu
     *
     * @param actionEvent occurs when Options Button is clicked
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        this.editor.getStageManager().showOptionsScreen();
    }

    /**
     * Opens the Emoji Picker
     *
     * @param actionEvent occurs when Emoji Button is clicked
     */
    private void btnEmojiOnClicked(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        this.editor.getStageManager().showEmojiScreen(tfPrivateChat, pos);
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
            user.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            user.listeners().addPropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
        }
    }

    /**
     * update automatically the listView when goes offline or online
     *
     * @param propertyChangeEvent event occurs when a users online status changes
     */
    private void usersOnlineListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        User user = (User) propertyChangeEvent.getSource();
        if (!user.isOnlineStatus()) {
            Platform.runLater(() -> {
                this.onlineUserObservableList.remove(user);
                lwOnlineUsers.refresh();
                if (user.getName().equals(this.lblSelectedUser.getText())) {
                    this.tfPrivateChat.setPromptText(user.getName() + " is offline");
                    this.tfPrivateChat.setEditable(false);
                }
            });
        } else {
            Platform.runLater(() -> {
                this.onlineUserObservableList.add(user);
                this.onlineUserObservableList.sort(Comparator.comparing(User::getName));
                lwOnlineUsers.refresh();
                if (user.getName().equals(this.lblSelectedUser.getText())) {
                    this.tfPrivateChat.setPromptText("your message");
                    this.tfPrivateChat.setEditable(true);
                }
            });
        }
    }

    /**
     * Update listview when user gets new message.
     *
     * @param propertyChangeEvent event occurs when a user gets a new message
     */
    private void usersMessageListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> lwOnlineUsers.refresh());
    }

    /**
     * update the listView automatically when a new user joined
     *
     * @param propertyChangeEvent event occurs when a user joined
     */
    private void newUser(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            User newUser = (User) propertyChangeEvent.getNewValue();
            newUser.listeners().addPropertyChangeListener(User.PROPERTY_ONLINE_STATUS, this.usersOnlineListListener);
            newUser.listeners().addPropertyChangeListener(User.PROPERTY_CHAT_READ, this.usersMessageListListener);
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
    public void initPrivateChat(User user) {
        removeQuote();
        if (this.currentChat != null) {
            this.currentChat.listeners().removePropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
        }

        if (user.getPrivateChat() == null) {
            user.setPrivateChat(new Chat());
        }
        this.currentChat = user.getPrivateChat();
        user.setChatRead(true);
        lwOnlineUsers.refresh();
        this.lblSelectedUser.setText(this.currentChat.getUser().getName());
        this.tfPrivateChat.setPromptText("your message");
        this.tfPrivateChat.setEditable(true);
        this.btnPlay.setVisible(true);

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
     * Filter for messages with ###game### prefix and handle when a game invite is accepted
     *
     * @param propertyChangeEvent event occurs when a new private message arrives
     */
    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            PrivateMessage message = (PrivateMessage) propertyChangeEvent.getNewValue();
            if (localUser.getGameInvites().contains(editor.getUser(message.getFrom()))) {
                Platform.runLater(() -> btnPlay.setText("Accept"));
            }

            if (message.getText().equals(GAMEACCEPT) && localUser.getGameRequests().contains(editor.getUser(message.getFrom()))) {
                message.setText(message.getText().substring(10));
                Platform.runLater(() -> this.editor.getStageManager().showGameScreen(editor.getUser(message.getFrom())));
            }

            if (message.getText().startsWith(PREFIX)) message.setText(message.getText().substring(PREFIX.length()));

            Platform.runLater(() -> this.privateMessageObservableList.add(message));
        }
    }

    /**
     * @param privateMessage
     */
    public void newChatMessage(PrivateMessage privateMessage) {
        List<User> userCell = lwOnlineUsers.getItems().stream().filter(user1 -> user1.getName().equals(privateMessage.getFrom())).collect(Collectors.toList());
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
            JsonObject jsonMsg;

            if (!lblQuote.getText().isEmpty()) {
                JsonObject quoteMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), QUOTE_PREFIX + lblQuote.getText() + QUOTE_ID + lblQuote.getAccessibleHelp() + QUOTE_SUFFIX);
                JsonObject jsonMessage = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
                removeQuote();
                editor.getNetworkController().sendPrivateChatMessage(quoteMsg.toString());
                editor.getNetworkController().sendPrivateChatMessage(jsonMessage.toString());


            } else {
                jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
                editor.getNetworkController().sendPrivateChatMessage(jsonMsg.toString());
            }


        }
    }

    /**
     * initPrivateChat when item of userList is clicked twice
     * manages the the Play button
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

    public void handleContextMenuClicked(String menu, PrivateMessage message) {
        lwPrivateChat.setContextMenu(null);
        lwPrivateChat.getSelectionModel().select(null);
        if (message != null) {
            if (menu.equals(QUOTE)) {
                String formatted = editor.getMessageFormatted(message);
                removeQuote();
                lblQuote.setText(formatted);
                lblQuote.setAccessibleHelp(message.getTimestamp() + "");
                quoteVisible.getChildren().add(lblQuote);
                quoteVisible.getChildren().add(btnCancelQuote);
            }
        }
    }

    private void cancelQuote(ActionEvent actionEvent) {
        removeQuote();
    }

    public void removeQuote() {
        lblQuote.setText("");
        quoteVisible.getChildren().clear();
    }

    private void onLwPrivatChatClicked(MouseEvent mouseEvent) {
        lwPrivateChat.setContextMenu(null);
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            if (lwPrivateChat.getSelectionModel().getSelectedItem() != null && !editor.isQuote(lwPrivateChat.getSelectionModel().getSelectedItem())) {
                lwPrivateChat.setContextMenu(messageContextMenu);
                messageContextMenu.show(lwPrivateChat, Side.LEFT, 0, 0);
            }
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (lwPrivateChat.getSelectionModel().getSelectedItem() != null && editor.isQuote(lwPrivateChat.getSelectionModel().getSelectedItem())) {
                PrivateMessage message = lwPrivateChat.getSelectionModel().getSelectedItem();
                String cleanMessage = editor.cleanMessage(message);
                for (PrivateMessage msg : privateMessageObservableList) {
                    if (editor.getMessageFormatted(msg).equals(cleanMessage)) {
                        lwPrivateChat.scrollTo(msg);
                        lwPrivateChat.getSelectionModel().select(msg);
                    }
                }
            }
        }
    }

}
