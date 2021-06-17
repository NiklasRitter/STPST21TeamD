package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.controller.NetworkController;
import de.uniks.stp.wedoit.accord.client.controller.SystemTrayController;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javax.json.JsonArray;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.ID;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.*;

public class Editor {

    private final NetworkController networkController = new NetworkController(this);
    private AccordClient accordClient;
    private Server currentServer;
    private StageManager stageManager;

    /**
     * @return private final NetworkController networkController
     */
    public NetworkController getNetworkController() {
        return networkController;
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
        stageManager.getResourceManager();
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
        networkController.setClearUsername();
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
     * This method gives the the server categories which are created with the data of the JSONArray
     * The categories dont have channels.
     *
     * @param server                  server which gets the categories
     * @param serversCategoryResponse server answer for categories of the server
     */
    public List<Category> haveCategories(Server server, JsonArray serversCategoryResponse) {
        Objects.requireNonNull(server);
        Objects.requireNonNull(serversCategoryResponse);

        List<String> categoryIds = new ArrayList<>();
        for (Category category : server.getCategories()) {
            categoryIds.add(category.getId());
        }
        for (int index = 0; index < serversCategoryResponse.toArray().length; index++) {
            if (!categoryIds.contains(serversCategoryResponse.getJsonObject(index).getString(ID))) {
                Category category = JsonUtil.parseCategory(serversCategoryResponse.getJsonObject(index));
                category.setServer(server);
            }
        }
        return server.getCategories();
    }

    /**
     * This method gives the category channels which are created with the data of the JSONArray
     *
     * @param category                  category which gets the channels
     * @param categoriesChannelResponse server answer for channels of the category
     */
    public List<Channel> haveChannels(Category category, JsonArray categoriesChannelResponse) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(categoriesChannelResponse);

        List<String> channelIds = new ArrayList<>();
        for (Channel channel : category.getChannels()) {
            channelIds.add(channel.getId());
        }
        for (int index = 0; index < categoriesChannelResponse.toArray().length; index++) {

            if (!channelIds.contains(categoriesChannelResponse.getJsonObject(index).getString(ID))) {
                Channel channel = JsonUtil.parseChannel(categoriesChannelResponse.getJsonObject(index));
                channel.setCategory(category);
                List<String> memberIds = JsonUtil.parseMembers(categoriesChannelResponse.getJsonObject(index));
                for (String memberId : memberIds) {
                    User user = this.getServerUserById(category.getServer(), memberId);

                    channel.withMembers(user);
                }
            }
        }
        return category.getChannels();
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
                    user.setOnlineStatus(false);
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
    public void addNewPrivateMessage(PrivateMessage message) {
        if (message.getText().equals(GAMEINVITE)) {
            if (message.getFrom().equals(getLocalUser().getName()))
                getLocalUser().withGameRequests(getUser(message.getTo()));
            else getLocalUser().withGameInvites(getUser(message.getFrom()));
            message.setText(message.getText().substring(PREFIX.length()));
        }
        if (message.getText().startsWith(PREFIX) && (message.getText().endsWith(ROCK) || message.getText().endsWith(PAPER) || message.getText().endsWith(SCISSORS))) {
            if (!message.getFrom().equals(getLocalUser().getName()))
                getUser(message.getFrom()).setGameMove(message.getText().substring(PREFIX.length()));

        } else {
            if (message.getFrom().equals(getLocalUser().getName())) {
                getUser(message.getTo()).getPrivateChat().withMessages(message);
            } else {
                SystemTrayController systemTrayController = stageManager.getSystemTrayController();
                if (systemTrayController != null) {
                    systemTrayController.displayPrivateMessageNotification(message);
                }
                User user = getUser(message.getFrom());
                Chat privateChat = user.getPrivateChat();
                if (privateChat == null) {
                    privateChat = new Chat().setName(user.getName()).setUser(user);
                    user.setPrivateChat(privateChat);
                }
                privateChat.withMessages(message);
                user.setChatRead(false);
            }
        }
    }

    /**
     * add message to channel chat
     *
     * @param message to add to the model
     */
    public void addNewChannelMessage(Message message) {
        message.getChannel().withMessages(message);
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
            networkController.stop();
            networkController.logoutUser(userKey);
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
     * @return the given server if the user was deleted
     * return null, if user was not in the members list
     */
    public Server userWithoutServer(String id, Server server) {
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
            return server;
        }
        return null;
    }

    /**
     * This method
     * <p>
     * - creates a category with the given arguments
     * <p>
     * - updates a category with the given name if the category has already been created
     *
     * @param id   id of the category
     * @param name name of the category
     * @return category with given id and name and with server server
     */
    public Category haveCategory(String id, String name, Server server) {

        for (Category category : server.getCategories()) {
            if (category.getId().equals(id)) {
                return category.setName(name);
            }
        }
        return new Category().setName(name).setId(id).setServer(server);
    }

    /**
     * This method
     * <p>
     * - creates a channel with the given arguments
     * <p>
     * - updates a channel with the given name, type, privileged, category and members
     * if the channel has already been created
     * <p>
     * to update a channel use updateChannel()
     *
     * @param id id of the channel which channels compared by
     * @return category with given id and name and with server server
     */
    public Channel haveChannel(String id, String name, String type, Boolean privileged, Category category, JsonArray members) {
        Server server = category.getServer();
        Channel channel = null;
        for (Channel channelIterator : category.getChannels()) {
            if (channelIterator.getId().equals(id)) {
                channel = channelIterator;
                break;
            }
        }
        if (channel == null) {
            channel = new Channel();
        }
        channel.setName(name).setPrivileged(privileged).setType(type).setId(id).setCategory(category);
        channel.withoutMembers(new ArrayList<>(channel.getMembers()));

        List<String> membersIds = new ArrayList<>();
        for (int index = 0; index < members.toArray().length; index++) {
            membersIds.add(members.getString(index));
        }

        if (privileged) {
            for (User user : server.getMembers()) {
                if (membersIds.contains(user.getId())) {
                    channel.withMembers(user);
                }
            }
        }
        return channel;
    }

    /**
     * This method
     * <p>
     * updates a channel with the given name, privileged and members. Only name, privileged and members will upgraded
     *
     * @param id id of the channel which channels compared by
     * @return channel upgraded channel or null
     */
    public Channel updateChannel(Server server, String id, String name, String type, Boolean privileged, String categoryId, JsonArray members) {

        for (Category category : server.getCategories()) {
            if (category.getId().equals(categoryId)) {
                for (Channel channel : category.getChannels()) {
                    if (channel.getId().equals(id)) {
                        channel.setName(name);
                        channel.setPrivileged(privileged);
                        channel.withoutMembers(new ArrayList<>(channel.getMembers()));
                        if (privileged) {
                            List<String> membersIds = new ArrayList<>();
                            for (int index = 0; index < members.toArray().length; index++) {
                                membersIds.add(members.getString(index));
                            }

                            for (String memberId : membersIds) {
                                User user = this.getServerUserById(category.getServer(), memberId);
                                Objects.requireNonNull(user);
                                channel.withMembers(user);
                            }
                        }
                        return channel;
                    }
                }
            }
        }
        return null;
    }

    public void updateChannelMessages(Channel channel, List<Message> messages) {
        List<Message> channelMessages = channel.getMessages();
        for (Message message : messages) {
            boolean msgExists = false;
            for (Message channelMessage : channelMessages) {
                if (channelMessage.getId().equals(message.getId())) {
                    msgExists = true;
                    break;
                }
            }
            if (!msgExists) {
                channel.withMessages(message);
            }
        }
    }

    public void leaveServer(String userKey, String id) {
        if (id != null && !id.isEmpty()) {
            networkController.leaveServer(userKey, id);
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

    public Boolean copyToSystemClipBoard(String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        return clipboard.setContent(content);
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(PrivateMessage message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }

    /**
     * formats a message with the correct date in the format
     * <p>
     * [" + dd/MM/yyyy HH:mm:ss + "] " + FROM + ": " + MESSAGE
     * @param message message which should formatted
     * @return the formatted message as string
     */
    public String getMessageFormatted(Message message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(message.getTimestamp()));

        return ("[" + time + "] " + message.getFrom() + ": " + message.getText());
    }


    public String cleanMessage(PrivateMessage item) {
        if (isQuote(item)) {
            String quoteMessage = item.getText().substring(QUOTE_PREFIX.length(), item.getText().length() - QUOTE_SUFFIX.length());
            String[] messages = quoteMessage.split(QUOTE_ID);
            return messages[0];
        } else return item.getText();
    }

    /**
     * checks whether a message is a quote
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(PrivateMessage item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }

    /**
     * checks whether a message is a quote
     * @param item item as message
     * @return boolean whether a item is a quote
     */
    public boolean isQuote(Message item) {
        return item.getText().contains(QUOTE_PREFIX) && item.getText().contains(QUOTE_SUFFIX) && item.getText().contains(QUOTE_ID)
                && item.getText().length() >= (QUOTE_PREFIX.length() + QUOTE_SUFFIX.length() + QUOTE_ID.length())
                && (item.getText()).startsWith(QUOTE_PREFIX);
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public void automaticLogin(AccordClient accordClient) {
        if (accordClient.getOptions().isRememberMe() && accordClient.getLocalUser() != null && accordClient.getLocalUser().getName() != null && accordClient.getLocalUser().getPassword() != null && !accordClient.getLocalUser().getName().isEmpty() && !accordClient.getLocalUser().getPassword().isEmpty()) {
            networkController.automaticLoginUser(accordClient.getLocalUser().getName(), accordClient.getLocalUser().getPassword(), this);
        } else {
            stageManager.showLoginScreen();
            stageManager.getStage().show();
        }
    }

    public void handleAutomaticLogin(boolean success) {
        if (success) {
            Platform.runLater(stageManager::showMainScreen);
        } else {
            Platform.runLater(stageManager::showLoginScreen);
        }
        Platform.runLater(() -> {
            stageManager.getStage().show();
        });
    }

    /*
    TODO: Add encryption for saved password and username
    public Key createEncryptionKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(System.getProperty("user.name").toCharArray(),
                Base64.getDecoder().decode(StageManager.getResourceManager().getOrCreateInitializationVector()), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public IvParameterSpec getOrCreateIvParameterSpec() throws NoSuchAlgorithmException {
        return new IvParameterSpec(Base64.getDecoder().decode(StageManager.getResourceManager().getOrCreateInitializationVector()));
    }

    public String encryptData(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, createEncryptionKey(), getOrCreateIvParameterSpec());
        byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(data));
        return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decryptData(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, createEncryptionKey(), getOrCreateIvParameterSpec());
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(data));
        return Base64.getEncoder().encodeToString(plainText);
    }*/

}
