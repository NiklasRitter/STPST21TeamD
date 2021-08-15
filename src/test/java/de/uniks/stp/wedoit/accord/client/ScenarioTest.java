package de.uniks.stp.wedoit.accord.client;

import de.uniks.stp.wedoit.accord.client.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class ScenarioTest {

    @Test
    public void localUserTest() {
        LocalUser localUser = new LocalUser();
        User user = new User();
        User user1 = new User();

        localUser.withGameInvites(user, user1);
        localUser.withoutGameInvites(user, user1);

        ArrayList<User> users = new ArrayList<>();

        users.add(user);
        users.add(user1);
        localUser.withGameInvites(users);
        localUser.withoutGameInvites(users);

        localUser.withGameRequests(user, user1);
        localUser.withoutGameRequests(user, user1);

        localUser.withGameRequests(users);
        localUser.withoutGameRequests(users);

        Server server = new Server();
        Server server1 = new Server();

        ArrayList<Server> servers = new ArrayList<>();
        servers.add(server);
        servers.add(server1);

        localUser.withServers(server, server1);
        localUser.withoutServers(server, server1);

        localUser.withServers(servers);
        localUser.withoutServers(servers);

        localUser.withUsers(user, user1);
        localUser.withoutUsers(user, user1);

        localUser.withUsers(users);
        localUser.withoutUsers(users);

        localUser.setDescription("description");
        localUser.setDescription("description");
        String description = localUser.getDescription();
        Assert.assertEquals(description, "description");

        localUser.setSteamGameExtraInfo("The Witcher");
        localUser.setSteamGameExtraInfo("The Witcher");
        String steamGameExtraInfo = localUser.getSteamGameExtraInfo();
        Assert.assertEquals(steamGameExtraInfo, "The Witcher");

        localUser.setSteam64ID("123");
        String s = localUser.toString();

        localUser.removeYou();

    }

    @Test
    public void UserTest() {
        User user = new User();
        Channel channel = new Channel();
        Channel channel1 = new Channel();

        user.setName("Dieter");
        user.setName("Dieter");

        user.setId("1");
        user.setId("1");

        user.setMuted(true);
        user.setMuted(true);

        user.setAudioVolume(1);
        user.setAudioVolume(1);

        ArrayList<Channel> channels = new ArrayList<>();
        channels.add(channel);
        channels.add(channel1);

        user.withChannels(channel, channel1);
        user.withoutChannels(channel, channel1);

        user.withChannels(channels);
        user.withoutChannels(channels);

        Server server = new Server();
        Server server1 = new Server();
        ArrayList<Server> servers = new ArrayList<>();

        user.withServers(server, server1);
        user.withoutServers(server, server1);

        user.withServers(servers);
        user.withoutServers(servers);

        String s = user.toString();

        user.removeYou();
    }

    @Test
    public void ServerTest() {
        Server server = new Server();
        server.setId("1");
        server.setId("1");

        Category category = new Category();
        Category category1 = new Category();
        ArrayList<Category> categories = new ArrayList<>();
        categories.add(category);
        categories.add(category1);

        server.withCategories(category, category1);
        server.withoutCategories(category, category1);

        server.withCategories(categories);
        server.withoutCategories(categories);

        User user = new User();
        User user1 = new User();
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        users.add(user1);

        server.withMembers(user, user1);
        server.withoutMembers(user, user1);

        server.withMembers(users);
        server.withoutMembers(users);

        Invitation invitation = new Invitation();
        Invitation invitation1 = new Invitation();

        server.withInvitations(invitation, invitation1);
        server.withoutInvitations(invitation, invitation1);

        String s = server.toString();

    }

    @Test
    public void PrivateMessageTest() {
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setId("1");
        privateMessage.setId("1");

        privateMessage.setTo("Me");
        privateMessage.setTo("Me");

        privateMessage.setFrom("Me");
        privateMessage.setFrom("Me");

        privateMessage.setText("Me");
        privateMessage.setText("Me");

        privateMessage.setTimestamp(1);
        privateMessage.setTimestamp(1);

        String s = privateMessage.toString();
        privateMessage.removeYou();
    }

    @Test
    public void MessageTest() {
        Message message = new Message();
        message.setId("1");
        message.setId("1");

        message.setFrom("Me");
        message.setFrom("Me");

        message.setText("Me");
        message.setText("Me");

        message.setTimestamp(1);
        message.setTimestamp(1);

        String s = message.toString();
        message.removeYou();
    }

    @Test
    public void OptionsTest() {
        Options options = new Options();
        options.setSystemVolume(10);
        options.setAudioRootMeanSquare(10);
        String s = options.toString();
        options.removeYou();
    }

    @Test
    public void InvitationsTest() {
        Invitation invitation = new Invitation();
        String s = invitation.toString();

        invitation.listeners();
        invitation.setCurrent(1);
        invitation.setCurrent(1);

        invitation.setMax(1);
        invitation.setMax(1);

        invitation.setId("1");
        invitation.setId("1");

        invitation.setType("1");
        invitation.setType("1");

        invitation.setLink("1");
        invitation.setLink("1");

    }

    @Test
    public void ChatTest() {
        Chat chat = new Chat();
        chat.setName("Test");
        chat.setName("Test");

        User user = new User();
        chat.setUser(user);

        String name = chat.getName();

        PrivateMessage privateMessage = new PrivateMessage();
        PrivateMessage privateMessage1 = new PrivateMessage();
        ArrayList<PrivateMessage> privateMessages = new ArrayList<>();

        privateMessages.add(privateMessage);
        privateMessages.add(privateMessage1);

        chat.withMessages(privateMessage, privateMessage1);
        chat.withoutMessages(privateMessage, privateMessage1);

        chat.withMessages(privateMessages);
        chat.withoutMessages(privateMessages);

        chat.withMessages(privateMessage);
        chat.withoutMessages(privateMessage);

        String s = chat.toString();
        chat.removeYou();
    }

    @Test
    public void ChannelTest() {
        Channel channel = new Channel();

        Message message = new Message();
        Message message1 = new Message();
        ArrayList<Message> messages = new ArrayList<>();

        messages.add(message);
        messages.add(message1);

        channel.withMessages(message, message1);
        channel.withoutMessages(message, message1);

        channel.withMessages(messages);
        channel.withoutMessages(messages);

        channel.withMessages(message);
        channel.withoutMessages(message);

        User user = new User();
        User user1 = new User();
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        users.add(user1);

        channel.withMembers(user, user1);
        channel.withoutMembers(user, user1);

        channel.withMembers(users);
        channel.withoutMembers(users);

        channel.withAudioMembers(user, user1);
        channel.withoutAudioMembers(user, user1);

        channel.withAudioMembers(users);
        channel.withoutAudioMembers(users);

        String s = channel.toString();
        channel.removeYou();
    }

    @Test
    public void CategoryTest() {
        Category category = new Category();

        Channel channel = new Channel();
        Channel channel1 = new Channel();
        ArrayList<Channel> channels = new ArrayList<>();

        channels.add(channel);
        channels.add(channel1);

        category.withChannels(channel, channel1);
        category.withoutChannels(channel, channel1);

        category.withChannels(channels);
        category.withoutChannels(channels);

        String s = category.toString();
    }

    @Test
    public void AccordClientTest() {
        AccordClient accordClient = new AccordClient();
        accordClient.setOptions(new Options());

        accordClient.removeYou();
    }

    @Test
    public void myFirstScenario() {
        // start typing your scenario or select an example using the dropdown above.
    }
}
