package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Category;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.Server;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class ChannelManagerTest {

    private ChannelManager channelManager;
    private Editor editor;
    private Category category;
    private Server server;
    private Channel channel;

    @Before
    public void ChannelManager() {
        editor = new Editor();
        server = new Server();
        channelManager = new ChannelManager(editor);
        channel = new Channel();
        category = new Category();
    }

    @Test
    public void testHaveChannel() {
        category = editor.getCategoryManager().haveCategory("123", "Conversation", server);

        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        memberJson.add(Json.createValue("0098021"));

        JsonArrayBuilder audioMemberJson = Json.createArrayBuilder();
        audioMemberJson.add(Json.createValue("9876543210"));

        Assert.assertFalse(category.getChannels().contains(channel));

        channel = channelManager.haveChannel("021", "Channel One", "text", false, category, memberJson.build(), audioMemberJson.build());

        Assert.assertEquals(channel.getCategory(), category);
        Assert.assertTrue(category.getChannels().contains(channel));
        Assert.assertEquals(channel.getCategory().getName(), "Conversation");
    }

    @Test
    public void testUpdateChannel() {
        testHaveChannel();

        Assert.assertEquals(channel.getCategory().getId(), "123");

        JsonArrayBuilder memberJson = Json.createArrayBuilder();
        memberJson.add(Json.createValue("666999555"));

        JsonArrayBuilder audioMemberJson = Json.createArrayBuilder();
        audioMemberJson.add(Json.createValue("123456123"));

        category = editor.getCategoryManager().haveCategory("741852963", "Conversation", server);

        channel = channelManager.updateChannel(server, "2109", "STP", "audio", true, category.getId(), memberJson.build(), audioMemberJson.build());

        Assert.assertEquals(channel.getCategory().getId(), "741852963");
        Assert.assertEquals(channel.getName(), "STP");
        Assert.assertEquals(channel.getType(), "audio");
    }

    @Test
    public void testHaveChannels() {
        category = editor.getCategoryManager().haveCategory("3423337213", "Conversation", server);

        JsonObject channelJson = Json.createObjectBuilder()
                .add("name", "Discussion")
                .add("id", "FB16")
                .add("type", "text")
                .add("privileged", false)
                .add("audioMembers", Json.createArrayBuilder()
                        .add("888855552222").build())
                .add("members", Json.createArrayBuilder()
                        .add("230919900098021").build())
                .add("category", category.getId()).build();

        JsonArray channelJsonArray = Json.createArrayBuilder().add(channelJson).build();

        channelManager.haveChannels(category, channelJsonArray);

        channel = category.getChannels().get(0);

        Assert.assertEquals(channel.getName(), "Discussion");
        Assert.assertEquals(channel.getId(), "FB16");
        Assert.assertEquals(channel.getCategory(), category);
    }

    @Test
    public void testGetChannel() {
        Assert.assertNull(channelManager.getChannel(channel.getId(), category));

        testHaveChannels();

        channel = channelManager.getChannel(channel.getId(), category);

        Assert.assertEquals(channel.getName(), "Discussion");
        Assert.assertEquals(channel.getId(), "FB16");
        Assert.assertEquals(channel.getCategory(), category);
    }

}
