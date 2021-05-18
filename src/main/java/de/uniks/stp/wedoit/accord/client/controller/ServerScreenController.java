package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.*;


public class ServerScreenController {

    private final Server server;
    private RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;
    private Label lbServerName;
    private TreeView tvServerChannels;
    private ListView lvServerUsers;
    private TextField tfInputMessage;
    private ListView listView;
    private WebSocketClient webSocket;
    private WebSocketClient serverWebSocket;
    private WSCallback serverWSCallback = this::handleServerMessage;

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
        this.tvServerChannels = (TreeView) view.lookup("#tvServerChannels");
        this.lvServerUsers = (ListView<User>) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        //TODO what type
        this.listView = (ListView) view.lookup("#lvTextChat");

        this.serverWebSocket = editor.haveWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId(), serverWSCallback);
        serverWebSocket.setCallback(serverWSCallback);

        // get members of this server
        restClient.getExplicitServerInformation(localUser.getUserKey(), server.getId(), response -> {
            if (response.getBody().getObject().getString("status").equals("success")) {
                JSONObject data = response.getBody().getObject().getJSONObject("data");
                JSONArray members = data.getJSONArray("members");
                server.setOwner(data.getString("owner"));

                // create user which are member in the server and load user list view
                createUserListView(members);

            } else {
                stop();
                Platform.runLater(() -> StageManager.showLoginScreen(restClient));
            }
            
        });

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);

        initTooltips();
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
        editor.withOutWebSocket(WS_SERVER_URL + WS_SERVER_ID_URL + server.getId());
        this.serverWebSocket.setCallback(null);
        this.serverWebSocket.stop();
        this.serverWebSocket = null;
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

    private void tfInputMessageOnEnter(ActionEvent actionEvent) {
        // get input message
        String message = this.tfInputMessage.getText();
        this.tfInputMessage.clear();

        /*
        //TODO no general channel yet
        String channelId = this.server.getCategories().get(0).getChannel().getId();

        //TODO multiple channels per category?

        // build message
        JsonObject jsonMsg = JsonUtil.buildServerChatMessage(channelId, message);

        // send message
        this.webSocket.sendMessage(jsonMsg.toString());
         */
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
