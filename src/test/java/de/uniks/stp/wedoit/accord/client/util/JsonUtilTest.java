package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.*;
import de.uniks.stp.wedoit.accord.client.network.RestClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.*;

public class JsonUtilTest {

    private JsonUtil jsonUtil;
    private RestClient restClient;
    private Editor editor;
    private User user;
    private LocalUser localUser;
    private Server server;
    private Category category;
    private Channel channel;

    @Before
    public void initJsonUtil() {
        jsonUtil = new JsonUtil();
        restClient = new RestClient();
        editor = new Editor();
        editor.haveAccordClient();
        editor.haveLocalUser();
        server = new Server();
        category = new Category();
        channel = new Channel();
        user = new User().setName("Gelareh").setId("021");
        localUser = new LocalUser().setName("Amir").setUserKey("testKey123").setId("1364");
    }

    @Test
    public void testParse() {
        String stringJson = "{\"data\":{\"name\":\"Iran\",\"id\":\"0098\"},\"message\":\"\",\"status\":\"success\"}";

        JsonObject createServerAnswer = JsonUtil.parse(stringJson);

        Assert.assertEquals(createServerAnswer.getString(MESSAGE), "");
        Assert.assertEquals(createServerAnswer.getString(STATUS), "success");
        Assert.assertEquals(createServerAnswer.getJsonObject(DATA).getString(NAME), "Iran");
        Assert.assertEquals(createServerAnswer.getJsonObject(DATA).getString(ID), "0098");
    }

    @Test
    public void testBuildLogin() {
        JsonObject loginData = JsonUtil.buildLogin(user.getName(), "1324567890");

        Assert.assertEquals(loginData.getString(NAME), "Gelareh");
        Assert.assertEquals(loginData.getString(PASSWORD), "1324567890");
    }

    @Test
    public void testParseTempUser() {
        JsonObject tempUserJson = Json.createObjectBuilder().add("name", "Abbas").build();

        Assert.assertEquals(localUser.getName(), "Amir");

        localUser = JsonUtil.parseTempUser(tempUserJson);

        Assert.assertEquals(localUser.getName(), tempUserJson.getString(NAME));
        Assert.assertEquals(localUser.getName(), "Abbas");
    }

    @Test
    public void testParseOnlineUser() {
        JsonObject onlineUserJson = Json.createObjectBuilder().add("name", "Nahid").add("id", "21091985").build();

        user.setOnlineStatus(false);
        Assert.assertFalse(user.isOnlineStatus());

        Assert.assertEquals(user.getName(), "Gelareh");

        user = JsonUtil.parseOnlineUser(onlineUserJson);

        Assert.assertEquals(user.getName(), onlineUserJson.getString(NAME));
        Assert.assertEquals(onlineUserJson.getString(ID), user.getId());
        Assert.assertEquals(onlineUserJson.getString(ID), "21091985");
        Assert.assertTrue(user.isOnlineStatus());
    }

    /**
     * following method tests parseServers() and parseServer() methods
     */
    @Test
    public void testParseServers() {
        JsonObject serverJson = Json.createObjectBuilder().add("name", "Accord").add("id", "STP2021").build();
        JsonArray serversJsonArray = Json.createArrayBuilder().add(serverJson).build();
        List<Server> servers = JsonUtil.parseServers(serversJsonArray);

        Assert.assertEquals(servers.size(), 1);
        Assert.assertEquals(servers.get(0).getName(), serverJson.getString(NAME));
        Assert.assertEquals(servers.get(0).getName(), "Accord");
        Assert.assertEquals(servers.get(0).getId(), serverJson.getString(ID));
        Assert.assertEquals(servers.get(0).getId(), "STP2021");
    }

    @Test
    public void testParseCategory() {
        JsonObject categoryJson = Json.createObjectBuilder().add("name", "STP").add("id", "2021").build();

        Assert.assertNull(category.getName());
        Assert.assertNull(category.getId());

        category = JsonUtil.parseCategory(categoryJson);

        Assert.assertEquals(category.getName(), categoryJson.getString(NAME));
        Assert.assertEquals(category.getName(), "STP");
        Assert.assertEquals(category.getId(), categoryJson.getString(ID));
        Assert.assertEquals(category.getId(), "2021");
    }

