package de.uniks.stp.wedoit.accord.client.controller.subcontroller;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.controller.Controller;
import de.uniks.stp.wedoit.accord.client.controller.ServerScreenController;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.EmojiTextFlowParameterHelper;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.MessageCellFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.CHANNEL;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class ServerChatController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private Channel currentChannel;
    private final ServerScreenController controller;

    private HBox quoteVisible;
    private Label lbChannelName;
    private TextArea tfInputMessage;
    private Button btnCancelQuote;
    private Button btnEmoji;
    private ListView<Message> lvTextChat;
    private ObservableList<Message> observableMessageList;
    private final PropertyChangeListener newMessagesListener = this::newMessage;
    private final PropertyChangeListener messageTextChangedListener = this::onMessageTextChanged;
    private ContextMenu contextMenuLocalUserMessage;
    private ContextMenu contextMenuUserMessage;
    private VBox vBoxTextField;
    private MarkingController markingController;
    private EmojiTextFlow quoteTextFlow; // this replaces the quoteLabel
    private String quotedText = ""; // this is needed so that we can access the text inside the quoteTextFlow, since the EmojiTextFlow does not have a getText() method
    private EmojiTextFlowParameters quoteParameter;

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
        this.vBoxTextField = (VBox) view.lookup("#boxTextfield");
        this.tfInputMessage = (TextArea) view.lookup("#tfInputMessage");

        this.lvTextChat = (ListView<Message>) view.lookup("#lvTextChat");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");
        this.quoteVisible = (HBox) view.lookup("#quoteVisible");
        this.btnCancelQuote = (Button) view.lookup("#btnCancelQuote");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");

        this.tfInputMessage.setOnKeyPressed(this::tfInputMessageOnEnter);
        this.lvTextChat.setOnMousePressed(this::lvTextChatOnClick);
        this.btnCancelQuote.setOnAction(this::cancelQuote);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);

        this.markingController = new MarkingController(tfInputMessage, currentChannel, vBoxTextField);
        this.markingController.init();

        quoteVisible.getChildren().clear();

        addUserMessageContextMenu();
        addLocalUserMessageContextMenu();

        this.lvTextChat.styleProperty().bind(Bindings.concat("-fx-font-size: ", editor.getChatFontSizeProperty().asString(), ";"));

        setQuoteParameter();
        this.quoteTextFlow = new EmojiTextFlow(quoteParameter);

        initToolTip();
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
        this.tfInputMessage.setOnKeyPressed(null);
        this.btnEmoji.setOnAction(null);
        this.lvTextChat.setOnMouseClicked(null);
        this.btnCancelQuote.setOnAction(null);
        this.markingController.stop();

        for (MenuItem item : contextMenuLocalUserMessage.getItems()) {
            item.setOnAction(null);
        }
        for (MenuItem item : contextMenuUserMessage.getItems()) {
            item.setOnAction(null);
        }
        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
            for (Message message : this.currentChannel.getMessages()) {
                message.listeners().removePropertyChangeListener(Message.PROPERTY_TEXT, this.messageTextChangedListener);
            }
        }
        if (this.localUser.getAccordClient() != null) {
            this.localUser.getAccordClient().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_DARKMODE, this::onDarkmodeChanged);
        }
        this.editor.getChatFontSizeProperty().removeListener(this::onDarkmodeChanged);
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
        MenuItem copy = new MenuItem("- " + LanguageResolver.getString("COPY"));
        MenuItem updateMessage = new MenuItem("- " + LanguageResolver.getString("UPDATE_MESSAGE_CONTEXT"));
        MenuItem deleteMessage = new MenuItem("- " + LanguageResolver.getString("DELETE_MESSAGE"));
        contextMenuLocalUserMessage = new ContextMenu();
        contextMenuLocalUserMessage.setId("localUserMessageContextMenu");
        contextMenuLocalUserMessage.getItems().add(copy);
        contextMenuLocalUserMessage.getItems().add(quote);
        contextMenuLocalUserMessage.getItems().add(updateMessage);
        contextMenuLocalUserMessage.getItems().add(deleteMessage);
        copy.setOnAction((event) -> handleContextMenuClicked(COPY, lvTextChat.getSelectionModel().getSelectedItem()));
        quote.setOnAction((event) -> handleContextMenuClicked(QUOTE, lvTextChat.getSelectionModel().getSelectedItem()));
        updateMessage.setOnAction((event) -> handleContextMenuClicked(UPDATE, lvTextChat.getSelectionModel().getSelectedItem()));
        deleteMessage.setOnAction((event) -> handleContextMenuClicked(DELETE, lvTextChat.getSelectionModel().getSelectedItem()));
    }

    /**
     * adds a context menu for a message
     */
    public void addUserMessageContextMenu() {
        MenuItem quote = new MenuItem("- " + LanguageResolver.getString("QUOTE"));
        MenuItem copy = new MenuItem("- " + LanguageResolver.getString("COPY"));
        contextMenuUserMessage = new ContextMenu();
        contextMenuUserMessage.setId("userMessageContextMenu");
        contextMenuUserMessage.getItems().add(copy);
        contextMenuUserMessage.getItems().add(quote);
        quote.setOnAction((event) -> handleContextMenuClicked(QUOTE, lvTextChat.getSelectionModel().getSelectedItem()));
        copy.setOnAction((event) -> handleContextMenuClicked(COPY, lvTextChat.getSelectionModel().getSelectedItem()));
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
                quoteTextFlow.parseAndAppend(formatted);
                quotedText = formatted;
                quoteTextFlow.setAccessibleHelp(message.getId());
                quoteVisible.getChildren().add(quoteTextFlow);
                quoteVisible.getChildren().add(btnCancelQuote);
            }
            if (menu.equals(UPDATE)) {
                this.editor.getStageManager().initView(ControllerEnum.UPDATE_MESSAGE_SCREEN, message, null);
            }
            if (menu.equals(DELETE)) {
                this.editor.getStageManager().initView(ControllerEnum.ATTENTION_SCREEN, message, null);
            }
            if (menu.equals(COPY)) {
                editor.copyToSystemClipBoard(message.getText());
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
        setQuoteParameter();
        quoteTextFlow = new EmojiTextFlow(quoteParameter);
        quotedText = "";
        quoteVisible.getChildren().clear();
    }

    private void setQuoteParameter(){
        quoteParameter = new EmojiTextFlowParameterHelper(10).createParameters();
        if (editor.getStageManager().getPrefManager().loadDarkMode()) {
            quoteParameter.setTextColor(Color.valueOf("#ADD8e6"));
        } else {
            quoteParameter.setTextColor(Color.valueOf("#000000"));
        }
    }

    /**
     * opens the EmojiScreen
     */
    private void btnEmojiOnClick(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        if (this.currentChannel != null) {
            Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
            this.editor.getStageManager().initView(ControllerEnum.EMOJI_PICKER_SCREEN, tfInputMessage, pos);
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
            if (lvTextChat.getSelectionModel().getSelectedItem() != null) {
                if (lvTextChat.getSelectionModel().getSelectedItem().getFrom().equals(editor.getLocalUser().getName())) {
                    lvTextChat.setContextMenu(contextMenuLocalUserMessage);
                    contextMenuLocalUserMessage.show(lvTextChat, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                } else {
                    lvTextChat.setContextMenu(contextMenuUserMessage);
                    contextMenuUserMessage.show(lvTextChat, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            }
        }
    }

    /**
     * send msg via websocket if enter
     *
     * @param keyEvent occurs on enter
     */
    private void tfInputMessageOnEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
            if (keyEvent.isShiftDown()) {
                tfInputMessage.appendText(System.getProperty("line.separator"));
            } else {
                sendMessage(this.tfInputMessage.getText());
            }
        }


    }

    private void sendMessage(String message) {
        this.tfInputMessage.clear();

        if (message != null && !message.isEmpty() && currentChannel != null) {
            message = message.trim();

            if (!quotedText.isEmpty()) {
                JsonObject quoteMsg = JsonUtil.buildServerChatMessage(currentChannel.getId(), QUOTE_PREFIX + quotedText
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
            this.markingController.stop();
        }

        channel.setRead(true);
        this.currentChannel = channel;
        this.lbChannelName.setText(this.currentChannel.getName());
        this.tfInputMessage.setPromptText(LanguageResolver.getString("YOUR_MESSAGE"));
        this.tfInputMessage.setEditable(this.currentChannel != null);

        // init list view
        lvTextChat.setCellFactory(new MessageCellFactory<>(this.editor.getStageManager()));
        this.observableMessageList = FXCollections.observableList(currentChannel.getMessages().stream().sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList()));

        this.lvTextChat.setItems(observableMessageList);

        // display last 50 messages
        String timestamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
        this.editor.getRestManager().getChannelMessages(this.localUser, this.server, channel.getCategory(), channel, timestamp, this);


        // Add listener for the loaded listView
        this.currentChannel.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        this.localUser.getAccordClient().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, this::onDarkmodeChanged);
        this.editor.getChatFontSizeProperty().addListener(this::onDarkmodeChanged);
        Platform.runLater(() -> this.lvTextChat.scrollTo(this.observableMessageList.size()));

        this.markingController = new MarkingController(tfInputMessage, currentChannel, vBoxTextField);
        this.markingController.init();
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
            Platform.runLater(() -> this.editor.getStageManager().initView(ControllerEnum.PRIVATE_CHAT_SCREEN,null,null));
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

    public ListView<Message> getLvTextChat() {
        return lvTextChat;
    }



    /**
     * Refreshes chat list in order to update the font and color
     *
     */
    private void onDarkmodeChanged(Object object) {
        this.lvTextChat.refresh();
    }

    public String getQuotedText() {
        return quotedText;
    }

}
