package de.uniks.stp.wedoit.accord.client.constants;

/**
 * includes constants to handle operations concerning Network, means rest and websocket connection.
 */
public class Network {
    public static final String REST_SERVER_URL = "https://ac.uniks.de";
    public static final String USERS_PATH = "/users";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";
    public static final String SERVER_PATH = "/servers";
    public static final String API_PREFIX = "/api";

    public static final String SLASH = "/";
    public static final String QUESTION_MARK = "?";
    public static final String EQUALS = "=";
    public static final String CHAT_PATH = "/ws/chat";

    public static final String WS_SERVER_URL = "wss://ac.uniks.de";
    public static final String SYSTEM_SOCKET_URL = WS_SERVER_URL + "/ws/system";
    public static final String WS_SERVER_ID_URL = "/ws/system?serverId=";
    public static final String CHAT_USER_URL = WS_SERVER_URL + "/ws/chat?user=";
    public static final String SERVER_ID_URL = "serverId=";
    public static final String AND_SERVER_ID_URL = "&" + SERVER_ID_URL;
    public static final String CATEGORIES = "/categories";
    public static final String CHANNELS = "/channels";
    public static final String MESSAGES = "/messages";
    public static final String INVITES = "/invites";
    public static final String LEAVE_SERVER = "/leave";
    public static final String SERVERSWITHOUTSLASH = "SERVERS";
    public static final String PRIVATE_USER_CHAT_PREFIX = WS_SERVER_URL + CHAT_PATH + "?user=";

}
