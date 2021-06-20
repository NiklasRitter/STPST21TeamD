package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;

public class WebSocketManager {

    private final Editor editor;
    private String cleanLocalUserName;
    private final Map<String, WebSocketClient> webSocketMap = new HashMap<>();

    /**
     * Create a WebSocketManager.
     *
     * @param editor The editor of the Application
     */
    public WebSocketManager(Editor editor) {
        this.editor = editor;
    }

    /**
     * Called to start this controller.
     * Only call after corresponding fxml is loaded.
     * <p>
     * Create default WebSocketClients.
     */
    public WebSocketManager start() {
        setClearUsername();
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + cleanLocalUserName, this::handlePrivateChatMessage);
        return this;
    }

    /**
     * sets the clean username of the current localUser
     */
    public void setClearUsername() {
        String newName;
        try {
            newName = URLEncoder.encode(this.editor.getLocalUser().getName(), StandardCharsets.UTF_8.toString());
            cleanLocalUserName = newName;
        } catch (UnsupportedEncodingException e) {
            cleanLocalUserName = this.editor.getLocalUser().getName();
        }
    }

    public String getCleanLocalUserName() {
        return cleanLocalUserName;
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
     */
    public void withOutWebSocket(String url) {
        WebSocketClient webSocketClient = webSocketMap.remove(url);
        if (webSocketClient != null) {
            webSocketClient.stop();
        }
    }


    /**
     * handle messages on the system channel by adding or deleting users from the data model
     *
     * @param msg message from the server on the system channel
     */
    public void handleSystemMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        JsonObject data = jsonObject.getJsonObject(DATA);

        if (jsonObject.getString(ACTION).equals(USER_JOINED)) {
            editor.haveUser(data.getString(ID), data.getString(NAME));

        } else if (jsonObject.getString(ACTION).equals(USER_LEFT)) {
            editor.userLeft(data.getString(ID));
        }
    }

    /**
     * handle chat message by adding it to the data model
     *
     * @param msg message from the server on the private chat channel
     */
    public void handlePrivateChatMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        PrivateMessage message = new PrivateMessage();
        message.setTimestamp(jsonObject.getJsonNumber(TIMESTAMP).longValue());
        message.setText(jsonObject.getString(MESSAGE));
        message.setFrom(jsonObject.getString(FROM));
        message.setTo(jsonObject.getString(TO));
        editor.getMessageManager().addNewPrivateMessage(message);
    }

    /**
     * Handles the response of the websocket server
     *
     * @param msg response of the websocket server
     */
    public void handleServerMessage(JsonStructure msg, Server server) {

        JsonObject data = ((JsonObject) msg).getJsonObject(DATA);
        String action = ((JsonObject) msg).getString(ACTION);

        // change members
        if (action.equals(USER_JOINED) || action.equals(USER_LEFT) || action.equals(USER_ARRIVED) || action.equals(USER_EXITED)) {
            String id = data.getString(ID);
            String name = data.getString(NAME);

            if (action.equals(USER_EXITED)) {
                editor.userWithoutServer(id, server);
            } else {
                // create or get a new user with the data
                User user = editor.haveUserWithServer(name, id, false, server);

                if (action.equals(USER_JOINED)) {
                    user.setOnlineStatus(true);
                    server.firePropertyChange(Server.PROPERTY_MEMBERS, null, server.getMembers());
                }
                if (action.equals(USER_LEFT)) {
                    user.setOnlineStatus(false);
                    server.firePropertyChange(Server.PROPERTY_MEMBERS, null, server.getMembers());
                }
                if (action.equals(USER_ARRIVED)) {
                    user.setOnlineStatus(data.getBoolean(ONLINE));
                }
            }
        }

        // change data of the server
        if (action.equals(SERVER_UPDATED)) {
            server.setName(data.getString(NAME));
        }
        if (action.equals(SERVER_DELETED)) {
            Platform.runLater(editor.getStageManager()::showMainScreen);
        }

        //change category
        if (action.equals(CATEGORY_UPDATED)) {
            editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server);
        }
        if (action.equals(CATEGORY_CREATED)) {
            editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server);
        }
        if (action.equals(CATEGORY_DELETED)){
            Category category = editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server);
            category.removeYou();
        }

        // change channel
        if (action.equals(CHANNEL_UPDATED)) {
            Channel channel = editor.getChannelManager().updateChannel(server, data.getString(ID), data.getString(NAME), data.getString(TYPE), data.getBoolean(PRIVILEGED), data.getString(CATEGORY), data.getJsonArray(MEMBERS));
            if (channel == null) {
                Platform.runLater(() -> editor.getStageManager().showServerScreen(server));
            }
        }
        if (action.equals(CHANNEL_CREATED)) {
            Category category = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
            editor.getChannelManager().haveChannel(data.getString(ID), data.getString(NAME), data.getString(TYPE), data.getBoolean(PRIVILEGED), category, data.getJsonArray(MEMBERS));
        }
        if (action.equals(CHANNEL_DELETED)){
            Category category = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
            Channel channel = editor.getChannelManager().haveChannel(data.getString(ID), data.getString(NAME), null, false, category, Json.createArrayBuilder().build());
            channel.removeYou();
        }

        // change invitation
        if (action.equals(INVITE_EXPIRED)) {
            editor.deleteInvite(data.getString(ID), server);
        }

    }

    /**
     * Send a private chat message.
     *
     * @param jsonMsgString The stringified Json message
     */
    public void sendPrivateChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + cleanLocalUserName);
        webSocketClient.sendMessage(jsonMsgString);
    }

    /**
     * Send a message in the current Server Channel
     *
     * @param jsonMsgString The stringified Json message
     */
    public void sendChannelChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(CHAT_USER_URL + cleanLocalUserName
                        + AND_SERVER_ID_URL + this.editor.getCurrentServer().getId());
        webSocketClient.sendMessage(jsonMsgString);
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
     * Called to stop this controller
     * <p>
     * Stop and remove WebSocketClients
     */
    public void stop() {
        Iterator<Map.Entry<String, WebSocketClient>> iterator = webSocketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WebSocketClient> entry = iterator.next();
            iterator.remove();
            entry.getValue().stop();
        }
    }
}
