package de.uniks.stp.wedoit.accord.client;


import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.WSCallback;
import de.uniks.stp.wedoit.accord.client.network.WebSocketClient;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import org.json.JSONArray;


import java.net.URI;
import java.util.*;

import static de.uniks.stp.wedoit.accord.client.Constants.COM_FROM;
import static de.uniks.stp.wedoit.accord.client.Constants.COM_MEMBERS;

public class Editor {

    private AccordClient accordClient;
    private Map<String,WebSocketClient> webSocketMap = new HashMap<>();
    private Server currentServer;

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
     * get a user by id
     *
     * @param id   id of the user
     * @return user
     */
    public User getUserById(String id) {
        List<User> users = this.currentServer.getMembers();
        Objects.requireNonNull(users);
        Objects.requireNonNull(id);

        for (User user: users) {
            if (id.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * builds a category based on the server json answer
     * !!! no channels added
     *
     * @param server which gets the categories
     * @param serversCategoryResponse server answer for categories of the server
     */
    public List<Category> haveCategories(Server server, JSONArray serversCategoryResponse) {
        Objects.requireNonNull(server);
        Objects.requireNonNull(serversCategoryResponse);

        List<Category> categories = new ArrayList<>();
        for (int index = 0; index < serversCategoryResponse.length(); index++) {
            Category category = JsonUtil.parseCategory(serversCategoryResponse.getJSONObject(index));
            category.setServer(server);
            categories.add(category);
        }
        server.withCategories(categories);
        return categories;
    }

    /**
     * builds a channel based on the server json answer
     *
     * @param category which gets the channels
     * @param categoriesChannelResponse server answer for channels of the category
     */
    public List<Channel> haveChannels(Category category, JSONArray categoriesChannelResponse) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(categoriesChannelResponse);

        this.currentServer = category.getServer();
        List<Channel> channels = new ArrayList<>();
        for (int index = 0; index < categoriesChannelResponse.length(); index++) {
            Channel channel = JsonUtil.parseChannel(categoriesChannelResponse.getJSONObject(index));
            channel.setCategory(category);
            List<String> memberIds = JsonUtil.parseMembers(categoriesChannelResponse.getJSONObject(index));
            for (String memberId: memberIds) {
                User user = this.getUserById(memberId);
                channel.withMembers(user);
            }
        }
        category.withChannels(channels);
        return channels;
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

}
