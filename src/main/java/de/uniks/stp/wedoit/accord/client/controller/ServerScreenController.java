package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import de.uniks.stp.wedoit.accord.client.view.MessageCellFactory;
import de.uniks.stp.wedoit.accord.client.view.ServerScreenChannelsCellFactory;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.*;


public class ServerScreenController implements Controller {

    private final Server server;
    private RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Label lbServerName;
    private ListView lvServerChannels;
    private ListView lvServerUsers;
    private TextField tfInputMessage;
    private ListView listView;
    private WSCallback serverWSCallback = this::handleServerMessage;
    private WSCallback chatWSCallback = this::handleChatMessage;
    private Channel currentChannel;
    private ServerScreenChannelsCellFactory categoriesListViewCellFactory;
    private final PropertyChangeListener newMessagesListener = this::newMessage;
    private ListView<Message> lvTextChat;
    private Label lbChannelName;
    private MessageCellFactory messageCellFactory;
    private ObservableList<Message> observableMessageList;

    public ServerScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient, Server server) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
        this.server = server;
    }

    public void init() {
        // Load all view references
        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerChannels = (ListView<Channel>) view.lookup("#lvServerChannels");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        this.lvTextChat = (ListView<Message>) view.lookup("#lvTextChat");
        this.lbChannelName = (Label) view.lookup("#lbChannelName");

        editor.getNetworkController().haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);

        // get members of this server
        editor.getNetworkController().getExplicitServerInformation(localUser, server, this);

        this.initCategoryChannelList();

        editor.getNetworkController().haveWebSocket(CHAT_USER_URL + this.localUser.getName()
                +  AND_SERVER_ID_URL + this.server.getId(), chatWSCallback);

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);
        this.lvServerChannels.setOnMouseReleased(this::lvServerChannelsOnDoubleClicked);

        initTooltips();
    }

    public void handleGetExplicitServerInformation(JSONArray members) {
        if (members != null) {
            // create users which are member in the server and load user list view
            createUserListView(members);
        } else {
            stop();
            Platform.runLater(() -> StageManager.showLoginScreen(restClient));
        }
    }

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
        this.btnLogout.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnOptions.setOnAction(null);
        this.btnLogout = null;
        this.btnHome = null;
        this.btnOptions = null;
        editor.getNetworkController().withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        editor.getNetworkController().withOutWebSocket(CHAT_USER_URL + this.localUser.getName()
                +  AND_SERVER_ID_URL + this.server.getId());
        this.lbServerName = null;
        this.lvServerChannels = null;
        this.lvServerUsers = null;
        this.tfInputMessage = null;
        this.lvTextChat = null;
    }


    // Additional methods

    private void homeButtonOnClick(ActionEvent actionEvent) {
        stop();
        StageManager.showMainScreen(restClient);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }


    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        editor.logoutUser(localUser.getUserKey(), restClient);

    }

    /**
     * initialize channel List view
     * gets Categories from server and calls loadCategoryChannels()
     */
    private void initCategoryChannelList() {
        restClient.getCategories(this.server.getId(), this.localUser.getUserKey(), categoryResponse -> {
            if (categoryResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray serversCategoryResponse = categoryResponse.getBody().getObject().getJSONArray("data");

                editor.haveCategories(this.server, serversCategoryResponse);

                List<Category> categoryList = this.server.getCategories();
                for (Category category: categoryList) {
                    loadCategoryChannels(category);
                }
            } else {
                System.err.println("Error while loading categories from server");
            }
        });
    }

    /**
     * load the channels of a category
     *
     * @param category of which the channels should be loaded
     */
    private void loadCategoryChannels(Category category) {
        restClient.getChannels(this.server.getId(), category.getId(), localUser.getUserKey(), channelsResponse -> {
            if (channelsResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray categoriesChannelResponse = channelsResponse.getBody().getObject().getJSONArray("data");

                editor.haveChannels(category, categoriesChannelResponse);

                //TODO use something different then a cell factory
                categoriesListViewCellFactory = new ServerScreenChannelsCellFactory();
                lvServerChannels.setCellFactory(categoriesListViewCellFactory);

                List<Channel> channelList = this.server.getCategories().get(0).getChannels().stream().sorted(Comparator.comparing(Channel::getName))
                        .collect(Collectors.toList());

                Platform.runLater(() -> this.lvServerChannels.setItems(FXCollections.observableList(channelList)));
            } else {
                System.err.println("Error while loading channels from server");
            }
        });
    }

    /**
     * initChannelChat when channel is clicked twice
     *
     * @param mouseEvent occurs when a listitem is clicked
     */
    private void lvServerChannelsOnDoubleClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            Channel channel = (Channel) lvServerChannels.getSelectionModel().getSelectedItem();
            if (channel != null) {
                this.initChannelChat(channel);
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

        this.currentChannel = channel;
        this.lbChannelName.setText(this.currentChannel.getName());

        // init list view
        this.messageCellFactory = new MessageCellFactory();
        lvTextChat.setCellFactory(messageCellFactory);
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
     * @param actionEvent
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

        if(jsonObject.getString(COM_CHANNEL).equals(currentChannel.getId())) {
            Message message = new Message();
            message.setChannel(currentChannel);
            message.setTimestamp(jsonObject.getJsonNumber(COM_TIMESTAMP).longValue());
            message.setFrom(jsonObject.getString(COM_FROM));
            message.setText(jsonObject.getString(COM_TEXT));

            this.editor.addNewChannelMessage(message);
        }
    }

    /**
     * Handles the response of the websocket server
     *
     * @param msg response of the websocket server
     */
    private void handleServerMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        // Create a new user if a user has joined and not member of this server or set user online
        if (jsonObject.getString(COM_ACTION).equals(COM_USER_JOINED)) {
            String id = jsonObject.getJsonObject(COM_DATA).getString(COM_ID);
            String name = jsonObject.getJsonObject(COM_DATA).getString(COM_NAME);
            User userJoined = editor.haveUserWithServer(name, id, true, this.server);
            userJoined.setOnlineStatus(true);
        }
        // Create a new user if a user has left and not member of this server or set user offline
        if (jsonObject.getString(COM_ACTION).equals(COM_USER_LEFT)) {
            String id = jsonObject.getJsonObject(COM_DATA).getString(COM_ID);
            String name = jsonObject.getJsonObject(COM_DATA).getString(COM_NAME);
            User userLeft = editor.haveUserWithServer(name, id, false, this.server);
            userLeft.setOnlineStatus(false);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateUserListView();
            }
        });

    }

    // Methods for the user list view

    /**
     * create new users which a member of this server and load user list view with this users,
     * sorted by the online status
     *
     * @param members JSONArray with users formatted as JSONObject
     */
    private void createUserListView(JSONArray members) {
        for (int index = 0; index < members.length(); index++) {

            String name = members.getJSONObject(index).getString("name");
            String id = members.getJSONObject(index).getString("id");
            boolean onlineStatus = members.getJSONObject(index).getBoolean("online");

            editor.haveUserWithServer(name, id, onlineStatus, server);
        }
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
