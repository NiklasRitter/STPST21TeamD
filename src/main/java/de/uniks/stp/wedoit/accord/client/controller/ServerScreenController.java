package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.ChannelTreeView;
import de.uniks.stp.wedoit.accord.client.view.MessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;


public class ServerScreenController implements Controller {

    private final LocalUser localUser;
    private final Editor editor;
    private final Parent view;
    private final Server server;
    private final Map<String, Channel> channelMap = new HashMap<>();
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Button btnEmoji;
    private Button btnEdit;
    private Label lbServerName;
    private TreeView<Object> tvServerChannels;
    private final PropertyChangeListener channelReadListener = this::handleChannelReadChange;
    private ListView<User> lvServerUsers;
    private TextField tfInputMessage;
    private Channel currentChannel;
    private ListView<Message> lvTextChat;
    private Label lbChannelName;
    private ObservableList<Message> observableMessageList;
    private PropertyChangeListener newMessagesListener = this::newMessage;
    private TreeItem<Object> tvServerChannelsRoot;
    private WSCallback chatWSCallback = this::handleChatMessage;
    private WSCallback serverWSCallback = this::handleServerMessage;

    /**
     * Create a new Controller
     *
     * @param view   The view this Controller belongs to
     * @param model  The model this Controller belongs to
     * @param editor The editor of the Application
     * @param server The Server this Screen belongs to
     */
    public ServerScreenController(Parent view, LocalUser model, Editor editor, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.server = server;
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
        // Load all view references
        editor.setCurrentServer(server);
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.btnEmoji = (Button) view.lookup("#btnEmoji");
        this.btnEdit = (Button) view.lookup("#btnEdit");

        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.tvServerChannels = (TreeView<Object>) view.lookup("#tvServerChannels");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        this.lvTextChat = (ListView<Message>) view.lookup("#lvTextChat");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");

        this.btnEdit.setVisible(false);

        if (server.getName() != null && !server.getName().equals("")) {
            this.lbServerName.setText(server.getName());
        }
        // Add server websocket
        editor.getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
        // Add chat server web socket
        editor.getNetworkController().haveWebSocket(CHAT_USER_URL + this.editor.getNetworkController().getCleanLocalUserName()
                + AND_SERVER_ID_URL + this.server.getId(), chatWSCallback);

        tvServerChannelsRoot = new TreeItem<>();
        ChannelTreeView channelTreeView = new ChannelTreeView();
        tvServerChannels.setCellFactory(channelTreeView);
        tvServerChannels.setShowRoot(false);
        tvServerChannels.setRoot(tvServerChannelsRoot);

        // get members of this server
        // load categories after get users of a server
        // finally add PropertyChangeListener
        editor.getNetworkController().getExplicitServerInformation(localUser, server, this);

        addActionListener();

        initTooltips();
    }

    private void btnEmojiOnClick(ActionEvent actionEvent) {
        //get the position of Emoji Button and pass it to showEmojiScreen
        Bounds pos = btnEmoji.localToScreen(btnEmoji.getBoundsInLocal());
        StageManager.showEmojiScreen(tfInputMessage, pos);
    }

    public void addActionListener() {

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::optionsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.btnEdit.setOnAction(this::editButtonOnClick);
        this.btnEmoji.setOnAction(this::btnEmojiOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);
        this.tvServerChannels.setOnMouseReleased(this::tvServerChannelsOnDoubleClicked);

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
        this.btnLogout.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnEmoji.setOnAction(null);
        this.btnEdit.setOnAction(null);

        this.tfInputMessage.setOnAction(null);
        this.tvServerChannels.setOnMouseReleased(null);

        for (Channel channel : channelMap.values()) {
            channel.listeners().removePropertyChangeListener(Channel.PROPERTY_READ, channelReadListener);
        }

        this.editor.getNetworkController().withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        this.editor.getNetworkController().withOutWebSocket(CHAT_USER_URL + this.localUser.getName()
                + AND_SERVER_ID_URL + this.server.getId());

        if (this.currentChannel != null) {
            this.currentChannel.listeners()
                    .removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        }
        this.chatWSCallback = null;
        this.serverWSCallback = null;
        this.newMessagesListener = null;

        editor.setCurrentServer(null);
        deleteCurrentServer();
    }

    // Additional methods

