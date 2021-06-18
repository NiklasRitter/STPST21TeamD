package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.StageManager;
import de.uniks.stp.wedoit.accord.client.constants.JSON;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;

import javax.json.*;
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
            cleanLocalUserName = newName;
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
                JsonObject createServerAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);
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
                JsonObject loginAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);
                String userKey = loginAnswer.getString(USER_KEY);
                LocalUser localUser = editor.haveLocalUser(username, userKey);
                localUser.setPassword(password);
                start();
                controller.handleLogin(true);
            }
        });
        return this;
    }

    public NetworkController automaticLoginUser(String username, String password, Editor editor) {
        restClient.login(username, password, (response) -> {
            if (!response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                editor.handleAutomaticLogin(false);
            } else {
                JsonObject loginAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);
                String userKey = loginAnswer.getString(USER_KEY);
                LocalUser localUser = editor.haveLocalUser(username, userKey);
                localUser.setPassword(password);
                start();
                editor.handleAutomaticLogin(true);
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
                JsonArray getServersResponse = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonArray(DATA);

                for (int index = 0; index < getServersResponse.toArray().length; index++) {
                    String name = getServersResponse.getJsonObject(index).getString(NAME);
                    String id = getServersResponse.getJsonObject(index).getString(ID);
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
                JsonObject data = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);
                JsonArray members = data.getJsonArray(MEMBERS);
                server.setOwner(data.getString(OWNER));
                server.setName(data.getString(NAME));
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
            JsonArray getServersResponse = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonArray(DATA);

            for (int index = 0; index < getServersResponse.toArray().length; index++) {
                String name = getServersResponse.getJsonObject(index).getString(NAME);
                String id = getServersResponse.getJsonObject(index).getString(ID);
                editor.haveUser(id, name);
            }
            controller.handleGetOnlineUsers();
        });
        return this;
    }

    public NetworkController getLocalUserId(LocalUser localUser) {
        // load online Users
        restClient.getOnlineUsers(localUser.getUserKey(), response -> {
            JsonArray getServersResponse = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonArray(DATA);

            for (int index = 0; index < getServersResponse.toArray().length; index++) {
                String name = getServersResponse.getJsonObject(index).getString(NAME);
                String id = getServersResponse.getJsonObject(index).getString(ID);
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
                JsonArray serversCategoryResponse = JsonUtil.parse(String.valueOf(categoryResponse.getBody().getObject())).getJsonArray(DATA);

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
                JsonArray categoriesChannelResponse = JsonUtil.parse(String.valueOf(channelsResponse.getBody().getObject())).getJsonArray(DATA);

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
                JsonObject createCategoryAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);
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

    public NetworkController createChannel(Server server, Category category, String channelNameInput, String type, boolean privileged, List<String> members, CreateChannelScreenController controller) {
        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        if (members != null) {
            for (String userId : members) {
                memberJson.add(Json.createValue(userId));
            }
        }
        restClient.createChannel(server.getId(), category.getId(), channelNameInput, type, privileged, memberJson.build(), editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JsonObject createChannelAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);

                String channelId = createChannelAnswer.getString(ID);
                String channelName = createChannelAnswer.getString(NAME);
                String channelType = createChannelAnswer.getString(TYPE);
                boolean channelPrivileged = createChannelAnswer.getBoolean(PRIVILEGED);
                String channelCategoryId = createChannelAnswer.getString(CATEGORY);
                JsonArray channelMembers = createChannelAnswer.getJsonArray(MEMBERS);

                if (category.getId().equals(channelCategoryId)) {
                    Channel channel = editor.haveChannel(channelId, channelName, channelType, channelPrivileged, category, channelMembers);
                    controller.handleCreateChannel(channel);
                } else {
                    controller.handleCreateChannel(null);
                }
            } else {
                controller.handleCreateChannel(null);
            }
        });
        return this;
    }

    public NetworkController updateChannel(Server server, Category category, Channel channel, String channelNameInput, boolean privileged, List<String> members, EditChannelScreenController controller) {
        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        if (members != null) {
            for (String userId : members) {
                memberJson.add(Json.createValue(userId));
            }
        }
        restClient.updateChannel(server.getId(), category.getId(), channel.getId(), channelNameInput, privileged, memberJson.build(), editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JsonObject createChannelAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);

                String channelId = createChannelAnswer.getString(ID);
                String channelName = createChannelAnswer.getString(NAME);
                String channelType = createChannelAnswer.getString(TYPE);
                boolean channelPrivileged = createChannelAnswer.getBoolean(PRIVILEGED);
                String channelCategoryId = createChannelAnswer.getString(CATEGORY);
                JsonArray channelMembers = createChannelAnswer.getJsonArray(MEMBERS);

                if (category.getId().equals(channelCategoryId)) {
                    Channel newChannel = editor.updateChannel(server, channelId, channelName, channelType, channelPrivileged, channelCategoryId, channelMembers);
                    controller.handleEditChannel(newChannel);
                } else {
                    controller.handleEditChannel(null);
                }
            } else {
                controller.handleEditChannel(null);
            }
        });
        return this;
    }

    public NetworkController updateCategory(Server server, Category category, String categoryNameInput, EditCategoryScreenController controller) {
        restClient.updateCategory(server.getId(), category.getId(), categoryNameInput, editor.getLocalUser().getUserKey(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JsonObject createCategoryIdAnswer = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonObject(DATA);

                String categoryId = createCategoryIdAnswer.getString(ID);
                String categoryName = createCategoryIdAnswer.getString(NAME);
                String serverId = createCategoryIdAnswer.getString(SERVER);
                JsonArray channels = createCategoryIdAnswer.getJsonArray(JSON.CHANNELS);

                if (category.getId().equals(categoryId)) {
                    Category newCategory = editor.haveCategory(categoryId, categoryName, server);
                    controller.handleEditCategory(newCategory);
                } else {
                    controller.handleEditCategory(null);
                }
            } else {
                controller.handleEditCategory(null);
            }
        });
        return this;
    }

    /**
     * This method does a rest request to create a new invitation link
     *
     * @param type       type of the invitation, means temporal or count with a int max
     * @param max        maximum size of users who can use this link, is the type temporal max is ignored
     * @param server     server
     * @param userKey    userKey of the logged in local user
     * @param controller controller which handles the new link
     */
    public void createInvitation(String type, int max, Server server, String userKey, EditServerScreenController controller) {
        String serverId = server.getId();
        if (type.equals(TEMPORAL)) {
            restClient.createInvite(serverId, userKey, invitationResponse -> {
                if (invitationResponse.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                    JsonObject response = JsonUtil.parse(String.valueOf(invitationResponse.getBody().getObject())).getJsonObject(DATA);
                    Invitation invitation = JsonUtil.parseInvitation(response, server);
                    controller.handleInvitation(invitation.getLink());
                } else {
                    controller.handleInvitation(null);
                }
            });

        } else if (type.equals(COUNT)) {
            restClient.createInvite(max, serverId, userKey, invitationResponse -> {
                if (invitationResponse.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                    JsonObject response = JsonUtil.parse(String.valueOf(invitationResponse.getBody().getObject())).getJsonObject(DATA);
                    Invitation invitation = JsonUtil.parseInvitation(response, server);
                    controller.handleInvitation(invitation.getLink());
                } else {
                    controller.handleInvitation(null);
                }
            });
        }
    }

    /**
     * Try to join a server with the Restclient::joinServer method
     *
     * @param localUser      localUser who is logged in
     * @param invitationLink invitation which is used to join a server
     * @param controller     controller in which the response is handled
     */
    public void joinServer(LocalUser localUser, String invitationLink, JoinServerScreenController controller) {
        restClient.joinServer(localUser, invitationLink, invitationResponse -> {
            if (!invitationResponse.isSuccess()) {
                if (invitationResponse.getBody() != null) {
                    controller.handleInvitation(null, invitationResponse.getBody().getObject().getString(MESSAGE));
                } else {
                    controller.handleInvitation(null, "No valid invitation link");
                }
            } else {
                if (invitationResponse.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                    String[] splitLink = invitationLink.split("/");
                    String id = null;
                    if (splitLink.length > 5) {
                        id = splitLink[5];
                    }
                    if (id != null) {
                        Server server = editor.haveServer(localUser, id, "");
                        controller.handleInvitation(server, invitationResponse.getBody().getObject().getString(MESSAGE));
                    } else
                        controller.handleInvitation(null, "MainScreen");
                } else {
                    controller.handleInvitation(null, invitationResponse.getBody().getObject().getString(MESSAGE));
                }
            }
        });
    }


    /**
     * Should be called if a server, category or channel will be deleted.
     * It automatically chooses the correct delete method
     */
    public void deleteObject(LocalUser localUser, Object objectToDelete, AttentionScreenController controller) {
        if (objectToDelete.getClass().equals(Server.class)) {
            deleteServer(localUser, (Server) objectToDelete, controller);
        }
        else if(objectToDelete.getClass().equals(Channel.class)){
            deleteChannel(localUser, (Channel) objectToDelete, controller);
        }
        else if(objectToDelete.getClass().equals(Category.class)){
            deleteCategory(localUser, (Category) objectToDelete, controller);
        }
    }

    private void deleteServer(LocalUser localUser, Server server, AttentionScreenController controller) {
        restClient.deleteServer(localUser.getUserKey(), server.getId(), (response) -> {
            controller.handleDeleteServer(response.getBody().getObject().getString(STATUS).equals(SUCCESS));
        });
    }

    private void deleteChannel(LocalUser localUser, Channel channel, AttentionScreenController controller) {
        restClient.deleteChannel(localUser.getUserKey(), channel.getId(), channel.getCategory().getId(), channel.getCategory().getServer().getId(), (response) -> {
            controller.handleDeleteChannel(response.getBody().getObject().getString(STATUS).equals(SUCCESS));
        });
    }

    /**
     * delivers last 50 messages from the channel after the timestamp
     *
     * @param localUser     localUser who is logged in
     * @param server        server of the channel
     * @param category      category of the channel
     * @param channel       channel of which the messages should be delivered
     * @param timestamp     timestamp from where the last 50 messages should be delivered
     * @param controller    controller in which the response is handled
     */
    public void getChannelMessages(LocalUser localUser, Server server, Category category, Channel channel, String timestamp, ServerScreenController controller) {
        restClient.getChannelMessages(localUser.getUserKey(), server.getId(), category.getId(), channel.getId(), timestamp, (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                JsonArray data = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonArray(DATA);
                controller.handleGetChannelMessages(channel, data);
            } else if (response.getBody().getObject().getString(STATUS).equals(FAILURE)) {
                controller.handleGetChannelMessages(null, null);
            }
        });
    }

    private void deleteCategory(LocalUser localUser, Category category, AttentionScreenController controller) {
        restClient.deleteCategory(localUser.getUserKey(), category.getId(), category.getServer().getId(), (response) -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                controller.handleDeleteCategory(true);
            } else {
                controller.handleDeleteCategory(false);
            }
        });
    }

    public void loadInvitations(Server server, String userKey, EditServerScreenController controller) {

        restClient.loadInvitations(server.getId(), userKey, response -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {

                JsonArray invitationResponse = JsonUtil.parse(String.valueOf(response.getBody().getObject())).getJsonArray(DATA);

                List<Invitation> allInvitations = JsonUtil.parseInvitations(invitationResponse, server);
                server.withoutInvitations(new ArrayList<>(server.getInvitations()));
                server.withInvitations(allInvitations);

                controller.handleOldInvitations(server.getInvitations());
            } else {
                controller.handleOldInvitations(null);
            }
        });

    }

    public void deleteInvite(String userKey, Invitation invitation, Server server, EditServerScreenController controller) {
        restClient.deleteInvitation(userKey, invitation.getId(), server.getId(), response -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {

                editor.deleteInvite(invitation.getId(), server);
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

    public NetworkController leaveServer(String userKey, String serverId) {
        restClient.leaveServer(userKey, serverId, response -> {
            if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                Platform.runLater(this.editor.getStageManager()::showMainScreen);
            } else {
                System.err.println("Error while leaving server");
                Platform.runLater(this.editor.getStageManager()::showMainScreen);
            }
        });
        return this;
    }


}
