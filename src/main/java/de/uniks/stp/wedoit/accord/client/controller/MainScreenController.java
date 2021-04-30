package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.view.MainScreenServerListView;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainScreenController {

    private final RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button welcomeButton;
    private Button settingsButton;
    private Button addServerButton;
    private Button serverButton;
    private Button logoutButton;
    private ListView<Server> serverListView;
    private PropertyChangeListener serverListListener = this::serverListViewChanged;

    public MainScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    public void init() {
        // Load all view references
        this.welcomeButton = (Button) view.lookup("#btnWelcome");
        this.settingsButton = (Button) view.lookup("#btnSetting");
        this.serverButton = (Button) view.lookup("#btnServerList");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.logoutButton = (Button) view.lookup("#btnLogout");
        this.serverListView = (ListView<Server>) view.lookup("#lwServerList");

        // load server of the localUser
        restClient.getServers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray("data");

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString("name");
                String id = getServersResponse.getJSONObject(index).getString("id");
                editor.haveServer(localUser, id, name);
            }

            //load list view
            serverListView.setCellFactory(new MainScreenServerListView());
            List<Server> localUserServers = localUser.getServers().stream().sorted(Comparator.comparing(Server::getName))
                    .collect(Collectors.toList());
            this.serverListView.setItems(FXCollections.observableList(localUserServers));


        });


        // Add action listeners
        this.welcomeButton.setOnAction(this::welcomeButtonOnClick);
        this.settingsButton.setOnAction(this::settingsButtonOnClick);
        this.serverButton.setOnAction(this::serverButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);
        this.serverListView.setOnMouseReleased(this::onServerListViewClicked);

        this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);

    }

    public void stop() {
        welcomeButton.setOnAction(null);
        settingsButton.setOnAction(null);
        serverButton.setOnAction(null);
        addServerButton.setOnAction(null);
        logoutButton.setOnAction(null);

        this.localUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);

    }

    // Additional methods

    /**
     * Redirect to the WelcomeScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void welcomeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showWelcomeScreen();
    }

    /**
     * Redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    /**
     * Redirect to the ServerScreen of the chosen server
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void serverButtonOnClick(ActionEvent actionEvent) {
        Server server = serverListView.getSelectionModel().getSelectedItem();
        if (server != null) {
            StageManager.showServerScreen(server);
        }
    }

    /**
     * Redirect to the ServerScreen of the chosen server
     *
     * @param mouseEvent Expects an mouse event, such as when the listView is doubleclicked
     */
    private void onServerListViewClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Server server = serverListView.getSelectionModel().getSelectedItem();
            if (server != null) {
                StageManager.showServerScreen(server);
            }
        }

    }

    /**
     * Create a new server and redirect to this one
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    /**
     * update the listView
     *
     * @param propertyChangeEvent
     */
    private void serverListViewChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Server) {
            serverListView.getItems().add((Server) propertyChangeEvent.getNewValue());
        }
        serverListView.refresh();
    }
}