    public void deleteCurrentServer() {
        // Delete all connection to the server in the data model
        for (Category category : server.getCategories()) {
            for (Channel channel : category.getChannels()) {
                channel.withoutMembers(new ArrayList<>(channel.getMembers()));
            }
        }
        server.withoutMembers(new ArrayList<>(server.getMembers()));
        localUser.withoutServers(server);
    }

    public void handleGetExplicitServerInformation(JsonArray members) {
        if (members != null) {
            // create users which are member in the server and load user list view
            Platform.runLater(() -> {
                lbServerName.setText(server.getName());
            });


            createUserListView(members);
        } else {
            Platform.runLater(StageManager::showLoginScreen);
        }
        if (this.localUser.getId().equals(this.server.getOwner())) {
            this.btnEdit.setVisible(true);
        }
    }

    /**
     * The localUser will be redirect to the HomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen();
    }

    /**
     * The localUser will be redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }


    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        editor.logoutUser(localUser.getUserKey());

    }

    /**
     * The localUser will be redirected to the EditServerScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void editButtonOnClick(ActionEvent actionEvent) {
        StageManager.showEditServerScreen(this.server);
    }


    /**
     * initialize channel List view
     * gets Categories from server and calls loadCategoryChannels()
     */
    private void initCategoryChannelList() {
        editor.getNetworkController().getCategories(localUser, server, this);
    }

    public void handleGetCategories(List<Category> categoryList) {
        if (categoryList != null) {
            for (Category category : categoryList) {
                TreeItem<Object> categoryItem = new TreeItem<>(category);
                categoryItem.setExpanded(true);

                tvServerChannelsRoot.getChildren().add(categoryItem);
                loadCategoryChannels(category, categoryItem);
            }
        } else {
            System.err.println("Error while loading categories from server");
            Platform.runLater(StageManager::showLoginScreen);
        }
    }

    /**
     * load the channels of a category
     *
     * @param category of which the channels should be loaded
     */
    private void loadCategoryChannels(Category category, TreeItem<Object> categoryItem) {
        editor.getNetworkController().getChannels(localUser, server, category, categoryItem, this);
    }

    public void handleGetChannels(List<Channel> channelList, TreeItem<Object> categoryItem) {
        if (channelList != null) {
            for (Channel channel : channelList) {
                channelMap.put(channel.getId(), channel);
                channel.listeners().addPropertyChangeListener(Channel.PROPERTY_READ, channelReadListener);

                TreeItem<Object> channelItem = new TreeItem<>(channel);
                categoryItem.getChildren().add(channelItem);
            }

        } else {
            System.err.println("Error while loading channels from server");
            Platform.runLater(StageManager::showLoginScreen);
        }
    }


