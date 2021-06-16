package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EditorTest {

    private Editor editor;
    private LocalUser localUser;
    private Server server;
    private User user;
    private Category category;
    private Channel channel;
    private List<String> userList;
    private Message message;

    @Before
    public void initEditor() {
        editor = new Editor();
        editor.haveAccordClient();
        editor.haveLocalUser();
        server = new Server();
        localUser = new LocalUser().setName("Amir").setUserKey("testKey123").setId("1364");
        user = new User().setName("Gelareh").setId("021");
        channel = new Channel();
        category = new Category();
        userList = new LinkedList<>();
        message = new Message();
    }

    @Test
    public void testHaveLocalUser() {
        localUser = editor.haveLocalUser(localUser.getName(), localUser.getUserKey());

        Assert.assertEquals(editor.getLocalUser().getId(), localUser.getId());
        Assert.assertEquals(editor.getLocalUser().getUserKey(), "testKey123");
        Assert.assertEquals(editor.getLocalUser().getName(), "Amir");
    }

    @Test
    public void testHaveServer() {
        server = editor.haveServer(localUser, "0098", "Accord");
        editor.setCurrentServer(server);

        Assert.assertNotNull(editor.getCurrentServer());
        Assert.assertEquals(server.getName(), "Accord");
        Assert.assertEquals(server, editor.getCurrentServer());
        Assert.assertTrue(localUser.getServers().contains(server));
    }

    @Test
    public void testHaveUserWithServer() {
        user = editor.haveUserWithServer(user.getName(), user.getId(), true, server);

        Assert.assertEquals(server.getName(), user.getServers().get(0).getName());
        Assert.assertEquals(server.getMembers().get(0).getName(), user.getName());
        Assert.assertTrue(server.getMembers().contains(user));
        Assert.assertTrue(server.getMembers().get(0).isOnlineStatus());

    }

    @Test
    public void testHaveUser() {
        localUser = editor.haveUser(user.getId(), user.getName());
        localUser.withUsers(user);

        Assert.assertEquals(localUser.getUsers().get(0).getName(), user.getName());
        Assert.assertEquals(localUser.getUsers().get(0).getName(), "Gelareh");
        Assert.assertTrue(localUser.getUsers().contains(user));
    }

    @Test
    public void testGetServerUserById() {
        Assert.assertNull(editor.getServerUserById(server, "021"));

        user.withServers(server);
        localUser.withUsers(user);

        Assert.assertEquals(user, editor.getServerUserById(server, "021"));
        Assert.assertTrue(localUser.getUsers().contains(editor.getServerUserById(server, "021")));
        Assert.assertEquals(user.getChannels(), editor.getServerUserById(server, "021").getChannels());
    }

    @Test
    public void testUserLeft() {
        user.setOnlineStatus(true);
        editor.getLocalUser().withUsers(user);
        editor.userLeft(user.getId());

        Assert.assertTrue(!user.isOnlineStatus());
        Assert.assertEquals(editor.getLocalUser(), user.getLocalUser());
    }

    @Test
    public void testGetOnlineUsers() {
        editor.getLocalUser().withUsers(user);

        Assert.assertEquals(editor.getOnlineUsers().size(), 0);
        Assert.assertFalse(editor.getOnlineUsers().contains(user));

        user.setOnlineStatus(true);

        Assert.assertEquals(editor.getOnlineUsers().size(), 1);
        Assert.assertTrue(editor.getOnlineUsers().contains(user));
    }

    @Test
    public void testHaveCategory() {
        category = editor.haveCategory("123", "Conversation", server);
        editor.setCurrentServer(server);

        Assert.assertTrue(server.getCategories().contains(category));
        Assert.assertEquals(editor.getCurrentServer().getCategories().size(), server.getCategories().size());
        Assert.assertEquals(server.getCategories().get(0).getName(), "Conversation");
    }

    @Test
    public void testHaveAndUpdateChannel() {
        server = editor.haveServer(localUser, "0098", "Accord");
        user = editor.haveUserWithServer(user.getName(), user.getId(), true, server);
        category = editor.haveCategory("123", "Conversation", server);
        editor.setCurrentServer(server);

        userList.add(user.getId());

        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        if (userList != null) {
            for (String userId : userList) {
                memberJson.add(Json.createValue(userId));
            }
        }

        channel = editor.haveChannel("ch01", "tasks", "text", false, category, memberJson.build());
        user.withChannels(channel);
        System.out.println(channel.getName());
        System.out.println(category.getId());

        Assert.assertTrue(category.getChannels().contains(channel));
        Assert.assertTrue(server.getCategories().contains(channel.getCategory()));
        Assert.assertEquals(category.getChannels().get(0).isPrivileged(), false);
        Assert.assertTrue(channel.getMembers().contains(user));
        Assert.assertEquals(channel.getName(), "tasks");

        channel = editor.updateChannel(server, "ch01", "Discussion", "text", true, category.getId(), memberJson.build());

        Assert.assertNotEquals(channel.getName(), "tasks");
        Assert.assertEquals(category.getChannels().get(0).isPrivileged(), true);
        Assert.assertTrue(category.getChannels().contains(channel));
        Assert.assertTrue(server.getCategories().contains(channel.getCategory()));
    }

    @Test
    public void testUpdateChannelMessages() {
        message = new Message().setId("009821").setText("Hello World!").setFrom(user.getName());
        List<Message> messages = new ArrayList<>();
        messages.add(message);

        Assert.assertTrue(!channel.getMessages().contains(message));

        editor.updateChannelMessages(channel, messages);

        Assert.assertTrue(channel.getMessages().contains(message));
        Assert.assertEquals(channel.getMessages().get(0).getFrom(), "Gelareh");
        Assert.assertEquals(message.getChannel(), channel);
    }

    @Test
    public void testLeaveServer() {
        server = editor.haveServer(localUser, "0098", "Accord");
        localUser = editor.haveLocalUser(localUser.getName(), localUser.getUserKey());
        server.setLocalUser(localUser);

        Assert.assertTrue(localUser.getServers().contains(server));
        Assert.assertEquals(server.getLocalUser(), localUser);

        editor.leaveServer(localUser.getUserKey(), server);

        Assert.assertEquals(server.getLocalUser(), null);
        Assert.assertFalse(localUser.getServers().contains(server));
    }

}
