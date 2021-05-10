package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.json.JSONArray;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

        try {
            this.webSocket = new WebSocketClient(editor, new URI(CHAT_USER_URL + this.localUser.getName()
                    + "&" +  SERVER_ID_URL + this.server.getId()), this::handleMessage);
        } catch (URISyntaxException e) {
            System.err.println("Error creating URI");
            e.printStackTrace();
        }

        this.loadServerData();
    }

    public void stop(){
        this.btnLogout.setOnAction(null);
        this.btnHome.setOnAction(null);
        this.btnOptions.setOnAction(null);

        this.webSocket.stop();
    }

    private void tfInputMessageOnEnter(ActionEvent actionEvent){
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

    private void homeButtonOnClick(ActionEvent actionEvent) {
        StageManager.showMainScreen(restClient);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showOptionsScreen();
    }

    private void logoutButtonOnClick(ActionEvent actionEvent) {
        //TODO
    }

    public void handleMessage (JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        if (!jsonObject.getString(COM_CHANNEL).equals("private")) {
            System.out.println("Received: " + msg.toString());
        }
        else {
            System.out.println("Not received: " + msg.toString());
        }
    }

}
