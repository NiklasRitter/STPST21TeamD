package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
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

import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.MAIN_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.ControllerNames.SERVER_SCREEN_CONTROLLER;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;
import static de.uniks.stp.wedoit.accord.client.constants.Network.*;
import static de.uniks.stp.wedoit.accord.client.constants.Stages.STAGE;

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
        PrivateMessage message = JsonUtil.parsePrivateMessage(jsonObject);
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
                if (user == null) {
                    return;
                }

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
            return;
        }

        switch (action) {
            case SERVER_UPDATED:
                server.setName(data.getString(NAME));
                break;
            case SERVER_DELETED:
                Platform.runLater(() -> editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
                break;
            case CATEGORY_CREATED:
                editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server);
                break;
            case CATEGORY_UPDATED:
                editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server);
                break;
            case CATEGORY_DELETED:
                editor.getCategoryManager().haveCategory(data.getString(ID), data.getString(NAME), server).removeYou();
                break;
            case CHANNEL_CREATED:
                Category category = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
                editor.getChannelManager().haveChannel(data.getString(ID), data.getString(NAME), data.getString(TYPE), data.getBoolean(PRIVILEGED), category, data.getJsonArray(MEMBERS), data.getJsonArray(AUDIOMEMBERS));
                break;
            case CHANNEL_UPDATED:
                Channel channel = editor.getChannelManager().updateChannel(server, data.getString(ID), data.getString(NAME), data.getString(TYPE), data.getBoolean(PRIVILEGED), data.getString(CATEGORY), data.getJsonArray(MEMBERS), data.getJsonArray(AUDIOMEMBERS));
                if (channel == null) {
                    Platform.runLater(() -> editor.getStageManager().initView(STAGE, LanguageResolver.getString("SERVER"), "ServerScreen", SERVER_SCREEN_CONTROLLER, true, server, null));
                }
                break;
            case CHANNEL_DELETED:
                Category categoryDeleted = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
                Channel channelDeleted = editor.getChannelManager().haveChannel(data.getString(ID), data.getString(NAME), null, false, categoryDeleted, Json.createArrayBuilder().build(), Json.createArrayBuilder().build());
                // Todo
                if(editor.getLocalUser().getAudioChannel() != null && editor.getLocalUser().getAudioChannel() == channelDeleted){
                    editor.getAudioManager().closeAudioConnection();
                    editor.getRestManager().getRestClient().leaveAudioChannel(editor.getLocalUser().getUserKey(), server.getId(), categoryDeleted.getId(), categoryDeleted.getId(), response -> {
                        if (response.getBody().getObject().getString(STATUS).equals(SUCCESS)) {
                            editor.getLocalUser().setAudioChannel(null);
                            channelDeleted.removeYou();
                        }
                    });
                }
                else{
                    channelDeleted.removeYou();
                }
                break;
            case INVITE_EXPIRED:
                editor.deleteInvite(data.getString(ID), server);
                break;
            case MESSAGE_UPDATED:
                Message messageToUpdate = JsonUtil.parseMessageUpdated(data);
                Channel channelUpdatedMessage = editor.getChannelById(server, data.getString(CATEGORY), data.getString(CHANNEL));
                if (channelUpdatedMessage == null) {
                    Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
                    System.err.println("Error from message updated");
                    return;
                }
                editor.getMessageManager().updateMessage(channelUpdatedMessage, messageToUpdate);
                break;
            case MESSAGE_DELETED:
                Channel channelDeleteMessage = editor.getChannelById(server, data.getString(CATEGORY), data.getString(CHANNEL));
                if (channelDeleteMessage == null) {
                    Platform.runLater(() -> this.editor.getStageManager().initView(STAGE, LanguageResolver.getString("MAIN"), "MainScreen", MAIN_SCREEN_CONTROLLER, true, null, null));
                    System.err.println("Error from message delete");
                    return;
                }
                editor.getMessageManager().deleteMessage(channelDeleteMessage, data.getString(ID));
                break;
            case AUDIO_JOINED:
                Category categoryJoined = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
                Channel channelJoined = editor.getChannelManager().getChannel(data.getString(CHANNEL), categoryJoined);
                User userJoined = editor.getServerUserById(server, data.getString(ID));
                channelJoined.withAudioMembers(userJoined);
                break;
            case AUDIO_LEFT:
                Category categoryLeft = editor.getCategoryManager().haveCategory(data.getString(CATEGORY), null, server);
                Channel channelLeft = editor.getChannelManager().getChannel(data.getString(CHANNEL), categoryLeft);
                User userLeft = editor.getServerUserById(server, data.getString(ID));
                channelLeft.withoutAudioMembers(userLeft);
                break;
            default:
                System.err.println("Unknown Server Action");
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
