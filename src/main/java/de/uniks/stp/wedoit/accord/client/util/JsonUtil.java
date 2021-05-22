package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.Constants.*;

public class JsonUtil {
    /**
     * Parse given stringified JSON to JsonObject.
     *
     * @param json The stringified JSON to be parsed.
     * @return The parsed JsonObject.
     */
    public static JsonObject parse(String json) {
        return Json.createReader(new StringReader(json)).readObject();
    }

    /**
     * Stringify the given JsonObject.
     *
     * @param object The JsonObject to be stringified.
     * @return The stringified JSON.
     */
    public static String stringify(JsonObject object) {
        return object.toString();
    }

    /**
     * Build the Login JSONObject.
     *
     * @param name     The Name of the User to be logged in.
     * @param password The Password of the User to be logged in.
     * @return The JsonObject for login.
     */
    public static JsonObject buildLogin(String name, String password) {
        return Json.createObjectBuilder()
                .add(COM_NAME, name)
                .add(COM_PASSWORD, password)
                .build();
    }

    /**
     * Parse a given JsonObject to a LocalUser.
     *
     * @param tempUserJson The JsonObject of the LocalUser parsed.
     * @return The parsed LocalUser.
     */
    public static LocalUser parseTempUser(JsonObject tempUserJson) {
        return new LocalUser().setName(tempUserJson.getString(COM_NAME));
    }

    /**
     * Parse a given JsonObject to a User.
     * <p>
     * Used for the Users returned when getting the online users.
     *
     * @param onlineUserJson The JsonObject of the User to be parsed.
     * @return The parsed User.
     */
    public static User parseOnlineUser(JsonObject onlineUserJson) {
        return new User().setId(onlineUserJson.getString(COM_ID))
                .setName(onlineUserJson.getString(COM_NAME))
                .setOnlineStatus(true);
    }

    /**
     * Parse a given JsonObject to a User.
     * <p>
     * Used for the Users returned when getting the server users.
     *
     * @param serverUserJson The JsonObject of the User to be parsed.
     * @return The parsed User.
     */
    public static User parseServerUser(JsonObject serverUserJson) {
        return parseOnlineUser(serverUserJson).setOnlineStatus(serverUserJson.getBoolean(COM_ONLINE));
    }


    /**
     * Parse a given JsonArray to a User List.
     * <p>
     * Used for the Users returned when getting the server users.
     *
     * @param serverUsersJsonArray The JsonArray of the Users to be parsed.
     * @return The parsed User List.
     */
    public static List<User> parseServerUsers(JsonArray serverUsersJsonArray) {
        List<User> users = new ArrayList<>();
        serverUsersJsonArray.forEach((jsonValue) -> users.add(parseServerUser(jsonValue.asJsonObject())));
        return users;
    }

    /**
     * Parse a given JsonObject to a Server.
     * <p>
     * Used for the Servers returned when getting all servers.
     *
     * @param serverJson The JsonObject of the Server to be parsed.
     * @return The parsed Server.
     */
    public static Server parseServer(JsonObject serverJson) {
        return new Server().setId(serverJson.getString(COM_ID))
                .setName(serverJson.getString(COM_NAME));
    }

    /**
     * Parse a given JsonArray to a Server List.
     * <p>
     * Used for the Servers returned when getting all servers.
     *
     * @param serversJsonArray The JsonArray of the Servers to be parsed.
     * @return The parsed Server List.
     */
    public static List<Server> parseServers(JsonArray serversJsonArray) {
        List<Server> servers = new ArrayList<>();
        serversJsonArray.forEach((jsonValue) -> servers.add(parseServer(jsonValue.asJsonObject())));
        return servers;
    }

    /**
     * Parse a given JsonObject to a User.
     * <p>
     * Used for the Servers returned when getting server explicit Information.
     *
     * @param serverDetailsJson The JsonObject of the Server to be parsed.
     * @return The parsed Server.
     */
    public static Server parseServerDetails(JsonObject serverDetailsJson) {
        JsonArray categoriesJson = serverDetailsJson.getJsonArray(COM_CATEGORIES);
        List<Category> categories = new ArrayList<>();
        categoriesJson.forEach((jsonValue) -> categories.add(new Category().setId(jsonValue.toString())));
        List<User> members = parseServerUsers(serverDetailsJson.getJsonArray(COM_MEMBERS));
        return parseServer(serverDetailsJson)
                .withCategories(categories)
                .withMembers(members)
                .setOwner(serverDetailsJson.getString(COM_OWNER));
    }

    /**
     * Parse a given JsonObject to a Category.
     * <p>
     * Used for the Categories returned when getting server categories.
     *
     * @param categoryJson The JsonObject of the Category to be parsed.
     * @return The parsed Category.
     */
    public static Category parseCategory(JSONObject categoryJson) {
        return new Category().setId(categoryJson.getString(COM_ID))
                .setName(categoryJson.getString(COM_NAME));
    }

    /**
     * Parse a given JsonObject to a Channel.
     * <p>
     * Used for the Channels returned when getting category Channels.
     *
     * @param channelJson The JsonObject of the Channel to be parsed.
     * @return The parsed Channel.
     */
    public static Channel parseChannel(JSONObject channelJson) {
        return new Channel().setId(channelJson.getString(COM_ID))
                .setName(channelJson.getString(COM_NAME))
                .setType(channelJson.getString(COM_TYPE))
                .setPrivileged(channelJson.getBoolean(COM_PRIVILEGED));
    }

    /**
     * Get the MemberIDs of a given JsonObject Channel.
     * <p>
     * Used for the Channels returned when getting category Channels.
     *
     * @param channelJson The JsonObject of the Channel containing the MemberIDs.
     * @return The MemberID List.
     */
    public static List<String> parseMembers(JSONObject channelJson) {
        JSONArray members = channelJson.getJSONArray(COM_MEMBERS);
        List<String> membersIds = new ArrayList<>();
        for (Object memberId : members) {
            membersIds.add(memberId.toString());
        }
        return membersIds;
    }


    /**
     * Build the Server Chat Message JSONObject.
     *
     * @param channelId The ID of the Channel this Message is send in.
     * @param message   The Message to be send.
     * @return The JsonObject for serverChatMessage.
     */
    public static JsonObject buildServerChatMessage(String channelId, String message) {
        return Json.createObjectBuilder()
                .add(COM_CHANNEL, channelId)
                .add(COM_MESSAGE, message)
                .build();
    }

    /**
     * Build the Private Chat Message JSONObject.
     *
     * @param to      The Name of the User the Message should be sent to.
     * @param message The Message to be send.
     * @return The JsonObject for privateChatMessage.
     */
    public static JsonObject buildPrivateChatMessage(String to, String message) {
        return Json.createObjectBuilder()
                .add(COM_CHANNEL, "private")
                .add(COM_TO, to)
                .add(COM_MESSAGE, message)
                .build();
    }
}
