package de.uniks.stp.wedoit.accord.client.constants;

import org.junit.Assert;
import org.junit.Test;

public class ConstantsClassesTest {

    @Test
    public void testControllerNames() {
        ControllerNames controllerNames = new ControllerNames();
        Assert.assertEquals(controllerNames.LOGIN_SCREEN_CONTROLLER, "loginScreenController");
    }

    @Test
    public void testChatMedia() {
        ChatMedia chatMedia = new ChatMedia();
        Assert.assertEquals(chatMedia.YT_PREFIX, "https://www.youtube.com/watch?v=");
    }

    @Test
    public void testGame() {
        Game game = new Game();
        Assert.assertEquals(game.GAME_INVITE, game.GAME_PREFIX + "Invites you to Rock - Paper - Scissors!");
    }

    @Test
    public void testImages() {
        Images images = new Images();
        Assert.assertEquals(images.IMAGE_MICRO, "micro.png");
    }

    @Test
    public void testJSON() {
        JSON json = new JSON();
        Assert.assertEquals(json.MESSAGE, "message");
    }

    @Test
    public void testMessageOperations() {
        MessageOperations messageOperations = new MessageOperations();
        Assert.assertEquals(messageOperations.QUOTE_MESSAGE, "###quoteMESSAGE###");
    }

    @Test
    public void testNetwork() {
        Network network = new Network();
        Assert.assertEquals(network.AND_SERVER_ID_URL, "&" + network.SERVER_ID_URL);
    }

}
