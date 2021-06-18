package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;

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
     * Build the Login JSONObject.
     *
     * @param name     The Name of the User to be logged in.
     * @param password The Password of the User to be logged in.
     * @return The JsonObject for login.
     */
    public static JsonObject buildLogin(String name, String password) {
        return Json.createObjectBuilder()
                .add(NAME, name)
                .add(PASSWORD, password)
                .build();
    }

    /**
     * Parse a given JsonObject to a Category.
     * <p>
     * Used for the Categories returned when getting server categories.
     *
     * @param categoryJson The JsonObject of the Category to be parsed.
     * @return The parsed Category.
     */
    public static Category parseCategory(JsonObject categoryJson) {
        return new Category().setId(categoryJson.getString(ID))
                .setName(categoryJson.getString(NAME));
    }

    /**
     * Parse a given JsonObject to a Channel.
     * <p>
     * Used for the Channels returned when getting category Channels.
     *
     * @param channelJson The JsonObject of the Channel to be parsed.
     * @return The parsed Channel.
     */
    public static Channel parseChannel(JsonObject channelJson) {
        return new Channel().setId(channelJson.getString(ID))
                .setName(channelJson.getString(NAME))
                .setType(channelJson.getString(TYPE))
                .setPrivileged(channelJson.getBoolean(PRIVILEGED));
    }

    /**
     * Get the MemberIDs of a given JsonObject Channel.
     * <p>
     * Used for the Channels returned when getting category Channels.
     *
     * @param channelJson The JsonObject of the Channel containing the MemberIDs.
     * @return The MemberID List.
     */
    public static List<String> parseMembers(JsonObject channelJson) {
        JsonArray members = channelJson.getJsonArray(MEMBERS);
        List<String> membersIds = new ArrayList<>();
        for (Object memberId : members) {
            membersIds.add(memberId.toString().substring(1, memberId.toString().length() - 1));
        }
        return membersIds;
    }

    /**
     * Parse given JsonArray to a List of Messages
     *
     * @param jsonMessages The JsonArray to parse
     * @return List of Messages
     */
    public static List<Message> parseMessageArray(JsonArray jsonMessages) {
        ArrayList<Message> messages = new ArrayList<>();
        for (JsonValue jsonMessage : jsonMessages) {
            Message message = new Message();
            message.setId(jsonMessage.asJsonObject().getString(ID));
            message.setText(jsonMessage.asJsonObject().getString(TEXT));
            message.setFrom(jsonMessage.asJsonObject().getString(FROM));
            String timestamp = jsonMessage.asJsonObject().get(TIMESTAMP).toString();
            message.setTimestamp(Long.parseLong(timestamp));
            messages.add(message);
        }
        return messages;
    }

    /**
     * Parse a given JsonObject to a invitation.
     * <p>
     * Used for the invitations returned when getting all invitations.
     *
     * @param invitationJson The JsonObject of the invitation to be parsed.
     * @return The parsed invitation.
     */
    public static Invitation parseInvitation(JsonObject invitationJson, Server server) {
        return new Invitation().setId(invitationJson.getString(ID))
                .setCurrent(invitationJson.getInt(CURRENT))
                .setType(invitationJson.getString(TYPE))
                .setLink(invitationJson.getString(LINK))
                .setMax(invitationJson.getInt(MAX))
                .setServer(server);
    }

    /**
     * Parse a given JsonArray to a invitation List.
     * <p>
     * Used for the invitations returned when getting all invitations.
     *
     * @param invitationsJsonArray The JsonArray of the invitations to be parsed.
     * @return The parsed invitations List.
     */
    public static List<Invitation> parseInvitations(JsonArray invitationsJsonArray, Server server) {
        List<Invitation> invitations = new ArrayList<>();
        invitationsJsonArray.forEach((jsonValue) -> invitations.add(parseInvitation(jsonValue.asJsonObject(), server)));
        return invitations;
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
                .add(CHANNEL, channelId)
                .add(MESSAGE, message)
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
                .add(CHANNEL, PRIVATE)
                .add(TO, to)
                .add(MESSAGE, message)
                .build();
    }
}
