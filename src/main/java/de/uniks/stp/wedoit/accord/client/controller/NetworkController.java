package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.scene.control.TreeItem;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class NetworkController {

    private final Map<String, WebSocketClient> webSocketMap = new HashMap<>();
    private final Editor editor;
    private RestClient restClient = new RestClient();

    /**
     * Create a NetworkController.
     *
     * @param editor The editor of the Application
     */
    public NetworkController(Editor editor) {
        this.editor = editor;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public NetworkController setRestClient(RestClient restClient) {
        this.restClient = restClient;
        return this;
    }

    /**
     * Called to start this controller.
     * Only call after corresponding fxml is loaded.
     * <p>
     * Create default WebSocketClients.
     */
    public NetworkController start() {
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + clearUsername(), this::handlePrivateChatMessage);
        return this;
    }

    public String clearUsername() {
        String newName;
        try {
            newName = URLEncoder.encode(this.editor.getLocalUser().getName(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return this.editor.getLocalUser().getName();
        }
        return newName;
    }

    /**
     * @param url
     * @return -
     */
    public WebSocketClient getOrCreateWebSocket(String url) {
        WebSocketClient webSocket = webSocketMap.get(url);
        if (webSocket == null) {
            webSocket = haveWebSocket(url, (JsonStructure msg) -> {
            });
        }
        return webSocket;
    }

    /**
     * Add a new WebSocketClient to the Controller
     * Override any previous WebSocketClients
     *
     * @param url             The URL of the webSocket
     * @param webSocketClient The WebSocketClient to be added
     * @return The given WebSocketClient
     */
    public WebSocketClient haveWebSocket(String url, WebSocketClient webSocketClient) {
        webSocketMap.put(url, webSocketClient);
        return webSocketClient;
    }

    /**
     * Create a new webSocket and add it
     * Override the Callback of any WebSocketClient for given URL
     *
     * @param url      The URL of the webSocket
     * @param callback The Callback for the URL
     * @return The created WebSocketClient
     */
    public WebSocketClient haveWebSocket(String url, WSCallback callback) {
        WebSocketClient webSocket = webSocketMap.get(url);
        if (webSocket != null) {
            webSocket.setCallback(callback);
        } else {
            webSocket = new WebSocketClient(editor, URI.create(url), callback);
        }
        webSocketMap.put(url, webSocket);
        return webSocket;
    }


    /**
     * remove a webSocket with given url
     *
     * @param url url of a webSocket
     * @return the webSocket which is removed or null if there was no mapping of this url
     */
    public WebSocketClient withOutWebSocket(String url) {
        WebSocketClient webSocketClient = webSocketMap.remove(url);
        if (webSocketClient != null) {
            webSocketClient.stop();
        }
        return webSocketClient;
    }


    /**
     * handle messages on the system channel by adding or deleting users from the data model
     *
     * @param msg message from the server on the system channel
     * @return NetworkController
     */
    public NetworkController handleSystemMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        JsonObject data = jsonObject.getJsonObject(DATA);

        if (jsonObject.getString(ACTION).equals("userJoined")) {
            editor.haveUser(data.getString(ID), data.getString(NAME));

        } else if (jsonObject.getString(ACTION).equals("userLeft")) {
            editor.userLeft(data.getString(ID));
        }
        return this;
    }

    /**
     * handle chat message by adding it to the data model
     *
     * @param msg message from the server on the private chat channel
     * @return NetworkController
     */
    public NetworkController handlePrivateChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        PrivateMessage message = new PrivateMessage();
        message.setTimestamp(jsonObject.getJsonNumber(TIMESTAMP).longValue());
        message.setText(jsonObject.getString(MESSAGE));
        message.setFrom(jsonObject.getString(FROM));
        message.setTo(jsonObject.getString(TO));

        editor.addNewPrivateMessage(message);
        return this;
    }

    /**
     * Send a private chat message.
     *
     * @param jsonMsgString The stringified Json message
     */
    public NetworkController sendPrivateChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + clearUsername());
        webSocketClient.sendMessage(jsonMsgString);
        return this;
    }

    /**
     * Send a message in the current Server Channel
     *
     * @param jsonMsgString The stringified Json message
     */
    public NetworkController sendChannelChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(CHAT_USER_URL + clearUsername()
                        + AND_SERVER_ID_URL + this.editor.getCurrentServer().getId());
        webSocketClient.sendMessage(jsonMsgString);
        return this;
    }

    public NetworkController createServer(String serverNameInput, CreateServerScreenController controller) {
        restClient.createServer(serverNameInput, editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString("status").equals("success")) {
                JSONObject createServerAnswer = response.getBody().getObject().getJSONObject("data");
                String serverId = createServerAnswer.getString("id");
                String serverName = createServerAnswer.getString("name");

                Server server = editor.haveServer(editor.getLocalUser(), serverId, serverName);
                controller.handleCreateServer(server);
            } else {
                controller.handleCreateServer(null);
            }
        });
        return this;
    }

    public NetworkController loginUser(String username, String password, LoginScreenController controller) {
        restClient.login(username, password, (response) -> {
            if (!response.getBody().getObject().getString("status").equals("success")) {
                controller.handleLogin(false);
            } else {
                JSONObject loginAnswer = response.getBody().getObject().getJSONObject(DATA);
                String userKey = loginAnswer.getString(USER_KEY);
                editor.haveLocalUser(username, userKey);
                start();
                controller.handleLogin(true);
            }
        });
        return this;
    }

    public NetworkController registerUser(String username, String password, LoginScreenController controller) {
        restClient.register(username, password, registerResponse -> {
            controller.handleRegister(registerResponse.getBody().getObject().getString("status").equals("success"));
        });
        return this;
    }

    public NetworkController getServers(LocalUser localUser, MainScreenController controller) {
        restClient.getServers(localUser.getUserKey(), response -> {
            if (response.getBody().getObject().getString("status").equals("success")) {
                JSONArray getServersResponse = response.getBody().getObject().getJSONArray("data");

                for (int index = 0; index < getServersResponse.length(); index++) {
                    String name = getServersResponse.getJSONObject(index).getString("name");
                    String id = getServersResponse.getJSONObject(index).getString("id");
                    editor.haveServer(localUser, id, name);
                }
                controller.handleGetServers(true);
            } else {
                controller.handleGetServers(false);
            }
        });
        return this;
    }

    public NetworkController getExplicitServerInformation(LocalUser localUser, Server server, ServerScreenController controller) {
        // get members of this server
        restClient.getExplicitServerInformation(localUser.getUserKey(), server.getId(), response -> {
            if (response.getBody().getObject().getString("status").equals("success")) {
                JSONObject data = response.getBody().getObject().getJSONObject("data");
                JSONArray members = data.getJSONArray("members");
                server.setOwner(data.getString("owner"));

                controller.handleGetExplicitServerInformation(members);
            } else {
                controller.handleGetExplicitServerInformation(null);
            }
        });
        return this;
    }

    public NetworkController getOnlineUsers(LocalUser localUser, PrivateChatsScreenController controller) {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray(DATA);

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString(NAME);
                String id = getServersResponse.getJSONObject(index).getString(ID);
                editor.haveUser(id, name);
            }
            controller.handleGetOnlineUsers();
        });
        return this;
    }

    public NetworkController logoutUser(String userKey) {
        restClient.logout(userKey, response -> {
            editor.handleLogoutUser(response.getBody().getObject().getString("status").equals("success"));
        });
        return this;
    }

    public NetworkController getCategories(LocalUser localUser, Server server, ServerScreenController controller) {
        restClient.getCategories(server.getId(), localUser.getUserKey(), categoryResponse -> {
            if (categoryResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray serversCategoryResponse = categoryResponse.getBody().getObject().getJSONArray("data");

                editor.haveCategories(server, serversCategoryResponse);

                List<Category> categoryList = server.getCategories();
                controller.handleGetCategories(categoryList);
            } else {
                controller.handleGetCategories(null);
            }
        });
        return this;
    }

    public NetworkController getChannels(LocalUser localUser, Server server, Category category, TreeItem<Object> categoryItem, ServerScreenController controller) {
        restClient.getChannels(server.getId(), category.getId(), localUser.getUserKey(), channelsResponse -> {
            if (channelsResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray categoriesChannelResponse = channelsResponse.getBody().getObject().getJSONArray("data");

                editor.haveChannels(category, categoriesChannelResponse);

                List<Channel> channelList = server.getCategories().get(0).getChannels().stream().sorted(Comparator.comparing(Channel::getName))
                        .collect(Collectors.toList());
                controller.handleGetChannels(channelList, categoryItem);
            } else {
                controller.handleGetChannels(null, categoryItem);
            }
        });
        return this;
    }

    /**
     * Called to stop this controller
     * <p>
     * Stop and remove WebSocketClients
     */
    public NetworkController stop() {
        Iterator<Map.Entry<String, WebSocketClient>> iterator = webSocketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WebSocketClient> entry = iterator.next();
            iterator.remove();
            entry.getValue().stop();
        }
        return this;
    }
}
