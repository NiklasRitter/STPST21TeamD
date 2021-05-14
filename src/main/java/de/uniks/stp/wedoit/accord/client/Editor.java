package de.uniks.stp.wedoit.accord.client;


import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import javafx.application.Platform;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.Constants.COM_FROM;

public class Editor {

    private AccordClient accordClient;
    private Map<String,WebSocketClient> webSocketMap = new HashMap<>();

    /**
     * create localUser without initialisation and set localUser in Editor
     *
     * @return localUser
     */
    public LocalUser haveLocalUser() {
        LocalUser localUser = new LocalUser();
        accordClient.setLocalUser(localUser);
        return localUser;
    }


    public AccordClient haveAccordClient() {
        accordClient = new AccordClient();
        return accordClient;
    }

    /**
     * create localUser with the given arguments and set localUser in Editor
     * <p>
     * if localUser already exists set username and userkey to current localUser
     *
     * @param username id of the localUser
     * @param userKey  name of the localUser
     * @return localUser
     */
    public LocalUser haveLocalUser(String username, String userKey) {
        LocalUser localUser = accordClient.getLocalUser();
        if (localUser == null) {
            localUser = new LocalUser();
        }
        localUser.setName(username);
        localUser.setUserKey(userKey);
        return localUser;
    }

    /**
     * return localUser
     *
     * @return localUser
     */
    public LocalUser getLocalUser() {
        return accordClient.getLocalUser();
    }

    /**
     * create a sever with the given arguments and with localUser as Member
     *
     * @param localUser localUser is member of the server with following id and name
     * @param id        id of the sever
     * @param name      name of the server
     * @return server with given id and name and with member localUser
     */
    public Server haveServer(LocalUser localUser, String id, String name) {
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        if (localUser.getServers() != null) {
            for (Server server : localUser.getServers()) {
                if (server.getId().equals(id)) {
                    return server;
                }
            }
        }
        Server server = new Server().setId(id).setName(name);
        server.setLocalUser(localUser);
        return server;
    }

    public User haveUserWithServer(String name, String id, boolean online, Server server) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(id);
        Objects.requireNonNull(server);
        for (User user: server.getMembers()) {
            if(user.getId().equals(id)) {
            return user;
            }
        }
        User user = new User().setName(name).setId(id).setOnlineStatus(online).withServers(server);
        return user;
    }

    /**
     * create a user with the given arguments and add to users of localUser
     *
     * @param id   id of the user
     * @param name name of the user
     * @return localUser
     */
    public LocalUser haveUser(String id, String name) {
        LocalUser localUser = accordClient.getLocalUser();
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);

        if (name.equals(localUser.getName())) {
            return localUser;
        }

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    return localUser;
                }
            }
        }

        User user = new User().setId(id).setName(name);
        localUser.withUsers(user);
        return localUser;
    }

    /**
     * @param name name of user
     * @return user with fitting username
     */
    public User getUser(String name) {
        Objects.requireNonNull(name);

        LocalUser localUser = accordClient.getLocalUser();

        for (User user : localUser.getUsers()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    /**
     * deletes a user with the given id
     *
     * @param id id of the user
     * @return this
     */
    public Editor userLeft(String id) {
        LocalUser localUser = accordClient.getLocalUser();

        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    localUser.withoutUsers(user);
                    return this;
                }
            }
        }
        return this;
    }

    /**
     * add message to privateChat of corresponding user
     *
     * @param message to add to the model
     */
    public void addNewPrivateMessage(PrivateMessage message){
        if (message.getFrom().equals(getLocalUser().getName())){
            getUser(message.getTo()).getPrivateChat().withMessages(message);
        }
        else {
            getUser(message.getFrom()).getPrivateChat().withMessages(message);
        }
    }

    /**
     * This method is for testing
     * @param url testUrl
     * @param webSocketClient testWebSocket
     * @return webSocketClient which is given
     */
    public WebSocketClient haveWebSocket(String url, WebSocketClient webSocketClient) {
        if (webSocketMap.get(url) != null) {
            return webSocketMap.get(url);
        } else {
            webSocketMap.put(url, webSocketClient);
        }
        return webSocketClient;
    }

    /**
     * Create a new webSocket and put the webSocket in the WebSocketMap,
     * The webSocket has to be deleted when the websocket is no longer used
     * with method editor.withOutUrl(url)
     * @param url url for the webSocket connection
     * @param callback callback for the
     * @return webSocketClient which is given
     */
    public WebSocketClient haveWebSocket(String url, WSCallback callback) {
        WebSocketClient webSocket;
        if (webSocketMap.get(url) != null) {
            return webSocketMap.get(url);
        } else{

            webSocket = new WebSocketClient(this,URI.create(url),callback);
            webSocketMap.put(url,webSocket);
            return webSocket;
        }
    }


    /**
     * remove a webSocket with given url
     * @param url url of a webSocket
     * @return the webSocket which is removed or null if there was no mapping of this url
     */
    public WebSocketClient withOutWebSocket(String url) {
        return webSocketMap.remove(url);
    }

    /**
     * the localUser is logged out and will be redirect to the LoginScreen
     *
     * @param userKey userKey of the user who is logged out
     * @param restClient restClient instance for the LoginScreenController
     */
    public void logoutUser(String userKey, RestClient restClient) {
        if (userKey != null && !userKey.isEmpty()) {
            restClient.logout(userKey, response -> {
                if (response.getBody().getObject().getString("status").equals("success")) {
                    Platform.runLater(() -> StageManager.showLoginScreen(restClient));
                } else {
                    System.err.println("Error while logging out");
                    Platform.runLater(() -> StageManager.showLoginScreen(restClient));
                }
            });
        }
    }
}
