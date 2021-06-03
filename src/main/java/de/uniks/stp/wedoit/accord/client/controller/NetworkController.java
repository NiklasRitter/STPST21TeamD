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
    private String cleanLocalUserName;

    /**
     * Create a NetworkController.
     *
     * @param editor The editor of the Application
     */
    public NetworkController(Editor editor) {
        this.editor = editor;
    }

    public String getCleanLocalUserName() {
        return cleanLocalUserName;
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
        setClearUsername();
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + cleanLocalUserName, this::handlePrivateChatMessage);
        return this;
    }

    public String setClearUsername() {
        String newName;
        try {
            newName = URLEncoder.encode(this.editor.getLocalUser().getName(), StandardCharsets.UTF_8.toString());
            cleanLocalUserName =  newName;
        } catch (UnsupportedEncodingException e) {
            cleanLocalUserName = this.editor.getLocalUser().getName();
        }
        return cleanLocalUserName;
    }

    /**
     * @param url url of the web socket
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

        if (jsonObject.getString(ACTION).equals(USER_JOINED)) {
            editor.haveUser(data.getString(ID), data.getString(NAME));

        } else if (jsonObject.getString(ACTION).equals(USER_LEFT)) {
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
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + cleanLocalUserName);
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
                getOrCreateWebSocket(CHAT_USER_URL + cleanLocalUserName
                        + AND_SERVER_ID_URL + this.editor.getCurrentServer().getId());
        webSocketClient.sendMessage(jsonMsgString);
        return this;
    }

    public NetworkController createServer(String serverNameInput, CreateServerScreenController controller) {
        restClient.createServer(serverNameInput, editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONObject createServerAnswer = response.getBody().getObject().getJSONObject(DATA);
                String serverId = createServerAnswer.getString(ID);
                String serverName = createServerAnswer.getString(NAME);

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
            if (!response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
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
            controller.handleRegister(registerResponse.getBody().getObject().getString(STATUS).equals(SUCCESS));
        });
        return this;
    }

    public NetworkController getServers(LocalUser localUser, MainScreenController controller) {
        restClient.getServers(localUser.getUserKey(), response -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONArray getServersResponse = response.getBody().getObject().getJSONArray(DATA);

                for (int index = 0; index < getServersResponse.length(); index++) {
                    String name = getServersResponse.getJSONObject(index).getString(NAME);
                    String id = getServersResponse.getJSONObject(index).getString(ID);
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
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONObject data = response.getBody().getObject().getJSONObject(DATA);
                JSONArray members = data.getJSONArray(MEMBERS);
                server.setOwner(data.getString(OWNER));

                controller.handleGetExplicitServerInformation(members);
            } else {
                controller.handleGetExplicitServerInformation(null);
            }
        });
        return this;
    }

    public NetworkController changeServerName(LocalUser localUser, Server server, String newServerName, EditServerScreenController controller) {
        restClient.changeServerName(server.getId(), newServerName, localUser.getUserKey(), response -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                server.setName(newServerName);
                controller.handleChangeServerName(true);
            } else {
                controller.handleChangeServerName(false);
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

    public NetworkController getLocalUserId(LocalUser localUser) {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray(DATA);

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString(NAME);
                String id = getServersResponse.getJSONObject(index).getString(ID);
                if (name.equals(localUser.getName())) {
                    localUser.setId(id);
                    return;
                }
            }

        });
        return this;
    }

    public NetworkController logoutUser(String userKey) {
        restClient.logout(userKey, response -> {
            editor.handleLogoutUser(response.getBody().getObject().getString(STATUS).equals(SUCCESS));
        });
        return this;
    }

    public NetworkController getCategories(LocalUser localUser, Server server, ServerScreenController controller) {
        restClient.getCategories(server.getId(), localUser.getUserKey(), categoryResponse -> {
            if (categoryResponse.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONArray serversCategoryResponse = categoryResponse.getBody().getObject().getJSONArray(DATA);

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
            if (channelsResponse.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONArray categoriesChannelResponse = channelsResponse.getBody().getObject().getJSONArray(DATA);

                editor.haveChannels(category, categoriesChannelResponse);

                List<Channel> channelList = category.getChannels().stream().sorted(Comparator.comparing(Channel::getName))
                        .collect(Collectors.toList());
                controller.handleGetChannels(channelList, categoryItem);
            } else {
                controller.handleGetChannels(null, categoryItem);
            }
        });
        return this;
    }

    public NetworkController createCategory(Server server, String categoryNameInput, CreateCategoryScreenController controller) {
        restClient.createCategory(server.getId(), categoryNameInput, editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JSONObject createCategoryAnswer = response.getBody().getObject().getJSONObject(DATA);
                String categoryId = createCategoryAnswer.getString(ID);
                String categoryName = createCategoryAnswer.getString(NAME);

                Category category = editor.haveCategory(categoryId, categoryName, server);
                controller.handleCreateCategory(category);
            } else {
                controller.handleCreateCategory(null);
            }
        });
        return this;
    }

    /**
     * Should be called if a server, category or channel will be deleted.
     * It automatically chooses the correct delete method
     */
    public void deleteObject(LocalUser localUser, Object objectToDelete, AttentionScreenController controller) {
        if (objectToDelete.getClass().equals(Server.class)) {
            deleteServer(localUser, (Server) objectToDelete, controller);
        } // else if is for other objects like channel or category
    }

    private void deleteServer(LocalUser localUser, Server server, AttentionScreenController controller) {
        restClient.deleteServer(localUser.getUserKey(), server.getId(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                controller.handleDeleteServer(true);
            } else {
                controller.handleDeleteServer(false);
            }
        });
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
