package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.view.MainScreenServerListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainScreenController implements Controller {

    private RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button welcomeButton;
    private Button optionsButton;
    private Button addServerButton;
    private Button serverButton;
    private Button logoutButton;
    private ListView<Server> serverListView;
    private PropertyChangeListener serverListListener = this::serverListViewChanged;
    private MainScreenServerListView mainScreenServerListView;

    public MainScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
    }

    public void init() {
        // Load all view references
        this.welcomeButton = (Button) view.lookup("#btnWelcome");
        this.optionsButton = (Button) view.lookup("#btnOptions");
        this.serverButton = (Button) view.lookup("#btnServerList");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.logoutButton = (Button) view.lookup("#btnLogout");
        this.serverListView = (ListView<Server>) view.lookup("#lwServerList");

        this.initTooltips();

        // load servers of the localUser
        editor.getNetworkController().getServers(localUser, this);

        // Add action listeners
        this.welcomeButton.setOnAction(this::welcomeButtonOnClick);
        this.optionsButton.setOnAction(this::optionsButtonOnClick);
        this.serverButton.setOnAction(this::serverButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);
        this.serverListView.setOnMouseReleased(this::onServerListViewClicked);
    }

    public void handleGetServers(boolean success) {
        if (success) {
            // load list view
            mainScreenServerListView = new MainScreenServerListView();
            serverListView.setCellFactory(mainScreenServerListView);
            List<Server> localUserServers = localUser.getServers().stream().sorted(Comparator.comparing(Server::getName))
                    .collect(Collectors.toList());
            this.serverListView.setItems(FXCollections.observableList(localUserServers));

            // Add listener for the loaded listView
            this.localUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_SERVERS, this.serverListListener);
        } else {
            Platform.runLater(() -> StageManager.showLoginScreen(restClient));
        }
    }

    private void initTooltips() {
        Tooltip welcomeButton = new Tooltip();
        welcomeButton.setText("private chats");
        this.welcomeButton.setTooltip(welcomeButton);

        Tooltip logoutButton = new Tooltip();
        logoutButton.setText("logout");
        this.logoutButton.setTooltip(logoutButton);

        Tooltip optionsButton = new Tooltip();
        optionsButton.setText("options");
        this.optionsButton.setTooltip(optionsButton);

        Tooltip addServerButton = new Tooltip();
        addServerButton.setText("create new server");
        this.addServerButton.setTooltip(addServerButton);

        Tooltip serverButton = new Tooltip();
        serverButton.setText("Enter selected server");
        this.serverButton.setTooltip(serverButton);
    }

    public void stop() {
        welcomeButton.setOnAction(null);
        optionsButton.setOnAction(null);
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
        StageManager.showWelcomeScreen(restClient);
    }

    /**
     * Redirect to the OptionsScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void optionsButtonOnClick(ActionEvent actionEvent) {
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
            StageManager.showServerScreen(server, restClient);
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
                StageManager.showServerScreen(server, restClient);
            }
        }

    }

    /**
     * Opens a pop-up windows, where you can enter the servername
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        StageManager.showCreateServerScreen(restClient);
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
     * update automatically the listView when localUser.getServers changed
     *
     * @param propertyChangeEvent event which changed the Listener for the servers of the local user
     */
    private void serverListViewChanged(PropertyChangeEvent propertyChangeEvent) {

        if (propertyChangeEvent.getNewValue() != null) {
            serverListView.getItems().removeAll();
            List<Server> localUserServers = localUser.getServers().stream().sorted(Comparator.comparing(Server::getName))
                    .collect(Collectors.toList());
            Platform.runLater(() -> this.serverListView.setItems(FXCollections.observableList(localUserServers)));
            serverListView.refresh();
        }
    }
}
