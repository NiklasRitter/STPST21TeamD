package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;

import javax.json.Json;
import javax.sound.sampled.AudioSystem;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioConnectionTest {
    @TestFactory
    public AudioConnection generateAudioConnection(LocalUser localUser, Channel channel, Editor editor) {
        if (editor == null) {
            editor = new Editor();
            if (localUser == null || localUser.getAccordClient() == null) editor.haveAccordClient();
            else if (localUser != null && localUser.getAccordClient() == null)
                editor.getAccordClient().setLocalUser(localUser);
            else editor.setAccordClient(localUser.getAccordClient());

        }
        if (localUser == null) {
            localUser = editor.haveLocalUser("username", "userKey");
        }
        if (channel == null) {
            channel = editor.getChannelManager().haveChannel("channelID", "channelName", "text", false,
                    editor.getCategoryManager().haveCategory("categoryID", "categoryName",
                            editor.haveServer(localUser, "serverID", "serverName")),
                    Json.createArrayBuilder().add(localUser.getName()).add("username2").build(),
                    Json.createArrayBuilder().add(localUser.getName()).add("username2").build());
        }
        Assert.assertNotNull(localUser);
        Assert.assertNotNull(channel);
        Assert.assertNotNull(editor);
        return new AudioConnection(localUser, channel, editor);
    }

    @Test
    public void testConnection() {
        LocalUser localUser = new LocalUser().setName("username").setUserKey("userKey")
                .setAccordClient(new AccordClient()
                        .setOptions(new Options().setInputDevice(AudioSystem.getMixerInfo()[0])
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setInputVolume(1f).setSystemVolume(100f)));
        System.out.println("This");
        AudioConnection audioConnection = generateAudioConnection(localUser, null, null);
        System.out.println("Should");
        Assert.assertNull(audioConnection.getAudioSend());
        System.out.println("Totally");
        Assert.assertNull(audioConnection.getAudioReceive());
        System.out.println("Work");
        Assert.assertNotNull(audioConnection.getChannel());
        System.out.println("Why");
        audioConnection.startConnection("localhost", 0);
        System.out.println("Doesn't");
        Assert.assertNotNull(audioConnection.getAudioSend());
        System.out.println("It");
        Assert.assertNotNull(audioConnection.getAudioReceive());
        System.out.println("Terminate");
        try {
            System.out.println(AudioSystem.getMixerInfo()[1]);
            localUser.getAccordClient().getOptions().setInputDevice(AudioSystem.getMixerInfo()[1]);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("There");
        try {
            localUser.getAccordClient().getOptions().setOutputDevice(AudioSystem.getMixerInfo()[1]);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Is");
        String username = "name";
        System.out.println("No");
        Assert.assertEquals(audioConnection.getAudioReceive().getAudioSenderName(JsonUtil.stringify(Json.createObjectBuilder().add(NAME, username).build()).getBytes()), "username");
        System.out.println("Reason");
        audioConnection.close();
        System.out.println("!!!");
    }
}
