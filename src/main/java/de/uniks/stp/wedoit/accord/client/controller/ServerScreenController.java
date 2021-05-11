package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.view.ServerUserListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.net.URISyntaxException;
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
    private ListView lvServerChannels;
    private ListView lvServerUsers;
    private TextField tfInputMessage;
    private ListView listView;
    private WebSocketClient webSocket;
    private String test;
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
        this.lvServerChannels = (ListView) view.lookup("#lvServerChannels");
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
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        StageManager.showLoginScreen(restClient);
                    }
                });
            }

        });

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);

        try {
            this.webSocket = new WebSocketClient(editor, new URI(CHAT_USER_URL + this.localUser.getName()
                    + "&" +  SERVER_ID_URL + this.server.getId()), this::handleMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error creating URI");
            e.printStackTrace();
        }

        this.loadServerData();
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

    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    private void tfInputMessageOnEnter(ActionEvent actionEvent) {
        // get input message
        String message = this.tfInputMessage.getText();
        this.tfInputMessage.clear();

        System.out.println(message);

        /*
        String channelId;

        // build message
        JsonObject jsonMsg = JsonUtil.buildServerChatMessage(channelId, message);

        // send message
        this.webSocket.sendMessage(jsonMsg.toString());
         */
    }

    private void loadServerData() {
        System.out.println(this.server.getCategories());

        restClient.getCategories(this.server.getId(), this.localUser.getUserKey(), categoryResponse -> {
            if (categoryResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray serversCategoryResponse = categoryResponse.getBody().getObject().getJSONArray("data");

                for (int index1 = 0; index1 < serversCategoryResponse.length(); index1++) {
                    String categoryId = serversCategoryResponse.getJSONObject(index1).getString("id");
                    String categoryName = serversCategoryResponse.getJSONObject(index1).getString("name");
                    String categoryServer = serversCategoryResponse.getJSONObject(index1).getString("server");

                    Category category = new Category().setId(categoryId).setName(categoryName).setServer(server);

                    restClient.getChannels(this.server.getId(), categoryId, localUser.getUserKey(), channelsResponse -> {
                        if (channelsResponse.getBody().getObject().getString("status").equals("success")) {
                            JSONArray serverChannelResponse = channelsResponse.getBody().getObject().getJSONArray("data");
                            //System.out.println(serverChannelResponse.toString());
                            for (int index2 = 0; index2 < serverChannelResponse.length(); index2++) {
                                String channelId = serverChannelResponse.getJSONObject(index2).getString("id");
                                String channelName = serverChannelResponse.getJSONObject(index2).getString("name");
                                String channelType = serverChannelResponse.getJSONObject(index2).getString("type");
                                String channelPrivileged = serverChannelResponse.getJSONObject(index2).getString("privileged");
                                String channelCategory = serverChannelResponse.getJSONObject(index2).getString("category");
                                JSONArray channelMembers = serverChannelResponse.getJSONObject(index2).getJSONArray("members");

                                //TODO add members
                                Channel channel = new Channel().setId(channelId).setName(channelName).setType(channelType).setPrivileged(channelPrivileged.equals("true")).setCategory(category).withMembers();
                                category.withChannels(channel);
                            }
                        } else {
                            System.err.println("Error while loading channels from server");
                        }
                    });

                    //TODO everything to Editor

                    this.server.withCategories(category);
                }
            } else {
                System.err.println("Error while loading categories from server");
            }

        });
        //System.out.println(test);
        /*
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */
        System.out.println(this.server.getCategories());
    }

    //TODO has to do something
    public void handleMessage (JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (!jsonObject.getString(COM_CHANNEL).equals("private")) {
            System.out.println("Received: " + msg.toString());
        }
        else {
            System.out.println("Not received: " + msg.toString());
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
