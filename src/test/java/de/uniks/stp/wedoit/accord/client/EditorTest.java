package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private PrivateMessage privateMessage;
    private Invitation invitation;

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
        message = new Message().setId("009821").setText("Hello World!").setFrom(user.getName());
        privateMessage = new PrivateMessage().setId("10203040").setText("This is a private message!").setFrom(user.getName());
        invitation = new Invitation();
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
        editor.haveUser(user.getId(), user.getName());
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
        category = editor.getCategoryManager().haveCategory("123", "Conversation", server);
        editor.setCurrentServer(server);

        Assert.assertTrue(server.getCategories().contains(category));
        Assert.assertEquals(editor.getCurrentServer().getCategories().size(), server.getCategories().size());
        Assert.assertEquals(server.getCategories().get(0).getName(), "Conversation");
    }

    @Test
    public void testHaveAndUpdateChannel() {
        server = editor.haveServer(localUser, "0098", "Accord");
        user = editor.haveUserWithServer(user.getName(), user.getId(), true, server);
        category = editor.getCategoryManager().haveCategory("123", "Conversation", server);
        editor.setCurrentServer(server);

        userList.add(user.getId());

        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        if (userList != null) {
            for (String userId : userList) {
                memberJson.add(Json.createValue(userId));
            }
        }

        channel = editor.getChannelManager().haveChannel("ch01", "tasks", "text", false, category, memberJson.build(), null);
        user.withChannels(channel);

        Assert.assertTrue(category.getChannels().contains(channel));
        Assert.assertTrue(server.getCategories().contains(channel.getCategory()));
        Assert.assertEquals(category.getChannels().get(0).isPrivileged(), false);
        Assert.assertTrue(channel.getMembers().contains(user));
        Assert.assertEquals(channel.getName(), "tasks");

        channel = editor.getChannelManager().updateChannel(server, "ch01", "Discussion", "text", true, category.getId(), memberJson.build(), null);

        Assert.assertNotEquals(channel.getName(), "tasks");
        Assert.assertEquals(category.getChannels().get(0).isPrivileged(), true);
        Assert.assertTrue(category.getChannels().contains(channel));
        Assert.assertTrue(server.getCategories().contains(channel.getCategory()));
    }

    @Test
    public void testUpdateChannelMessages() {
        List<Message> messages = new ArrayList<>();
        messages.add(message);

        Assert.assertTrue(!channel.getMessages().contains(message));

        editor.getMessageManager().updateChannelMessages(channel, messages);

        Assert.assertTrue(channel.getMessages().contains(message));
        Assert.assertEquals(channel.getMessages().get(0).getFrom(), "Gelareh");
        Assert.assertEquals(message.getChannel(), channel);
    }

    @Test
    public void testDeleteInvite() {
        String invitationLink = "https://ac.uniks.de/api/servers/60b0d03e026b3534ca54acf8/invites/60ca7f694445370200a52209";
        invitation.setLink(invitationLink).setId("3006");
        server.withInvitations(invitation);
        channel.setId("3006");

        Assert.assertEquals(server.getInvitations().get(0), invitation);
        Assert.assertTrue(server.getInvitations().contains(invitation));
        Assert.assertEquals(invitation.getLink(), invitationLink);
        Assert.assertEquals(invitation.getServer(), server);

        invitation = editor.deleteInvite(channel.getId(), server);

        Assert.assertNotEquals(invitation.getServer(), server);
        Assert.assertFalse(server.getInvitations().contains(invitation));
    }

    @Test
    public void calculateRMS() {
        byte[] sample = {30, 40, 2, 10, 100, 125, 97};
        Assert.assertEquals(0.010094394907355309, editor.calculateRMS(sample, 4), 0.000001);
    }

    @Test
    public void testGetMessageFormatted() {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(privateMessage.getTimestamp()));
        Assert.assertEquals(editor.getMessageManager().getMessageFormatted(privateMessage), ("[" + time + "] " + privateMessage.getFrom() + ": " + privateMessage.getText()));
        Assert.assertNotNull(editor.getMessageManager().getMessageFormatted(privateMessage));
    }

}
