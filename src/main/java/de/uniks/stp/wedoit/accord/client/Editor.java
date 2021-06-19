package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.*;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;

public class Editor {

    private final RestManager restManager = new RestManager(this);
    private final WebSocketManager webSocketManager = new WebSocketManager(this);
    private final ChannelManager channelManager = new ChannelManager(this);
    private final CategoryManager categoryManager = new CategoryManager();
    private final MessageManager messageManager = new MessageManager(this);
    private AccordClient accordClient;
    private Server currentServer;
    private StageManager stageManager;

    /**
     * @return private final RestManager restManager
     */
    public RestManager getRestManager() {
        return restManager;
    }

    /**
     * @return private final WebSocketManager webSocketManager
     */
    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public Server getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

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
     * if localUser already exists set username and userKey to current localUser
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
        webSocketManager.setClearUsername();
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
     * This method
     * <p>
     * creates a sever with the given arguments and with localUser as Member
     * <p>
     * updates a server with the given name if the server has already been created
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
                    return server.setName(name);
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
        for (User user : server.getMembers()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return new User().setName(name).setId(id).setOnlineStatus(online).withServers(server);
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
                    user.setOnlineStatus(true);
                    return localUser;
                }
            }
        }

        User user = new User().setId(id).setName(name).setOnlineStatus(true);
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
     * @param userId id of the user
     * @return user
     */
    public User getServerUserById(Server server, String userId) {
        List<User> users = server.getMembers();
        Objects.requireNonNull(users);
        Objects.requireNonNull(userId);

        for (User user : users) {
            if (userId.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * deletes a user with the given id
     *
     * @param id id of the user
     */
    public void userLeft(String id) {
        LocalUser localUser = accordClient.getLocalUser();

        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    user.setOnlineStatus(false);
                    return;
                }
            }
        }
    }

    /**
     * add message to channel chat
     *
     * @param ownAction game action of localUser
     * @param oppAction game action of opponent user
     */
    public Boolean resultOfGame(String ownAction, String oppAction) {
        if (ownAction.equals(oppAction)) return null;

        if (ownAction.equals(ROCK)) return oppAction.equals(SCISSORS);

        else if (ownAction.equals(PAPER)) return oppAction.equals(ROCK);

        else return oppAction.equals(PAPER);
    }

    /**
     * the localUser is logged out and will be redirect to the LoginScreen
     *
     * @param userKey userKey of the user who is logged out
     */
    public void logoutUser(String userKey) {
        accordClient.getOptions().setRememberMe(false);
        accordClient.getLocalUser().setPassword("");
        accordClient.getLocalUser().setName("");
        if (userKey != null && !userKey.isEmpty()) {
            webSocketManager.stop();
            restManager.logoutUser(userKey);
        }
    }

    public void handleLogoutUser(boolean success) {
        if (!success) {
            System.err.println("Error while logging out");
        }
        Platform.runLater(() -> stageManager.showLoginScreen());
    }

    public List<User> getOnlineUsers() {
        List<User> allUsers = this.getLocalUser().getUsers();
        List<User> onlineUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (user.isOnlineStatus()) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    /**
     * Delete a member of a server with the given id
     *
     * @param id     id of the member who should deleted
     * @param server server with member
     */
    public void userWithoutServer(String id, Server server) {
        User thisUser = null;
        for (User user : server.getMembers()) {
            if (user.getId().equals(id)) {
                thisUser = user;
                server.withoutMembers(user);
            }
        }
        if (thisUser != null) {
            for (Category category : server.getCategories()) {
                for (Channel channel : category.getChannels()) {
                    if (channel.getMembers().contains(thisUser)) {
                        channel.withoutMembers(thisUser);
                    }
                }
            }
        }
    }

    public void leaveServer(String userKey, Server server) {
        if (server.getId() != null && !server.getId().isEmpty()) {
            restManager.leaveServer(userKey, server.getId());
            this.getLocalUser().withoutServers(server);
        }
    }


    public Invitation deleteInvite(String id, Server server) {
        for (Invitation invite : server.getInvitations()) {
            if (invite.getId().equals(id)) {
                invite.removeYou();
                return invite;
            }
        }
        return null;
    }

    public void copyToSystemClipBoard(String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
