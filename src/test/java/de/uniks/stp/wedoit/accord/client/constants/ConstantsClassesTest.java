package de.uniks.stp.wedoit.accord.client.constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testPreferences() {
        Preferences preferences = new Preferences();
        Assert.assertEquals(preferences.INITIALIZATION_VECTOR, "initialization_vector");
    }

    @Test
    public void testStages() {
        Stages stages = new Stages();
        Assert.assertEquals(stages.EMOJIPICKERSTAGE, "emojiPickerStage");
    }

    @Test
    public void testIcons() {
        List<Icons> iconsUnicodeList = Arrays.asList(Icons.values());
        Assert.assertEquals(iconsUnicodeList.get(0).toString(), "\uD83D\uDE01");
    }

}
