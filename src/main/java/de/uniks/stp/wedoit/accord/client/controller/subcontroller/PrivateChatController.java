package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.PrivateChatsScreenController;
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
import java.util.Comparator;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.Game.GAMEACCEPT;
import static de.uniks.stp.wedoit.accord.client.constants.Game.PREFIX;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.QUOTE_SUFFIX;

public class PrivateChatController implements Controller {

    private final Parent view;
    private final LocalUser localUser;
    private final Editor editor;
    private final PrivateChatsScreenController controller;

    private ContextMenu messageContextMenu;
    private HBox quoteVisible;
    private Label lblQuote;
    private Button btnCancelQuote;
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
    public PrivateChatController(Parent view, LocalUser model, Editor editor, PrivateChatsScreenController controller) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.controller = controller;
    }

    @Override
    public void init() {
        this.btnEmoji = (Button) view.lookup("#btnEmoji");
        this.lwPrivateChat = (ListView<PrivateMessage>) view.lookup("#lwPrivateChat");
        this.quoteVisible = (HBox) view.lookup("#quoteVisible");
        this.btnCancelQuote = (Button) view.lookup("#btnCancelQuote");
        this.lblQuote = (Label) view.lookup("#lblQuote");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");

        this.btnEmoji.setOnAction(this::btnEmojiOnClicked);
        this.lwPrivateChat.setOnMouseClicked(this::onLwPrivatChatClicked);
        this.tfPrivateChat.setOnAction(this::tfPrivateChatOnEnter);
        this.btnCancelQuote.setOnAction(this::cancelQuote);
        quoteVisible.getChildren().clear();
        addMessageContextMenu();
        this.tfPrivateChat.setPromptText("select a User");
        this.tfPrivateChat.setEditable(false);
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
        this.tfPrivateChat.setPromptText("your message");
        this.tfPrivateChat.setEditable(true);

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
                Platform.runLater(() -> controller.setBtnPlayText("Accept"));
            }

            if (message.getText().equals(GAMEACCEPT) && localUser.getGameRequests().contains(editor.getUser(message.getFrom()))) {
                message.setText(message.getText().substring(10));
                Platform.runLater(() -> this.editor.getStageManager().showGameScreen(editor.getUser(message.getFrom())));
            }

            if (message.getText().startsWith(PREFIX)) message.setText(message.getText().substring(PREFIX.length()));

            Platform.runLater(() -> this.privateMessageObservableList.add(message));
        }
    }

//    /**
//     * @param privateMessage
//     */
//    public void newChatMessage(PrivateMessage privateMessage) {
//        List<User> userCell = lwOnlineUsers.getItems().stream().filter(user1 -> user1.getName().equals(privateMessage.getFrom())).collect(Collectors.toList());
//    }

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
            if (lwPrivateChat.getSelectionModel().getSelectedItem() != null && !editor.getMessageManager().isQuote(lwPrivateChat.getSelectionModel().getSelectedItem())) {
                lwPrivateChat.setContextMenu(messageContextMenu);
                messageContextMenu.show(lwPrivateChat, Side.LEFT, 0, 0);
            }
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            if (lwPrivateChat.getSelectionModel().getSelectedItem() != null && editor.getMessageManager().isQuote(lwPrivateChat.getSelectionModel().getSelectedItem())) {
                PrivateMessage message = lwPrivateChat.getSelectionModel().getSelectedItem();
                String cleanMessage = editor.getMessageManager().cleanMessage(message);
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
                editor.getWebSocketManager().sendPrivateChatMessage(quoteMsg.toString());
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMessage.toString());

            } else {
                jsonMsg = JsonUtil.buildPrivateChatMessage(currentChat.getUser().getName(), message);
                editor.getWebSocketManager().sendPrivateChatMessage(jsonMsg.toString());
            }
        }
    }

    public Chat getCurrentChat(){
        return currentChat;
    }

    public TextField getTfPrivateChat() {
        return tfPrivateChat;
    }
}
