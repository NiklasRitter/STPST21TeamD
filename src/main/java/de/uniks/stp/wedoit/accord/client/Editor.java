package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.constants.ControllerEnum;
import de.uniks.stp.wedoit.accord.client.constants.StageEnum;
import de.uniks.stp.wedoit.accord.client.db.SqliteDB;
import de.uniks.stp.wedoit.accord.client.language.LanguageResolver;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.util.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static de.uniks.stp.wedoit.accord.client.constants.Game.*;
import static de.uniks.stp.wedoit.accord.client.constants.MessageOperations.MESSAGE_LINK;
import static de.uniks.stp.wedoit.accord.client.constants.Network.SLASH;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.AUDIO;
import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.*;

public class Editor {

    private final RestManager restManager = new RestManager(this);
    private final SteamManager steamManager = new SteamManager(this);
    private final WebSocketManager webSocketManager = new WebSocketManager(this);
    private final ChannelManager channelManager = new ChannelManager(this);
    private final CategoryManager categoryManager = new CategoryManager();
    private final MessageManager messageManager = new MessageManager(this);
    private final AudioManager audioManager = new AudioManager(this);
    private AccordClient accordClient;
    private Server currentServer;
    private StageManager stageManager;
    private SqliteDB db;

    /**
     * used to decode the given string
     *
     * @param property given string that has to be decoded
     * @return decoded property as bytes
     */
    private static byte[] base64Decode(String property) {
        return Base64.getDecoder().decode(property);
    }

    public static String parseUserDescription(String rawDescription) {
        if (rawDescription != null && !rawDescription.isEmpty()) {
            switch (String.valueOf(rawDescription.charAt(0))) {
                case SPOTIFY_KEY:
                    return rawDescription.replace(SPOTIFY_KEY, LanguageResolver.getString(SPOTIFY));
                case GITHUB_KEY:
                    return rawDescription.replace(GITHUB_KEY, LanguageResolver.getString(GITHUB));
                case STEAM_KEY:
                    return rawDescription.replace(STEAM_KEY, LanguageResolver.getString(STEAM));
                case CUSTOM_KEY:
                    return rawDescription.replace(CUSTOM_KEY, LanguageResolver.getString(CUSTOM));
                case CLUB_PENGUIN:
                    return rawDescription.replace(CLUB_PENGUIN_KEY, LanguageResolver.getString(CLUB_PENGUIN));
            }
        }
        return rawDescription;
    }

    /**
     * @return private final RestManager restManager
     */
    public RestManager getRestManager() {
        return restManager;
    }

    /**
     * @return private final SteamManager steamManager
     */
    public SteamManager getSteamManager() {
        return steamManager;
    }

    public AccordClient getAccordClient() {
        return accordClient;
    }

    /**
     * @return private final WebSocketManager webSocketManager
     */
    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    /**
     * @return private final AudioManager audioManager
     */
    public AudioManager getAudioManager() {
        return audioManager;
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
        steamManager.setupSteamTimer();
        return localUser;
    }