    /**
     * Listen for changes in channel read for unread message markings.
     *
     * @param propertyChangeEvent The event of the property change.
     */
    private void handleChannelReadChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue()) {
            Platform.runLater(() -> {
                tvServerChannels.refresh();
            });
        }
    }

    /**
     * initChannelChat when channel is clicked twice
     *
     * @param mouseEvent occurs when a listitem is clicked
     */
    private void tvServerChannelsOnDoubleClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {

            if (tvServerChannels.getSelectionModel().getSelectedItem() != null) {
                if (((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue() instanceof Channel) {

                    Channel channel = (Channel) ((TreeItem<?>) tvServerChannels.getSelectionModel().getSelectedItem()).getValue();
                    this.initChannelChat(channel);
                }
            }
        }
    }

    /**
     * initialize channel Chat
     *
     * @param channel selected channel in lvServerChannels
     */
    private void initChannelChat(Channel channel) {
        if (this.currentChannel != null) {
            this.currentChannel.listeners().removePropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
        }

        channel.setRead(true);
        this.currentChannel = channel;
        this.lbChannelName.setText(this.currentChannel.getName());

        // init list view
        lvTextChat.setCellFactory(new MessageCellFactory());
        this.observableMessageList = FXCollections.observableList(currentChannel.getMessages().stream().sorted(Comparator.comparing(Message::getTimestamp))
                .collect(Collectors.toList()));

        this.lvTextChat.setItems(observableMessageList);

        // Add listener for the loaded listView
        this.currentChannel.listeners().addPropertyChangeListener(Channel.PROPERTY_MESSAGES, this.newMessagesListener);
    }

    /**
     * update the chat when a new message arrived
     *
     * @param propertyChangeEvent event occurs when a new private message arrives
     */
    private void newMessage(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null) {
            Message newMessage = (Message) propertyChangeEvent.getNewValue();
            Platform.runLater(() -> this.observableMessageList.add(newMessage));
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
            JsonObject jsonMsg = JsonUtil.buildServerChatMessage(currentChannel.getId(), message);
            editor.getNetworkController().sendChannelChatMessage(jsonMsg.toString());
        }
    }

    /**
     * handles channel message by adding it to the data model
     *
     * @param msg message from the server on the channel
     */
    private void handleChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (jsonObject.getString(CHANNEL).equals(currentChannel.getId())) {
            Message message = new Message();
            message.setChannel(currentChannel);
            message.setTimestamp(jsonObject.getJsonNumber(TIMESTAMP).longValue());
            message.setFrom(jsonObject.getString(FROM));
            message.setText(jsonObject.getString(TEXT));

            this.editor.addNewChannelMessage(message);
        } else {
            Channel channel = channelMap.get(jsonObject.getString(CHANNEL));
            if (channel != null) {
                channel.setRead(false);
            }
        }
    }

    // Methods for handle server messages of the websocket

    /**
     * Handles the response of the websocket server
     *
     * @param msg response of the websocket server
     */
    private void handleServerMessage(JsonStructure msg) {
        JsonObject data = ((JsonObject) msg).getJsonObject(DATA);
        String action = ((JsonObject) msg).getString(ACTION);

        // change members
        if (action.equals(USER_JOINED) || action.equals(USER_LEFT) || action.equals(USER_ARRIVED) || action.equals(USER_EXITED)) {
            String id = data.getString(ID);
            String name = data.getString(NAME);

            if (action.equals(USER_EXITED)) {
                editor.userWithoutServer(id, this.server);
            } else {
                // create or get a new user with the data
                User user = editor.haveUserWithServer(name, id, false, this.server);

                if (action.equals(USER_JOINED)) {
                    user.setOnlineStatus(true);
                }
                if (action.equals(USER_LEFT)) {
                    user.setOnlineStatus(false);
                }
                if (action.equals(USER_ARRIVED)) {
                    user.setOnlineStatus(data.getBoolean(ONLINE));
                }
            }
            Platform.runLater(this::updateUserListView);
        }

        // change data of the server
        if (action.equals(SERVER_UPDATED)) {
            server.setName(data.getString(NAME));
            Platform.runLater(() -> lbServerName.setText(server.getName()));
        }
        if (action.equals(SERVER_DELETED)) {
            Platform.runLater(StageManager::showMainScreen);
        }

        //change category
        if (action.equals(CATEGORY_UPDATED)) {
            editor.haveCategory(data.getString(ID), data.getString(NAME), server);
            tvServerChannels.refresh();
        }
        if (action.equals(CATEGORY_CREATED) || action.equals(CATEGORY_DELETED)) {
            changeCategoryTreeItems(action, data);
            tvServerChannels.refresh();
        }

        // change channel
        if (action.equals(CHANNEL_UPDATED)) {
            Channel channel = editor.updateChannel(server, data.getString(ID), data.getString(NAME), data.getString(TYPE),
                    data.getBoolean(PRIVILEGED), data.getString(CATEGORY), data.getJsonArray(MEMBERS));
            if (channel == null) {
                Platform.runLater(() -> StageManager.showServerScreen(server));
            }
            tvServerChannels.refresh();
        }
        if (action.equals(CHANNEL_CREATED) || action.equals(CHANNEL_DELETED)) {
            changeChannelTreeItems(action, data);
            tvServerChannels.refresh();
        }

        // change invitation
        if (action.equals(INVITE_EXPIRED)) {
            // TODO inviteExpired
        }

    }

    /**
     * This method
     * <p>
     * gets the TreeItem with the category as value which value have the same id as from the data.
     * <p>
     * If there is no category whit this id, then create a new category with the data,
     * <p>
     * else if there is a category and the action is "CATEGORY_DELETED" then delete all channels and the category self.
     *
     * @param action action of the web socket message to distinguish between created and deleted
     * @param data   data to handle for the action
     */
    private void changeCategoryTreeItems(String action, JsonObject data) {
        Category category = null;
        TreeItem categoryTreeItem = null;
        for (TreeItem<Object> categoryItem : tvServerChannelsRoot.getChildren()) {
            Category currentCategory = (Category) categoryItem.getValue();
            if (currentCategory.getId().equals(data.getString(ID))) {
                categoryTreeItem = categoryItem;
                category = currentCategory;
                break;
            }
        }
        if (category == null) {
            if (action.equals(CATEGORY_CREATED)) {
                Category newCategory = editor.haveCategory(data.getString(ID), data.getString(NAME), server);
                TreeItem<Object> categoryItem = new TreeItem<>(newCategory);
                categoryItem.setExpanded(true);
                tvServerChannelsRoot.getChildren().add(categoryItem);
            } else {
                Platform.runLater(() -> StageManager.showServerScreen(server));
            }
        } else {
            if (action.equals(CATEGORY_DELETED)) {
                tvServerChannelsRoot.getChildren().remove(categoryTreeItem);
                for (Channel channel : category.getChannels()) {
                    channel.removeYou();
                }
                category.removeYou();
            }

        }
    }

    /**
     * This method
     * <p>
     * gets the TreeItem with the category as value which value have the same id as from the data -> category.
     * <p>
     * If there is no category whit this id, then load the whole server again
     * <p>
     * else if there is a category then get the channel with the same id as from the data
     * <p>
     * --- If there is no channel whit this id, then create a new channel with the data,
     * <p>
     * --- else if there is a channel and the action is "CHANNEL_DELETED" then delete the channel self.
     *
     * @param action action of the web socket message to distinguish between created and deleted
     * @param data   data to handle for the action
     */
    private void changeChannelTreeItems(String action, JsonObject data) {

        TreeItem<Object> category = null;
        for (TreeItem<Object> categoryItem : tvServerChannelsRoot.getChildren()) {
            Category currentCategory = (Category) categoryItem.getValue();
            if (currentCategory.getId().equals(data.getString(CATEGORY))) {
                category = categoryItem;
                break;
            }
        }
        if (category == null) {
            StageManager.showServerScreen(server);
        } else {
            Channel channel = null;
            TreeItem<Object> channelItem = null;
            for (TreeItem<Object> channelItemIterator : category.getChildren()) {
                Channel currentChannel = (Channel) channelItemIterator.getValue();
                if (currentChannel.getId().equals(data.getString(ID))) {
                    channel = currentChannel;
                    channelItem = channelItemIterator;
                    break;
                }
            }

            if (channel == null) {
                if (action.equals(CHANNEL_CREATED)) {
                    Channel newChannel = editor.haveChannel(data.getString(ID), data.getString(NAME),
                            data.getString(TYPE), data.getBoolean(PRIVILEGED), (Category) category.getValue(),
                            data.getJsonArray(MEMBERS));
                    TreeItem<Object> newChannelItem = new TreeItem<>(newChannel);
                    newChannelItem.setExpanded(true);
                    category.getChildren().add(newChannelItem);
                } else {
                    Platform.runLater(() -> StageManager.showServerScreen(server));
                }
            } else {
                if (action.equals(CHANNEL_DELETED)) {
                    category.getChildren().remove(channelItem);
                    channel.removeYou();
                }
            }
        }
    }

    // Methods for the user list view

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param members JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JsonArray members) {
        for (int index = 0; index < members.toArray().length; index++) {

            String name = members.getJsonObject(index).getString(NAME);
            String id = members.getJsonObject(index).getString(ID);
            boolean onlineStatus = members.getJsonObject(index).getBoolean(ONLINE);

            editor.haveUserWithServer(name, id, onlineStatus, server);
        }

        // load categories
        initCategoryChannelList();

        // load list view
        ServerUserListView serverUserListView = new ServerUserListView();
        lvServerUsers.setCellFactory(serverUserListView);
        List<User> users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus))
                .collect(Collectors.toList());
        Collections.reverse(users);
        this.lvServerUsers.setItems(FXCollections.observableList(users));
    }

    /**
     * update user list view
     * <p>
     * remove all items from the list view and put all member of a server back in the list view
     * sorted by online status
     */
    public void updateUserListView() {
        lvServerUsers.getItems().removeAll();
        List<User> users = server.getMembers().stream().sorted(Comparator.comparing(User::isOnlineStatus))
                .collect(Collectors.toList());
        Collections.reverse(users);
        lvServerUsers.setItems(FXCollections.observableList(users));
        lvServerUsers.refresh();
    }
}
