package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.PrivateMessage;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;

import javax.json.JsonObject;
import javax.json.JsonStructure;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class NetworkController {
    private final Map<String, WebSocketClient> webSocketMap = new HashMap<>();
    private final Editor editor;

    /**
     * Create a NetworkController.
     *
     * @param editor The editor of the Application
     */
    public NetworkController(Editor editor) {
        this.editor = editor;
    }

    /**
     * Called to start this controller.
     * Only call after corresponding fxml is loaded.
     * <p>
     * Create default WebSocketClients.
     */
    public void start() {
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName(), this::handlePrivateChatMessage);
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
     */
    public void handleSystemMessage(JsonStructure msg) {
        JsonObject jsonObject = (JsonObject) msg;
        JsonObject data = jsonObject.getJsonObject(COM_DATA);

        if (jsonObject.getString(COM_ACTION).equals("userJoined")) {
            editor.haveUser(data.getString(COM_ID), data.getString(COM_NAME));

        } else if (jsonObject.getString(COM_ACTION).equals("userLeft")) {
            editor.userLeft(data.getString(COM_ID));
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
        message.setTimestamp(jsonObject.getJsonNumber(COM_TIMESTAMP).longValue());
        message.setText(jsonObject.getString(COM_MESSAGE));
        message.setFrom(jsonObject.getString(COM_FROM));
        message.setTo(jsonObject.getString(COM_TO));

        editor.addNewPrivateMessage(message);
    }

    /**
     * Send a private chat message.
     *
     * @param jsonMsgString The stringified Json message
     */
    public void sendPrivateChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName());
        webSocketClient.sendMessage(jsonMsgString);
    }

    /**
     * Send a message in the current Server Channel
     *
     * @param jsonMsgString The stringified Json message
     */
    public void sendChannelChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(CHAT_USER_URL + this.editor.getLocalUser().getName()
                        + AND_SERVER_ID_URL + this.editor.getCurrentServer().getId());
        webSocketClient.sendMessage(jsonMsgString);
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
