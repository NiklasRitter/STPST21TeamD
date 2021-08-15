package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;

import javax.json.Json;
import javax.sound.sampled.Mixer;

public class AudioConnectionTest {
    @TestFactory
    public AudioConnection generateAudioConnection(LocalUser localUser, Channel channel, Editor editor) {
        if (editor == null) {
            editor = new Editor();
            if (localUser == null)
                editor.haveAccordClient().setOptions(new Options().setInputDevice(new TestMixerInfo("test"))
                        .setOutputDevice(new TestMixerInfo("test"))
                        .setInputVolume(1f).setSystemVolume(100f));
            else if (localUser != null && localUser.getAccordClient() == null)
                editor.haveAccordClient().setOptions(new Options().setInputDevice(new TestMixerInfo("test"))
                        .setOutputDevice(new TestMixerInfo("test"))
                        .setInputVolume(1f).setSystemVolume(100f)).setLocalUser(localUser);
            else editor.setAccordClient(localUser.getAccordClient());

        }
        if (localUser == null) {
            localUser = editor.haveLocalUser("username", "userKey")
                    .setAccordClient(editor.getAccordClient()
                            .setOptions(new Options().setInputDevice(new TestMixerInfo("test"))
                                    .setOutputDevice(new TestMixerInfo("test"))
                                    .setInputVolume(1f).setSystemVolume(100f)));
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
                        .setOptions(new Options().setInputDevice(new TestMixerInfo("test"))
                                .setOutputDevice(new TestMixerInfo("test"))
                                .setInputVolume(1f).setSystemVolume(100f)));
        AudioConnection audioConnection = generateAudioConnection(localUser, null, null);
        Assert.assertNull(audioConnection.getAudioSend());
        Assert.assertNull(audioConnection.getAudioReceive());
        Assert.assertNotNull(audioConnection.getChannel());
        audioConnection.startSendingAudio("localhost", 0);
        audioConnection.startReceivingAudio();
        localUser.getAccordClient().getOptions().setInputDevice(new TestMixerInfo("test1"));
        localUser.getAccordClient().getOptions().setOutputDevice(new TestMixerInfo("test1"));
        Assert.assertNotNull(audioConnection.createSocket());
        audioConnection.stop();
    }

    @Test
    public void testStartingConnection() {
        AudioConnection audioConnection = generateAudioConnection(null, null, null);
        audioConnection.startConnection("localhost", 0);
        Assert.assertNotNull(audioConnection.getAudioSend());
        Assert.assertNotNull(audioConnection.getAudioReceive());
    }

    class TestMixerInfo extends Mixer.Info {
        protected TestMixerInfo(String strings) {
            super(strings, strings, strings, strings);
        }
    }
}
