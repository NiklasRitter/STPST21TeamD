package de.uniks.stp.wedoit.accord.client.constants;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ConstantsClassesTest {

    @Test
    public void testControllerNames() {
        ControllerNames controllerNames = new ControllerNames();
        Assert.assertEquals(ControllerNames.LOGIN_SCREEN_CONTROLLER, "loginScreenController");
    }

    @Test
    public void testChatMedia() {
        ChatMedia chatMedia = new ChatMedia();
        Assert.assertEquals(ChatMedia.YT_PREFIX, "https://www.youtube.com/watch?v=");
    }

    @Test
    public void testGame() {
        Game game = new Game();
        Assert.assertEquals(Game.GAME_INVITE, Game.GAME_PREFIX + "play " + Icons.HANDSHAKE);
    }

    @Test
    public void testImages() {
        Images images = new Images();
        Assert.assertEquals(Images.IMAGE_MICRO, "micro.png");
    }

    @Test
    public void testJSON() {
        JSON json = new JSON();
        Assert.assertEquals(JSON.MESSAGE, "message");
    }

    @Test
    public void testMessageOperations() {
        MessageOperations messageOperations = new MessageOperations();
        Assert.assertEquals(MessageOperations.QUOTE_MESSAGE, "###quoteMESSAGE###");
    }

    @Test
    public void testNetwork() {
        Network network = new Network();
        Assert.assertEquals(Network.AND_SERVER_ID_URL, "&" + Network.SERVER_ID_URL);
    }

    @Test
    public void testPreferences() {
        Preferences preferences = new Preferences();
        Assert.assertEquals(Preferences.INITIALIZATION_VECTOR, "initialization_vector");
    }

    @Test
    public void testStages() {
        Stages stages = new Stages();
        Assert.assertEquals(Stages.EMOJI_PICKER_STAGE, "emojiPickerStage");
    }

    @Test
    public void testIcons() {
        List<Icons> iconsUnicodeList = Arrays.asList(Icons.values());
        Assert.assertEquals(iconsUnicodeList.get(0).toString(), "\uD83D\uDE01");
    }

}
