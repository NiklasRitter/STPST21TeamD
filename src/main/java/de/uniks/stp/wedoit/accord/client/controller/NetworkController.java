package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class NetworkController {
    private final Map<String, WebSocketClient> webSocketMap = new HashMap<>();
    private final Editor editor;
    private RestClient restClient = new RestClient();

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

    public NetworkController start() {
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName(), this::handlePrivateChatMessage);
        return this;
    }

    public WebSocketClient getOrCreateWebSocket(String url) {
        WebSocketClient webSocket = webSocketMap.get(url);
        if (webSocket == null) {
            webSocket = haveWebSocket(url, (JsonStructure msg) -> {
            });
        }
        return webSocket;
    }

    /**
     * This method is for testing
     *
     * @param url             testUrl
     * @param webSocketClient testWebSocket
     * @return webSocketClient which is given
     */
    public WebSocketClient haveWebSocket(String url, WebSocketClient webSocketClient) {
        webSocketMap.put(url, webSocketClient);
        return webSocketClient;
    }

    /**
     * Create a new webSocket and put the webSocket in the WebSocketMap,
     * The webSocket has to be deleted when the websocket is no longer used
     * with method editor.withOutUrl(url)
     *
     * @param url      url for the webSocket connection
     * @param callback callback for the
     * @return webSocketClient which is givenMr Spock
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
     * @return
     */
    public NetworkController handleSystemMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        JsonObject data = jsonObject.getJsonObject(COM_DATA);

        if (jsonObject.getString(COM_ACTION).equals("userJoined")) {
            editor.haveUser(data.getString(COM_ID), data.getString(COM_NAME));

        } else if (jsonObject.getString(COM_ACTION).equals("userLeft")) {
            editor.userLeft(data.getString(COM_ID));
        }
        return this;
    }

    /**
     * handle chat message by adding it to the data model
     *
     * @param msg message from the server on the private chat channel
     * @return
     */
    public NetworkController handlePrivateChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;

        jsonObject.getString(COM_CHANNEL).equals("private");
        PrivateMessage message = new PrivateMessage();
        message.setTimestamp(jsonObject.getJsonNumber(COM_TIMESTAMP).longValue());
        message.setText(jsonObject.getString(COM_MESSAGE));
        message.setFrom(jsonObject.getString(COM_FROM));
        message.setTo(jsonObject.getString(COM_TO));

        editor.addNewPrivateMessage(message);
        return this;
    }

    public NetworkController sendPrivateChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName());
        webSocketClient.sendMessage(jsonMsgString);
        return this;
    }

    public NetworkController sendChannelChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(CHAT_USER_URL + this.editor.getLocalUser().getName()
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
                JSONObject loginAnswer = response.getBody().getObject().getJSONObject(COM_DATA);
                String userKey = loginAnswer.getString(COM_USER_KEY);
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

    public NetworkController getOnlineUsers(LocalUser localUser, WelcomeScreenController controller) {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JSONArray getServersResponse = response.getBody().getObject().getJSONArray(COM_DATA);

            for (int index = 0; index < getServersResponse.length(); index++) {
                String name = getServersResponse.getJSONObject(index).getString(COM_NAME);
                String id = getServersResponse.getJSONObject(index).getString(COM_ID);
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

    public NetworkController getChannels(LocalUser localUser, Server server, Category category, ServerScreenController controller) {
        restClient.getChannels(server.getId(), category.getId(), localUser.getUserKey(), channelsResponse -> {
            if (channelsResponse.getBody().getObject().getString("status").equals("success")) {
                JSONArray categoriesChannelResponse = channelsResponse.getBody().getObject().getJSONArray("data");

                editor.haveChannels(category, categoriesChannelResponse);

                List<Channel> channelList = server.getCategories().get(0).getChannels().stream().sorted(Comparator.comparing(Channel::getName))
                        .collect(Collectors.toList());
                controller.handleGetChannels(channelList);
            } else {
                controller.handleGetChannels(null);
            }
        });
        return this;
    }

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