    @Test
    public void testParseChannel() {
        JsonObject channelJson = Json.createObjectBuilder()
                .add("name", "University")
                .add("id", "FB16")
                .add("type", "text")
                .add("privileged", false).build();

        Assert.assertNull(channel.getType());
        Assert.assertNull(channel.getName());

        channel = JsonUtil.parseChannel(channelJson);

        Assert.assertEquals(channel.getName(), channelJson.getString(NAME));
        Assert.assertEquals(channel.getName(), "University");
        Assert.assertEquals(channel.getId(), channelJson.getString(ID));
        Assert.assertEquals(channel.getId(), "FB16");
        Assert.assertEquals(channel.getType(), channelJson.getString(TYPE));
        Assert.assertEquals(channel.getType(), "text");
        Assert.assertTrue(!channel.isPrivileged());
        Assert.assertEquals(channel.isPrivileged(), channelJson.getBoolean(PRIVILEGED));
    }

    @Test
    public void testParseMembers() {
        JsonObject channelJson = Json.createObjectBuilder()
                .add("name", "University")
                .add("id", "FB16")
                .add("type", "text")
                .add("privileged", false)
                .add("audioMembers", "")
                .add("members", Json.createArrayBuilder()
                        .add("60ba1d40026b3534ca6021da")
                        .add("60acf309c77d3f78988b2796")
                        .add("60ae3e9fd821a339ba4bffa8").build())
                .add("category", "3213131313131").build();

        List<String> memberIds = JsonUtil.parseMembers(channelJson);

        Assert.assertEquals(channelJson.getJsonArray(MEMBERS).size(), 3);
        Assert.assertEquals(channelJson.getJsonArray(MEMBERS).getString(0), "60ba1d40026b3534ca6021da");
        Assert.assertEquals(memberIds.get(0), channelJson.getJsonArray(MEMBERS).getString(0));
        Assert.assertEquals(memberIds.size(), channelJson.getJsonArray(MEMBERS).size());
    }

    @Test
    public void testParseMessageArray() {
        JsonObject messageJson = Json.createObjectBuilder()
                .add("channel", "741852963")
                .add("from", "Amir")
                .add("id", "0098215700554")
                .add("text", "Hello World!")
                .add("timestamp", 162386966).build();
        JsonArray messageJsonArray = Json.createArrayBuilder().add(messageJson).build();

        List<Message> messages = JsonUtil.parseMessageArray(messageJsonArray);

        Assert.assertEquals(messages.size(), messageJsonArray.size());
        Assert.assertEquals(messages.get(0).getFrom(), messageJson.getString(FROM));
        Assert.assertEquals(messages.get(0).getTimestamp(), messageJson.getInt(TIMESTAMP));
        Assert.assertEquals(messages.get(0).getText(), "Hello World!");
        Assert.assertEquals(messageJsonArray.getJsonObject(0).getString(TEXT), "Hello World!");
        Assert.assertEquals(messageJsonArray.getJsonObject(0).getString(ID), "0098215700554");
    }

    /**
     * following method tests parseInvitation() and parseInvitations() methods
     */
    @Test
    public void testParseInvitations() {
        JsonArray messageJsonArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("id", "666999888")
                        .add("current", 2)
                        .add("type", "private")
                        .add("link", "www.google.de")
                        .add("max", 100).build())
                .add(Json.createObjectBuilder()
                        .add("id", "0001111222")
                        .add("current", 5)
                        .add("type", "public")
                        .add("link", "www.uni-kassel.de")
                        .add("max", 50).build()).build();

        List<Invitation> invitations = JsonUtil.parseInvitations(messageJsonArray, server);

        Assert.assertTrue(server.getInvitations().equals(invitations));
        Assert.assertTrue(invitations.get(0).getServer().equals(server));
        Assert.assertEquals(invitations.get(0).getLink(), messageJsonArray.getJsonObject(0).getString(LINK));
    }

    @Test
    public void testBuildServerChatMessage() {
        JsonObject chatMessageJson = JsonUtil.buildServerChatMessage("Channel0098", "HelloMyChannel");

        Assert.assertTrue(JsonUtil.buildServerChatMessage("Channel0098", "HelloMyChannel") instanceof JsonObject);
        Assert.assertEquals(chatMessageJson.getString(CHANNEL), "Channel0098");
        Assert.assertEquals(chatMessageJson.getString(MESSAGE), "HelloMyChannel");
    }

    @Test
    public void testBuildPrivateChatMessage() {
        JsonObject privateChatMessageJson = JsonUtil.buildPrivateChatMessage("Ashkan", "GoStone");

        Assert.assertTrue(JsonUtil.buildPrivateChatMessage("Ashkan", "GoStone") instanceof JsonObject);
        Assert.assertEquals(privateChatMessageJson.getString(TO), "Ashkan");
        Assert.assertEquals(privateChatMessageJson.getString(MESSAGE), "GoStone");
    }

}
