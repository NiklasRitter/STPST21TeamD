package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;

import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import org.junit.*;

public class EditorTest {

    private Editor editor;
    @Before
    public void initEditor() {
        editor = new Editor();
    }

    @Test
    public void testHaveLocalUser() {
        editor.haveAccordClient();
        editor.haveLocalUser();

        LocalUser localUser = editor.haveLocalUser("Amir", "testKey123");

        Assert.assertEquals(editor.getLocalUser().getId(), localUser.getId());
        Assert.assertEquals(editor.getLocalUser().getUserKey(), "testKey123");
        Assert.assertEquals(editor.getLocalUser().getName(), "Amir");
    }

}
