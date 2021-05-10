package de.uniks.stp.wedoit.accord.client;

public class Constants {
    // JSON Keys
    public static final String COM_STATUS = "status";
    public static final String COM_MESSAGE = "message";
    public static final String COM_DATA = "data";
    public static final String COM_ACTION = "action";
    public static final String COM_ID = "id";
    public static final String COM_NAME = "name";
    public static final String COM_PASSWORD = "password";
    public static final String COM_USER_KEY = "userKey";
    public static final String COM_OWNER = "owner";
    public static final String COM_CATEGORIES = "categories";
    public static final String COM_MEMBERS = "members";
    public static final String COM_ONLINE = "online";
    public static final String COM_SERVER = "server";
    public static final String COM_CHANNELS = "channels";
    public static final String COM_CHANNEL = "channel";
    public static final String COM_TYPE = "type";
    public static final String COM_PRIVILEGED = "privileged";
    public static final String COM_TIMESTAMP = "timestamp";
    public static final String COM_FROM = "from";
    public static final String COM_TO = "to";
    public static final String COM_TEXT = "text";

    // api routes
    public static final String REST_SERVER_URL = "https://ac.uniks.de";
    public static final String USERS_PATH = "/users";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";
    public static final String SERVER_PATH = "/servers";
    public static final String API_PREFIX = "/api";
    public static final String CHAT_PATH = "/ws/chat";
    public static final String WS_SERVER_URL = "wss://ac.uniks.de";
    public static final String SYSTEM_SOCKET_URL = WS_SERVER_URL + "/ws/system";
    public static final String CHAT_USER_URL = WS_SERVER_URL + "/ws/chat?user=";
    public static final String SERVER_ID_URL = "serverId=";
    public static final String PRIVATE_USER_CHAT_PREFIX = WS_SERVER_URL + CHAT_PATH + "?user=";

    // Preferences Keys
    public static final String COM_DARKMODE = "darkmode";
}
