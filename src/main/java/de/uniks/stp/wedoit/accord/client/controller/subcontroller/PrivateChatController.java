package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.model.Chat;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.Game.GAME_ACCEPTS;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class PrivateChatController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;

    private ContextMenu messageContextMenu;
    private HBox quoteVisible;
    private Label lblQuote;
    private Button btnCancelQuote, btnPlay;
    private TextField tfPrivateChat;
    private ObservableList<PrivateMessage> privateMessageObservableList;
    private ListView<PrivateMessage> lwPrivateChat;
    private Button btnEmoji;
    private Chat currentChat;
    private final PropertyChangeListener chatListener = this::newMessage;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     */
    public PrivateChatController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    @Override
    public void init() {
        this.btnEmoji = (Button) view.lookup("#btnEmoji");
        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");
        this.quoteVisible = (HBox) view.lookup("#quoteVisible");
        this.btnCancelQuote = (Button) view.lookup("#btnCancelQuote");
        this.lblQuote = (Label) view.lookup("#lblQuote");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");
        this.btnPlay = (Button) view.lookup("#btnPlay");

        this.btnEmoji.setOnAction(this::btnEmojiOnClicked);
        this.lwPrivateChat.setOnMouseClicked(this::onLwPrivatChatClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.btnCancelQuote.setOnAction(this::cancelQuote);
        this.btnPlay.setOnAction(this::btnPlayOnClicked);
        quoteVisible.getChildren().clear();
        addMessageContextMenu();
        this.tfPrivateChat.setPromptText("select a User");
        this.tfPrivateChat.setEditable(false);

        Tooltip emojiButton = new Tooltip();
        emojiButton.setText("Emojis");
        emojiButton.setStyle("-fx-font-size: 10");
        this.btnEmoji.setTooltip(emojiButton);
    }

    @Override
    public void stop() {
        this.lwPrivateChat.setOnMouseClicked(null);
        this.btnCancelQuote.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.tfPrivateChat.setOnAction(null);
        for (MenuItem item : messageContextMenu.getItems()) {
            item.setOnAction(null);
        }
        if (this.currentChat != null) {
            this.currentChat.listeners().removePropertyChangeListener(Chat.PROPERTY_MESSAGES, this.chatListener);
        }
        messageContextMenu = null;
        btnCancelQuote.setOnAction(null);
    }

    /**
     * adds message context menu for messages with the option "quote"
     */
    private void addMessageContextMenu() {
        MenuItem quote = new MenuItem("- quote");
        messageContextMenu = new ContextMenu();
        messageContextMenu.setId("messageContextMenu");
        messageContextMenu.getItems().add(quote);
        quote.setOnAction((event) -> handleContextMenuClicked(QUOTE, lwPrivateChat.getSelectionModel().getSelectedItem()));
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
        editor.updateUserChatRead(user);
        this.tfPrivateChat.setPromptText("your message");
        this.tfPrivateChat.setEditable(true);

        // load list view
        PrivateMessageCellFactory chatCellFactory = new PrivateMessageCellFactory();
        lwPrivateChat.setCellFactory(chatCellFactory);
        List<PrivateMessage> oldMessages = editor.loadOldMessages(user.getName());
        Collections.reverse(oldMessages);
        this.privateMessageObservableList = FXCollections.observableList(oldMessages);
        if (oldMessages.size() == 50) {
            displayLoadMore();
        }

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
            Platform.runLater(() -> this.privateMessageObservableList.add(message));

            if(message.getText().equals(GAME_INVITE) && !message.getFrom().equals(localUser.getName())){
                Platform.runLater(()-> btnPlay.setText("Accept"));
            }
            if(message.getText().equals(GAME_START) && currentChat != null){
                Platform.runLater(()-> btnPlay.setText("Play"));
            }
        }
    }

    /**
     * handles if the context menu is clicked and sets the quote label.
     *
     * @param menu    menu which is selected
     * @param message message which is selected
     */
    public void handleContextMenuClicked(String menu, PrivateMessage message) {
        lwPrivateChat.setContextMenu(null);
        lwPrivateChat.getSelectionModel().select(null);
        if (message != null) {
            if (menu.equals(QUOTE)) {
                String formatted = editor.getMessageManager().getMessageFormatted(message);
                removeQuote();
                lblQuote.setText(formatted);
                lblQuote.setAccessibleHelp(message.getTimestamp() + "");
                quoteVisible.getChildren().add(lblQuote);
                quoteVisible.getChildren().add(btnCancelQuote);
            }
        }
    }

    /**
     * cancels a quote and remove the quote from the view.
     *
     * @param actionEvent such as when the quote cancel button is clicked
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
     * implements that
     * <p>
     * - with a secondary mouse click, the context menu is shown
     * <p>
     * - with a primary mouse click and if the message is a quote, the chat scrolls to the original message
     */
    private void onLwPrivatChatClicked(MouseEvent mouseEvent) {
        lwPrivateChat.setContextMenu(null);
        PrivateMessage selectedMessage = lwPrivateChat.getSelectionModel().getSelectedItem();
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            if (selectedMessage != null && selectedMessage.getId() == null && !editor.getMessageManager().isQuote(selectedMessage)) {
                lwPrivateChat.setContextMenu(messageContextMenu);
                messageContextMenu.show(lwPrivateChat, Side.LEFT, 0, 0);
            }
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (selectedMessage != null &&
                    selectedMessage.getId() != null &&
                    selectedMessage.getId().equals("idLoadMore")) {
                Platform.runLater(this::loadMoreMessages);
            }
            if (selectedMessage != null && editor.getMessageManager().isQuote(selectedMessage)) {
                String cleanMessage = editor.getMessageManager().cleanMessage(selectedMessage);
                for (PrivateMessage msg : privateMessageObservableList) {
                    if (editor.getMessageManager().getMessageFormatted(msg).equals(cleanMessage)) {
                        lwPrivateChat.scrollTo(msg);
                        lwPrivateChat.getSelectionModel().select(msg);
                    }
                }
            }
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
            JsonObject jsonMsg;

            if (!lblQuote.getText().isEmpty()) {
                JsonObject quoteMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), QUOTE_PREFIX + lblQuote.getText() + QUOTE_ID + lblQuote.getAccessibleHelp() + QUOTE_SUFFIX);
                JsonObject jsonMessage = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
                removeQuote();
                editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(quoteMsg));
                editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMessage));

            } else {
                if(message.equals(GAME_INVITE) || message.equals(GAME_ACCEPTS) || message.equals(GAME_CLOSE) || message.equals(GAME_START) || message.equals(GAME_INGAME))
                    message = message.substring(GAME_PREFIX.length());
                jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
                editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
            }
        }
    }

    /**
     * Displays load more on first position of ListView of the Chat
     */
    private void displayLoadMore() {
        if (privateMessageObservableList.size() >= 50) {
            PrivateMessage topMessage = new PrivateMessage().setText("Load more...").setId("idLoadMore");
            this.privateMessageObservableList.add(0, topMessage);
        }
    }

    private void loadMoreMessages() {
        this.privateMessageObservableList.remove(0);
        if (currentChat != null && currentChat.getUser() != null) {
            int offset = privateMessageObservableList.size();
            String userName = currentChat.getUser().getName();
            List<PrivateMessage> olderMessages = editor.loadOlderMessages(userName, offset);
            Collections.reverse(olderMessages);
            privateMessageObservableList.addAll(olderMessages);
            privateMessageObservableList.sort(Comparator.comparingLong(PrivateMessage::getTimestamp));
            if (olderMessages.size() == 50) {
                Platform.runLater(this::displayLoadMore);
            }
        }
    }

    /**
     * Send a game request or accept a pending invite, if invite accepted redirect to GameScreen
     *
     * @param actionEvent occurs when Play Button is clicked
     */
    private void btnPlayOnClicked(ActionEvent actionEvent) {

        if (currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Play") && !localUser.getGameRequests().contains(currentChat.getUser())) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), GAME_INVITE);
            editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
        }else if ((currentChat != null && currentChat.getUser() != null && btnPlay.getText().equals("Accept"))
                &&
                (!editor.getStageManager().getGameStage().isShowing() || editor.getStageManager().getGameStage().getTitle().equals("Result"))) {
            JsonObject jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), GAME_ACCEPTS);
            editor.getWebSocketManager().sendPrivateChatMessage(JsonUtil.stringify(jsonMsg));
        }else if(currentChat != null && currentChat.getUser() != null && editor.getStageManager().getGameStage().isShowing() && !localUser.getGameRequests().contains(currentChat.getUser())){
            privateMessageObservableList.add(new PrivateMessage().setText("###game### System: Cant play two games at once."));
        }

    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    public TextField getTfPrivateChat() {
        return tfPrivateChat;
    }
}
