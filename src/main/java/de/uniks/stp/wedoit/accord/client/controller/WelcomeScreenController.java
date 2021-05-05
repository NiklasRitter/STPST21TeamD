package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Message;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.view.WelcomeScreenOnlineUsersListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.json.JSONArray;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.SYSTEM_SOCKET_URL;

public class WelcomeScreenController {

    private Parent view;
    private LocalUser localUser;
    private Editor editor;
    private Button btnOptions;
    private Button btnHome;
    private Button btnLogout;

    private RestClient restClient;
    private ListView<User> lwOnlineUsers;
    private TextField tfPrivateChat;
    private ListView<Message> lwPrivateChat;
    private WelcomeScreenOnlineUsersListView usersListView;
    private PropertyChangeListener usersListListener = this::usersListViewChanged;
    private WebSocketClient websocket;

    public WelcomeScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    public void init() {

        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lwOnlineUsers = (ListView<User>) view.lookup("#lwOnlineUsers");
        this.tfPrivateChat = (TextField) view.lookup("#tfEnterPrivateChat");

        this.lwPrivateChat = (ListView<Message>) view.lookup("#lwPrivateChat");


        this.btnHome.setOnAction(this::btnHomeOnClicked);
        this.btnLogout.setOnAction(this::btnLogoutOnClicked);
        this.btnOptions.setOnAction(this::btnOptionsOnClicked);

        this.initOnlineUsersList();

        try {
            this.websocket = new WebSocketClient(editor, new URI(SYSTEM_SOCKET_URL), this::handleMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error while making new URI");
            e.printStackTrace();
        }
    }

    public void stop() {
        this.btnHome.setOnAction(null);
        this.btnLogout.setOnAction(null);
        this.btnOptions.setOnAction(null);

        this.btnOptions = null;
        this.btnHome = null;
        this.btnLogout = null;
    }

    /**
     * redirect to Main Screen
     *
     * @param actionEvent
     */
    private void btnHomeOnClicked(ActionEvent actionEvent) {
        StageManager.showMainScreen();
    }

    /**
     * logout current LocalUser and redirect to the LoginScreen
     *
     * @param actionEvent
     */
    private void btnLogoutOnClicked(ActionEvent actionEvent) {
        String userKey = this.localUser.getUserKey();

        if (userKey != null && !userKey.isEmpty()) {
            restClient.logout(userKey, response -> {
                //if response status is successful
                if (response.getBody().getObject().getString("status").equals("success")) {
                    this.localUser.setUserKey(null);
                    Platform.runLater(() -> StageManager.showLoginScreen(restClient));
                } else {
                    System.err.println("Error while logging out");
                }
            });
        }
    }

    /**
     * redirect to Options Menu
     *
     * @param actionEvent
     */
    private void btnOptionsOnClicked(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    private void initOnlineUsersList(){
        //TODO negativen Fall abprüfen
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray("data");

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString("name");
                String id = getServersResponse.getJSONObject(index).getString("id");
                editor.haveUser(id, name);
            }

            // load list view
            usersListView = new WelcomeScreenOnlineUsersListView();
            lwOnlineUsers.setCellFactory(usersListView);
            List<User> availableUser = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                    .collect(Collectors.toList());

            this.lwOnlineUsers.setItems(FXCollections.observableList(availableUser));

            // Add listener for the loaded listView
            this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_USERS, this.usersListListener);
        });
    }

    private void usersListViewChanged(PropertyChangeEvent propertyChangeEvent) {

        if (propertyChangeEvent.getNewValue() != null) {
            lwOnlineUsers.getItems().removeAll();
            List<User> availableUser = localUser.getUsers().stream().sorted(Comparator.comparing(User::getName))
                    .collect(Collectors.toList());
            this.lwOnlineUsers.setItems(FXCollections.observableList(availableUser));
            lwOnlineUsers.refresh();
        }
    }

    public void handleMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (jsonObject.getString("action").equals("userJoined")) {
            System.out.println(jsonObject.toString());
        }
        else if(jsonObject.getString("action").equals("userLeft")) {
            System.out.println(jsonObject.toString());
        }

    }
}
