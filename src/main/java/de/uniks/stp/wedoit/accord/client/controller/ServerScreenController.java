package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ServerScreenController {

    private final Server server;
    private RestClient restClient;
    private LocalUser localUser;
    private Editor editor;
    private Parent view;
    private Button btnSetting;
    private Button btnHome;
    private Button btnLogout;
    private Label lbServerName;
    private ListView lvServerChannels;
    private ListView lvServerUsers;
    private TextField tfInputMessage;

    public ServerScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient, Server server){
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
        this.server = server;
    }

    public void init(){
        // Load all view references
        this.btnSetting = (Button) view.lookup("#btnSetting");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerChannels = (ListView) view.lookup("#lvServerChannels");
        this.lvServerUsers = (ListView) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnSetting.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
    }

    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen();
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    public void stop(){
        this.btnLogout.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnSetting.setOnAction(null);
    }
}
