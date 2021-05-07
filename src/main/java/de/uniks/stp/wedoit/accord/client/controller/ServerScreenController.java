package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.net.URISyntaxException;

import static de.uniks.stp.wedoit.accord.client.Constants.COM_CHANNEL;

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

    public ServerScreenController(Parent view, LocalUser model, Editor editor, RestClient restClient, Server server){
        this.view = view;
        this.localUser = model;
        this.editor = editor;
        this.restClient = restClient;
        this.server = server;
    }

    public void init(){
        // Load all view references

        this.btnOptions = (Button) view.lookup("#btnOptions");
        this.btnHome = (Button) view.lookup("#btnHome");
        this.btnLogout = (Button) view.lookup("#btnLogout");
        this.lbServerName = (Label) view.lookup("#lbServerName");
        this.lvServerChannels = (ListView) view.lookup("#lvServerChannels");
        this.lvServerUsers = (ListView) view.lookup("#lvServerUsers");
        this.tfInputMessage = (TextField) view.lookup("#tfInputMessage");
        //TODO what type
        this.listView = (ListView) view.lookup("#lvTextChat");

        // Add action listeners
        this.btnLogout.setOnAction(this::logoutButtonOnClick);
        this.btnOptions.setOnAction(this::settingsButtonOnClick);
        this.btnHome.setOnAction(this::homeButtonOnClick);
        this.tfInputMessage.setOnAction(this::tfInputMessageOnEnter);
    }

    private void tfInputMessageOnEnter(ActionEvent actionEvent){
        // get input message
        String message = this.tfInputMessage.getText();
        this.tfInputMessage.clear();

        //TODO woher richtiger channel?
        String serverId = this.server.getId();
        String dummyChannel = serverId;

        //this.server.getCategories().get(1).getChannel();

        // build message
        JsonUtil.buildServerChatMessage(dummyChannel, message);

        // send message
        //TODO muss hier jedes mal neuer WebSocket aufgemacht werden?
        try {
            //TODO was ist die URI?
            //TODO callback handle message implementieren?
            WebSocketClient wsc = new WebSocketClient(editor, new URI("wss://ac.uniks.de/ws/"), msg -> {
                JsonObject jsonObject = (JsonObject) msg;

                if (!jsonObject.getString(COM_CHANNEL).equals("private")) {
                    System.out.println("Received: " + msg.toString());
                }
                else {
                    System.out.println("Not received: " + msg.toString());
                }
            });
        } catch (URISyntaxException e) {
            System.err.println("URI wrong");
        }
    }

    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen(restClient);
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
        this.btnOptions.setOnAction(null);
    }
}