    /**
     * creates a new AccordClient
     *
     * @return new Accord client
     */
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
            haveLocalUser();
        }
        localUser.setName(username);
        localUser.setUserKey(userKey);
        webSocketManager.setClearUsername();
        setUpDB();
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
        return haveUserWithServer(name, id, online, server, "");
    }

    /**
     * @return a user with given id, onlineStatus and name who is member of the given server
     */
    public User haveUserWithServer(String name, String id, boolean online, Server server, String description) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(id);
        Objects.requireNonNull(server);
        for (User user : server.getMembers()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return haveUser(id, name, description).setOnlineStatus(online).withServers(server);
    }

    public User haveUser(String id, String name) {
        return haveUser(id, name, "");
    }

    /**
     * create a user with the given arguments and add to users of localUser
     *
     * @param id   id of the user
     * @param name name of the user
     * @return localUser
     */
    public User haveUser(String id, String name, String description) {
        LocalUser localUser = accordClient.getLocalUser();
        Objects.requireNonNull(localUser);
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);

        if (name.equals(localUser.getName())) {
            User user = getUser(name);
            if (user == null) {
                user = new User().setName(name);
            }
            user.setId(id);
            user.setDescription(description);
            return user;
        }

        if (localUser.getUsers() != null) {
            for (User user : localUser.getUsers()) {
                if (user.getId().equals(id)) {
                    user.setOnlineStatus(true);
                    user.setChatRead(true);
                    return user;
                }
            }
        }

        User user = new User().setId(id).setName(name).setOnlineStatus(true).setChatRead(true);
        localUser.withUsers(user);
        user.setDescription(description);
        return user;
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

        if (ownAction.equals(GAME_ROCK)) return oppAction.equals(GAME_SCISSORS);

        else if (ownAction.equals(GAME_PAPER)) return oppAction.equals(GAME_ROCK);

        else return oppAction.equals(GAME_PAPER);
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

    /**
     * redirect to the LoginScreen if success
     *
     * @param success of the logout request
     */
    public void handleLogoutUser(boolean success) {
        if (!success) {
            System.err.println("Error while logging out");
        }
        Platform.runLater(() -> {
            stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null);
            stageManager.getStage(StageEnum.POPUP_STAGE).hide();
        });
    }

    /**
     * @return all online users who are listed in the data model
     */
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

    public double calculateRMS(byte[] buf, int bytes) {
        float[] samples = new float[1024];

        for(int i = 0, s = 0; i < bytes;) {
            int sample = 0;

            sample |= buf[i++] & 0xFF; // (reverse these two lines
            sample |= buf[i++] << 8;   //  if the format is big endian)

            // normalize to range of +/-1.0f
            samples[s++] = sample / 32768f;
        }
        float rms = 0f;
        for (float sample : samples) {
            rms += sample * sample;
        }

        return (float) Math.sqrt(rms / samples.length);

    }

    /**
     * creates a instance of the sqlite databank and loads the font size
     * right after log in since the username is needed
     */
    public void setUpDB() {
        db = new SqliteDB(webSocketManager.getCleanLocalUserName());
        getLocalUser().setSteam64ID(getSteam64ID());
    }

    /**
     * @param message to be saved
     */
    public void savePrivateMessage(PrivateMessage message) {
        db.save(message);
    }

    public String getSteam64ID() {
        return db.getSteam64ID();
    }

    public void saveSteam64ID(String steam64ID) {
        db.updateSteam64ID(steam64ID);
    }

    /**
     * loads old offline chats that the local users has a history with
     *
     * @return list of offline users with a chat history
     */
    public List<User> loadOldChats() {
        List<User> offlineUser = new ArrayList<>();
        for (String s : db.getOpenChats(getLocalUser().getName())) {
            if (getOnlineUsers().stream().noneMatch((u) -> u.getName().equals(s))) {
                User user = new User().setName(s);
                getUserChatRead(user);
                offlineUser.add(user);
            }
        }
        return offlineUser;
    }

    /**
     * @param user specific user to load 50 old messages
     * @return the last 50 or less messages between local user and user
     */
    public List<PrivateMessage> loadOldMessages(String user) {
        return db.getLastFiftyMessagesBetweenUsers(user);
    }

    /**
     * @param user   specific user to load 50 old messages
     * @param offset current offset to load the next 50
     * @return the next 50 or less messages
     */
    public List<PrivateMessage> loadOlderMessages(String user, int offset) {
        return db.getLastFiftyMessagesBetweenUsers(user, offset);
    }

    public void updateUserChatRead(User user) {
        db.updateOrInsertUserChatRead(user);
    }

    public void getUserChatRead(User user) {
        db.getChatReadForUser(user);
    }

    /**
     * removes the localuser from a server in the data model and call the rest manager
     */
    public void leaveServer(String userKey, Server server) {
        if (server.getId() != null && !server.getId().isEmpty()) {
            restManager.leaveServer(userKey, server.getId());
            this.getLocalUser().withoutServers(server);
        }
    }

    /**
     * delete a invitation in the data model
     */
    public Invitation deleteInvite(String id, Server server) {
        for (Invitation invite : server.getInvitations()) {
            if (invite.getId().equals(id)) {
                invite.removeYou();
                return invite;
            }
        }
        return null;
    }

    /**
     * copies a given text to the system clip board
     *
     * @param text text which should be copied
     */
    public void copyToSystemClipBoard(String text) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * calls the restManager to login automatically or show the login screen if remember me is not set.
     */
    public void automaticLogin(AccordClient accordClient) {
        if (accordClient.getOptions().isRememberMe() && accordClient.getLocalUser() != null && accordClient.getLocalUser().getName() != null && accordClient.getLocalUser().getPassword() != null && !accordClient.getLocalUser().getName().isEmpty() && !accordClient.getLocalUser().getPassword().isEmpty()) {
            restManager.automaticLoginUser(accordClient.getLocalUser().getName(), accordClient.getLocalUser().getPassword(), this);
        } else {
            stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null);
        }
    }

    /**
     * handles the automatic login
     */
    public void handleAutomaticLogin(boolean success) {
        if (success) {
            Platform.runLater(() -> {
                stageManager.initView(ControllerEnum.PRIVATE_CHAT_SCREEN, null, null);
                stageManager.getStage(StageEnum.STAGE).setResizable(true);
                stageManager.getStage(StageEnum.STAGE).setMaximized(true);
            });
        } else {
            Platform.runLater(() -> Platform.runLater(() -> stageManager.initView(ControllerEnum.LOGIN_SCREEN, true, null)));
        }
    }

    /**
     * Adds members to datamodel if they not exist already and sets link to server
     *
     * @param members The users that have to be added
     * @param server  The server the users have to be added to
     */
    public void serverWithMembers(List<User> members, Server server) {
        for (User member : members) {
            haveUserWithServer(member.getName(), member.getId(), member.isOnlineStatus(), server, member.getDescription());
        }
    }

    /**
     * returns channel by its id
     *
     * @param server     server of the channel
     * @param channelId  id of the searched channel
     * @param categoryId category of the channel
     * @return the channel with the given id
     */
    public Channel getChannelById(Server server, String categoryId, String channelId) {
        Category category = getCategoryById(server, categoryId);
        if (category == null) {return null;}
        for (Channel channel : category.getChannels()) {
            if (channel.getId().equals(channelId)) {
                return channel;
            }
        }
        return null;
    }

    /**
     * returns category by its id
     *
     * @param server server of the channel
     * @param id     id of the searched category
     * @return the category with the given id
     */
    public Category getCategoryById(Server server, String id) {
        for (Category category : server.getCategories()) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Creates encryption key based on username of the pc and uses InitializationVector as salt
     *
     * @return the created Key
     */
    public Key createEncryptionKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec keySpec = new PBEKeySpec(System.getProperty("user.name").toCharArray(),
                stageManager.getResourceManager().getOrCreateInitializationVector().getBytes(StandardCharsets.UTF_8),
                65536, 256);
        SecretKey key = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(key.getEncoded(), "AES");
    }

    /**
     * encrypts the given data
     *
     * @param data given data that has to be encrypted
     * @return encrypted version of data
     */
    public String encrypt(String data) throws Exception {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Key key = createEncryptionKey();
        pbeCipher.init(Cipher.ENCRYPT_MODE, key);
        AlgorithmParameters parameters = pbeCipher.getParameters();
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
        byte[] cryptoText = pbeCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        byte[] iv = ivParameterSpec.getIV();
        return base64Encode(iv) + ":" + base64Encode(cryptoText);
    }

    /**
     * decrypts the given data or encrypts it if its still plain text
     *
     * @param data given data that has to be decrypted
     */
    public String decryptData(String data) throws Exception {
        if (data.contains(":")) {
            String iv = data.split(":")[0];
            String property = data.split(":")[1];
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key key = createEncryptionKey();
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(iv)));
            return new String(cipher.doFinal(base64Decode(property)), StandardCharsets.UTF_8);
        } else {
            /*
            this case only appears when starting the application the first time with the new encryption system because
            then the password is still saved in plain text and there will be no ":" which would lead to an error
             */
            String encrypted = encrypt(data);
            stageManager.getPrefManager().saveEncryptedPassword(encrypted);
            return data;
        }
    }

    /**
     * used to encode the given bytes
     *
     * @param bytes given bytes that has to be encoded
     * @return encoded bytes as string
     */
    private String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public StageManager getStageManager() {
        return stageManager;
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
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

    public void changeUserDescription(String userId, String newDescription) {

        if (this.getLocalUser().getId().equals(userId)) {
            this.getLocalUser().setDescription(newDescription);

        }
        for (User user : getLocalUser().getUsers()) {

            if (user.getId().equals(userId)) {
                user.setDescription(newDescription);
                break;
            }
        }
        for (Server server : getLocalUser().getServers()) {
            for (User user : server.getMembers()) {
                if (user.getId().equals(userId)) {
                    user.setDescription(newDescription);
                    break;
                }
            }
        }

    }


    /**
     * converts a message link, given in the format: messageLink/ServerId/CategoryId/ChannelId/MessageId/Timestamp,
     * to array of the strings which contains messageLink, ServerId, CategoryId, ChannelId, MessageId and Timestamp.
     */
    public String[] parseReferenceMessage(String message) {
        if (message.startsWith(MESSAGE_LINK + SLASH) && message.split("/").length == 6) {
            String[] split = message.split("/");
            return split;
        }
        return null;
    }


    /**
     * removes the audiomembers of a channel of a given server
     */
    public void removeUserFromAudioChannelOfServer(Server server) {
        for (Category category: server.getCategories()) {
            for (Channel channel: category.getChannels()) {
                if (channel.getType().equals(AUDIO) && channel != getLocalUser().getAudioChannel()){
                    for (User user: channel.getAudioMembers()) {
                        channel.withoutAudioMembers(user);
                        break;
                    }
                }
            }
        }
    }
}
