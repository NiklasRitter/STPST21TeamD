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
    private Map<String, WebSocketClient> webSocketMap = new HashMap<>();
    private Editor editor;

    public NetworkController(Editor editor) {
        this.editor = editor;
    }

    public void start() {
        haveWebSocket(SYSTEM_SOCKET_URL, this::handleSystemMessage);
        //haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getLocalUser().getName(), this::handlePrivateChatMessage);
        haveWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getCleanUsername(), this::handlePrivateChatMessage);
        System.out.println(PRIVATE_USER_CHAT_PREFIX + this.editor.getCleanUsername());
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

        jsonObject.getString(COM_CHANNEL).equals("private");
        PrivateMessage message = new PrivateMessage();
        message.setTimestamp(jsonObject.getInt(COM_TIMESTAMP));
        message.setText(jsonObject.getString(COM_MESSAGE));
        message.setFrom(jsonObject.getString(COM_FROM));
        message.setTo(jsonObject.getString(COM_TO));

        editor.addNewPrivateMessage(message);
    }

    public void sendPrivateChatMessage(String jsonMsgString) {
        WebSocketClient webSocketClient =
                getOrCreateWebSocket(PRIVATE_USER_CHAT_PREFIX + this.editor.getCleanUsername());

        webSocketClient.sendMessage(jsonMsgString);
    }

    public void stop() {
        Iterator<Map.Entry<String, WebSocketClient>> iterator = webSocketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WebSocketClient> entry = iterator.next();
            iterator.remove();
            entry.getValue().stop();
        }
    }
}
