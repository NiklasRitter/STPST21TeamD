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
}
