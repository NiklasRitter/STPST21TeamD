package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.MessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.SelectUserCellFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.CHANNEL;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.*;

public class ServerChatController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private Channel currentChannel;
    private final ServerScreenController controller;

    private HBox quoteVisible;
    private Label lbChannelName;
    private Label lblQuote;
    private TextField tfInputMessage;
    private Button btnCancelQuote;
    private Button btnEmoji;
    private ListView<Message> lvTextChat;
    private ObservableList<Message> observableMessageList;
    private final PropertyChangeListener newMessagesListener = this::newMessage;
    private final PropertyChangeListener messageTextChangedListener = this::onMessageTextChanged;
    private ContextMenu localUserMessageContextMenu;
    private ContextMenu userMessageContextMenu;

    private ObservableList<User> selectUserObservableList;
    private ListView<User> lvSelectUser;
    private VBox boxTextfield;
    private AtPositions activeAt;
    private ArrayList<Integer> ats = new ArrayList<>();
    private int caret = 0;
    private ArrayList<AtPositions> atPositions = new ArrayList<>();


    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public ServerChatController(Parent view, LocalUser model, Editor editor, Server server, ServerScreenController controller) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
        this.controller = controller;
    }

    /**
     * Called to start this controller
     * Only call after corresponding fxml is loaded
     * <p>
     * Load necessary GUI elements
     * Add action listeners
     * Add necessary webSocketClients
     */
    public void init() {
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        this.boxTextfield = (VBox) view.lookup("#boxTextfield");
        this.lvTextChat = (ListView<Message>) view.lookup("#lvTextChat");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");
        this.quoteVisible = (HBox) view.lookup("#quoteVisible");
        this.btnCancelQuote = (Button) view.lookup("#btnCancelQuote");
        this.lblQuote = (Label) view.lookup("#lblQuote");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");

        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);
        this.lvTextChat.setOnMousePressed(this::lvTextChatOnClick);
        this.btnCancelQuote.setOnAction(this::cancelQuote);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);
        this.tfInputMessage.setOnKeyTyped(this::isMarking);

        lvSelectUser = new ListView<>();
        lvSelectUser.setVisible(false);

        quoteVisible.getChildren().clear();

        addUserMessageContextMenu();
        addLocalUserMessageContextMenu();

        initToolTip();
    }

    public class AtPositions {

        int start;
        int end;
        boolean complete;

        public AtPositions(int start, int end) {
            this.start = start;
            this.end = end;
            this.complete = false;
        }

        public void shiftLeft() {
            this.start = this.start - 1;
            this.end = this.end - 1;
        }

        public void shiftRight() {
            this.start = this.start + 1;
            this.end = this.end + 1;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean isComplete() {
            return complete;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }
    }

    private void isMarking(KeyEvent keyEvent) {

        if (caret >= tfInputMessage.getCaretPosition() && tfInputMessage.getText().contains("@")) {
            updateAtPositions(keyEvent);
        }

        caret = tfInputMessage.getCaretPosition();

        if (keyEvent.getCharacter().equals("@") && !lvSelectUser.isVisible() && currentChannel != null) {
            activeAt = new AtPositions(caret - 1, caret - 1);
            atPositions.add(activeAt);

            initLwSelectUser(lvSelectUser);

        } else if (keyEvent.getCharacter().equals("\b") && !activeAt.isComplete()) {
            checkMarkingPossible(tfInputMessage.getText().substring(activeAt.getStart() + 1, caret));

        } else if (lvSelectUser.isVisible()) {
            checkMarkingPossible(tfInputMessage.getText().substring(activeAt.getStart() + 1, caret));
        }
    }

    private void initLwSelectUser(ListView<User> lvSelectUser) {

        this.lvSelectUser.setOnMousePressed(this::lvSelectUserOnClick);

        boxTextfield.getChildren().add(lvSelectUser);

        lvSelectUser.setMinHeight(45);
        lvSelectUser.setPrefHeight(45);
        lvSelectUser.setVisible(true);

        // init list view
        lvSelectUser.setCellFactory(new SelectUserCellFactory());

        ArrayList<User> availableUsers;
        if (currentChannel != null && currentChannel.isPrivileged()) {
            availableUsers = new ArrayList<>(currentChannel.getMembers());
        } else {
            availableUsers = new ArrayList<>(server.getMembers());
        }

        this.selectUserObservableList = FXCollections.observableList(availableUsers);

        this.selectUserObservableList.sort((Comparator.comparing(User::isOnlineStatus).reversed()
                .thenComparing(User::getName, String::compareToIgnoreCase).reversed()).reversed());

        this.lvSelectUser.setItems(selectUserObservableList);
    }

    private void lvSelectUserOnClick(MouseEvent mouseEvent) {

        if (mouseEvent.getClickCount() == 1) {
            User selectedUser = lvSelectUser.getSelectionModel().getSelectedItem();
            String currentText = tfInputMessage.getText();

            Integer correspondingAt = null;

            for (int i = caret - 1; i >= 0; i--) {
                if (currentText.charAt(i) == '@') {
                    correspondingAt = i;
                    break;
                }
            }
            if (!(correspondingAt == null)) {

                String firstPart = currentText.substring(0, correspondingAt);
                String secondPart = currentText.substring(caret);
                tfInputMessage.setText(firstPart + "@" + selectedUser.getName() + secondPart);
                activeAt.setEnd(activeAt.getStart() + selectedUser.getName().length() + 1);
                caret = activeAt.getEnd();
                tfInputMessage.positionCaret(caret);
                activeAt.setComplete(true);
                removeSelectionMenu();
            }
        }
    }

    private void updateAtPositions(KeyEvent keyEvent) {
        //akualisiere activeAt in dieser Methode

        String currentText = tfInputMessage.getText();
        int currentCaret = tfInputMessage.getCaretPosition();
        AtPositions atToDelete = null;
        boolean isBackspace = keyEvent.getCharacter().equals("\b");
        for (AtPositions at : atPositions) {
            if (currentCaret < at.getStart()) {
                if (isBackspace) {
                    at.shiftLeft();
                } else {
                    at.shiftRight();
                }
            } else if (currentCaret <= at.getEnd() && at.isComplete()) {
                String start = currentText.substring(0, at.getStart());
                String end;
                if (isBackspace) {
                    end = currentText.substring(at.getEnd() - 1);
                } else {
                    end = currentText.substring(at.getEnd() + 1);
                }
                tfInputMessage.setText(start + end);
                tfInputMessage.positionCaret(start.length());
                atToDelete = at;
            }
        }
        if (atToDelete != null) {
            atPositions.remove(atToDelete);
        }
    }

    private void checkMarkingPossible(String text) {

        ArrayList<User> possibleUsers;

        if (currentChannel != null && currentChannel.isPrivileged()) {
            possibleUsers = new ArrayList<>(currentChannel.getMembers());
        } else {
            possibleUsers = new ArrayList<>(server.getMembers());
        }

        // Was wenn Nutzer gleich heißen nur der andere längeren Namen hat (Eric, EricR)
        for (User user : possibleUsers) {
            if (!user.getName().contains(text)) {
                selectUserObservableList.remove(user);
            } else if (user.getName().equals(text)) {
                activeAt.setComplete(true);
                activeAt.setEnd(caret - 1);
                break;
            } else if (!selectUserObservableList.contains(user)) {
                selectUserObservableList.add(user);
            }
        }

        if (!lvSelectUser.isVisible() && !activeAt.isComplete() && !selectUserObservableList.isEmpty()) {
            showSelectionMenu();
        }

        if (selectUserObservableList.isEmpty() || activeAt.isComplete()) {
            removeSelectionMenu();
        } else {
            activeAt.setEnd(activeAt.getEnd() + 1);
        }
    }

    private void removeSelectionMenu() {
        boxTextfield.getChildren().remove(lvSelectUser);
        lvSelectUser.setVisible(false);
        this.lvSelectUser.setOnMousePressed(null);
    }

    private void showSelectionMenu() {
        boxTextfield.getChildren().add(lvSelectUser);
        lvSelectUser.setVisible(true);
        this.lvSelectUser.setOnMousePressed(this::lvSelectUserOnClick);
    }

    public void initToolTip() {
        Tooltip emojiButton = new Tooltip();
        emojiButton.setText(LanguageResolver.getString("EMOJIS"));
        emojiButton.setStyle("-fx-font-size: 10");
        this.btnEmoji.setTooltip(emojiButton);
    }

    /**
     * Called to stop this controller
     * <p>
     * Remove action listeners
     */
    public void stop() {
        this.tfInputMessage.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.lvTextChat.setOnMouseClicked(null);
        this.btnCancelQuote.setOnAction(null);

        for (MenuItem item : localUserMessageContextMenu.getItems()) {
            item.setOnAction(null);
        }
        for (MenuItem item : userMessageContextMenu.getItems()) {
            item.setOnAction(null);
        }
        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
            for (Message message : this.currentChannel.getMessages()) {
                message.listeners().removePropertyChangeListener(Message.PROPERTY_TEXT, this.messageTextChangedListener);
            }
        }
    }

    /**
     * update the chat when a new message arrived or an old message is deleted
     *
     * @param propertyChangeEvent event occurs when a new private message arrives
     */
    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            Message newMessage = (Message) propertyChangeEvent.getNewValue();
            Platform.runLater(() -> {
                if (this.observableMessageList.isEmpty()) {
                    this.observableMessageList.add(newMessage);
                } else if (newMessage.getTimestamp() <= this.observableMessageList.get(observableMessageList.size() - 1).getTimestamp()) {
                    this.observableMessageList.add(0, newMessage);
                } else this.observableMessageList.add(newMessage);
                newMessage.listeners().addPropertyChangeListener(Message.PROPERTY_TEXT, this.messageTextChangedListener);
            });
        } else {
            Message oldMessage = (Message) propertyChangeEvent.getOldValue();
            Platform.runLater(() -> {
                this.observableMessageList.remove(oldMessage);
                this.lvTextChat.refresh();
            });
        }
    }

    /**
     * refreshes the textChatListView when a message is updated. This is needed, so that the message
     * is displayed correctly
     *
     * @param propertyChangeEvent event occurs when a messageText is changed
     */
    private void onMessageTextChanged(PropertyChangeEvent propertyChangeEvent) {
        Platform.runLater(() -> this.lvTextChat.refresh());
    }

    // Additional methods

    /**
     * adds a context menu for a message from localUser
     */
    public void addLocalUserMessageContextMenu() {
        MenuItem quote = new MenuItem("- " + LanguageResolver.getString("QUOTE"));
        MenuItem updateMessage = new MenuItem("- " + LanguageResolver.getString("UPDATE_MESSAGE_CONTEXT"));
        MenuItem deleteMessage = new MenuItem("- " + LanguageResolver.getString("DELETE_MESSAGE"));
        localUserMessageContextMenu = new ContextMenu();
        localUserMessageContextMenu.setId("localUserMessageContextMenu");
        localUserMessageContextMenu.getItems().add(quote);
        localUserMessageContextMenu.getItems().add(updateMessage);
        localUserMessageContextMenu.getItems().add(deleteMessage);
        quote.setOnAction((event) -> handleContextMenuClicked(QUOTE, lvTextChat.getSelectionModel().getSelectedItem()));
        updateMessage.setOnAction((event) -> handleContextMenuClicked(UPDATE, lvTextChat.getSelectionModel().getSelectedItem()));
        deleteMessage.setOnAction((event) -> handleContextMenuClicked(DELETE, lvTextChat.getSelectionModel().getSelectedItem()));
    }

    /**
     * adds a context menu for a message
     */
    public void addUserMessageContextMenu() {
        MenuItem quote = new MenuItem("- quote");
        userMessageContextMenu = new ContextMenu();
        userMessageContextMenu.setId("userMessageContextMenu");
        userMessageContextMenu.getItems().add(quote);
        quote.setOnAction((event) -> handleContextMenuClicked(QUOTE, lvTextChat.getSelectionModel().getSelectedItem()));
    }

    /**
     * handles when the context menu of the text chat is clicked
     *
     * @param menu    the menu which is clicked like "quote"
     * @param message message which is selected in the text chat
     */
    public void handleContextMenuClicked(String menu, Message message) {
        lvTextChat.setContextMenu(null);
        lvTextChat.getSelectionModel().select(null);
        if (message != null) {
            if (menu.equals(QUOTE)) {
                String messageText = editor.getMessageManager().isQuote(message) ?
                        editor.getMessageManager().cleanQuoteMessage(message) : message.getText();

                String formatted = editor.getMessageManager().getMessageFormatted(message, messageText);
                removeQuote();
                lblQuote.setText(formatted);
                lblQuote.setAccessibleHelp(message.getId());
                quoteVisible.getChildren().add(lblQuote);
                quoteVisible.getChildren().add(btnCancelQuote);
            }
            if (menu.equals(UPDATE)) {
                this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("UPDATE_MESSAGE"),
                        "UpdateMessageScreen", UPDATE_MESSAGE_SCREEN_CONTROLLER,
                        false, message, null);
            }
            if (menu.equals(DELETE)) {
                this.editor.getStageManager().initView(POPUPSTAGE, LanguageResolver.getString("ATTENTION"),
                        "AttentionScreen", ATTENTION_SCREEN_CONTROLLER,
                        false, message, null);
            }
        }
    }

    /**
     * This method cancels a quote
     *
     * @param actionEvent such as when a button is fired
     */
    private void cancelQuote(ActionEvent actionEvent) {
        removeQuote();
    }

    /**
     * removes a quote from the view
     */
    public void removeQuote() {
        lblQuote.setText("");
        quoteVisible.getChildren().clear();
    }

    /**
     * opens the EmojiScreen
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        if (this.currentChannel != null) {
            Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
            this.editor.getStageManager().initView(EMOJIPICKERSTAGE, LanguageResolver.getString("EMOJI_PICKER"), "EmojiScreen", EMOJI_SCREEN_CONTROLLER, false, tfInputMessage, pos);
        }
    }

    /**
     * Checks if "Load more..." is clicked and if yes, then it loads new messages
     */
    private void lvTextChatOnClick(MouseEvent mouseEvent) {
        lvTextChat.setContextMenu(null);
        Message selectedMessage = lvTextChat.getSelectionModel().getSelectedItem();
        if (selectedMessage != null && selectedMessage.getId() != null && selectedMessage.getId().equals("idLoadMore")) {
            this.observableMessageList.remove(0);
            Message oldestMessage = this.observableMessageList.get(0);
            Channel channel = oldestMessage.getChannel();
            String timestamp = String.valueOf(oldestMessage.getTimestamp());
            this.editor.getRestManager().getChannelMessages(this.localUser, this.server, channel.getCategory(), channel, timestamp, this);
        }
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            System.out.println(lvTextChat.getSelectionModel().getSelectedItem().getText());
            if (lvTextChat.getSelectionModel().getSelectedItem() != null) {
                if (lvTextChat.getSelectionModel().getSelectedItem().getFrom().equals(editor.getLocalUser().getName())) {
                    lvTextChat.setContextMenu(localUserMessageContextMenu);
                    localUserMessageContextMenu.show(lvTextChat, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                } else {
                    lvTextChat.setContextMenu(userMessageContextMenu);
                    userMessageContextMenu.show(lvTextChat, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            }
        }
    }

    /**
     * send msg via websocket if enter
     *
     * @param actionEvent occurs on enter
     */
    private void tfInputMessageOnEnter(ActionEvent actionEvent) {
        String message = this.tfInputMessage.getText();
        this.tfInputMessage.clear();

        if (message != null && !message.isEmpty() && currentChannel != null) {

            if (!lblQuote.getText().isEmpty()) {
                JsonObject quoteMsg = JsonUtil.buildServerChatMessage(currentChannel.getId(), QUOTE_PREFIX + lblQuote.getText()
                        + QUOTE_MESSAGE + message + QUOTE_SUFFIX);
                removeQuote();

                editor.getWebSocketManager().sendChannelChatMessage(JsonUtil.stringify(quoteMsg));
            } else {

                JsonObject jsonMsg = JsonUtil.buildServerChatMessage(currentChannel.getId(), message);
                editor.getWebSocketManager().sendChannelChatMessage(JsonUtil.stringify(jsonMsg));
            }
        }
    }

    /**
     * initialize channel Chat
     *
     * @param channel selected channel in lvServerChannels
     */
    public void initChannelChat(Channel channel) {
        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        }

        channel.setRead(true);
        this.currentChannel = channel;
        this.lbChannelName.setText(this.currentChannel.getName());
        this.tfInputMessage.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"));
        this.tfInputMessage.setEditable(this.currentChannel != null);

        // init list view
        lvTextChat.setCellFactory(new MessageCellFactory<>(editor));
        this.observableMessageList = FXCollections.observableList(currentChannel.getMessages().stream().sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList()));

        this.lvTextChat.setItems(observableMessageList);

        // display last 50 messages
        String timestamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
        this.editor.getRestManager().getChannelMessages(this.localUser, this.server, channel.getCategory(), channel, timestamp, this);


        // Add listener for the loaded listView
        this.currentChannel.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        Platform.runLater(() -> this.lvTextChat.scrollTo(this.observableMessageList.size()));
    }

    /**
     * handles new messages loaded over rest in the view
     */
    public void handleGetChannelMessages(Channel channel, JsonArray data) {
        if (channel != null) {
            List<Message> messages = JsonUtil.parseMessageArray(data);
            Collections.reverse(messages);
            this.editor.getMessageManager().updateChannelMessages(channel, messages);
            if (messages.size() == 50) {
                Platform.runLater(this::displayLoadMore);
            }
        } else {
            Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
        }
    }

    /**
     * Displays load more on first position of ListView of the Chat
     */
    private void displayLoadMore() {
        if (observableMessageList.size() >= 50) {
            Message topMessage = new Message().setText("Load more...").setId("idLoadMore");
            this.observableMessageList.add(0, topMessage);
        }
    }

    /**
     * handles channel message by adding it to the data model
     *
     * @param msg message from the server on the channel
     */
    public void handleChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (jsonObject.getString(CHANNEL).equals(currentChannel.getId())) {
            Message message = JsonUtil.parseMessage(jsonObject);
            message.setChannel(currentChannel);

            this.editor.getMessageManager().addNewChannelMessage(message);
        } else {
            Channel channel = controller.getCategoryTreeViewController().getChannelMap().get(jsonObject.getString(CHANNEL));
            if (channel != null) {
                channel.setRead(false);
            }
        }
    }
}
