package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class MainScreenController {

    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button welcomeButton;
    private Button settingsButton;
    private Button addServerButton;
    private Button serverButton;
    private Button logoutButton;
    private ListView<Server> serverListView;

    public MainScreenController(Parent view, LocalUser model, Editor editor) {
        this.view = view;
        this.localUser = model;
        this.editor = editor;
    }

    public void init() {
        // Load all view references
        this.welcomeButton = (Button) view.lookup("#btnWelcome");
        this.settingsButton = (Button) view.lookup("#btnSetting");
        this.serverButton = (Button) view.lookup("#btnServerList");
        this.addServerButton = (Button) view.lookup("#btnAddServer");
        this.logoutButton = (Button) view.lookup("#btnLogout");
        this.serverListView = (ListView<Server>) view.lookup("#lwServerList");

        // TODO load list view

        // Add action listeners
        this.welcomeButton.setOnAction(this::welcomeButtonOnClick);
        this.settingsButton.setOnAction(this::settingsButtonOnClick);
        this.serverButton.setOnAction(this::serverButtonOnClick);
        this.addServerButton.setOnAction(this::addServerButtonOnClick);
        this.logoutButton.setOnAction(this::logoutButtonOnClick);
        this.serverListView.setOnMouseReleased(this::onServerListViewClicked);

    }

    public void stop() {
        welcomeButton.setOnAction(null);
        settingsButton.setOnAction(null);
        serverButton.setOnAction(null);
        addServerButton.setOnAction(null);
        logoutButton.setOnAction(null);
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
        //TODO
    }

    /**
     * Redirect to the ServerScreen of the chosen server
     *
     * @param mouseEvent Expects an mouse event, such as when the listView is doubleclicked
     */
    private void onServerListViewClicked(MouseEvent mouseEvent) {
        //TODO
    }

    /**
     * Opens a pop-up windows, where you can enter the servername
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void addServerButtonOnClick(ActionEvent actionEvent) {
        //TODO
        StageManager.showCreateServerScreen();
    }

    /**
     * The localUser will be logged out and redirect to the LoginScreen
     *
     * @param actionEvent Expects an action event, such as when a javafx.scene.control.Button has been fired
     */
    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

}
