package de.uniks.stp.wedoit.accord.client.controller;

import de.uniks.stp.wedoit.accord.client.Editor;

import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Server;
import org.junit.*;

public class EditorTest {

    private Editor editor;
    private LocalUser localUser;
    private Server server;
    @Before
    public void initEditor() {
        editor = new Editor();
        editor.haveAccordClient();
        editor.haveLocalUser();
        localUser = new LocalUser();
        server = new Server();
    }

    @Test
    public void testHaveLocalUser() {

        localUser = editor.haveLocalUser("Amir", "testKey123");

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
    }

}
