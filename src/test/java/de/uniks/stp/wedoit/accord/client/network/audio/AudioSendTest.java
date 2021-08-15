package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;

import javax.json.Json;
import javax.sound.sampled.AudioSystem;
import java.net.DatagramSocket;
import java.net.SocketException;

public class AudioSendTest {
    @TestFactory
    public AudioSend generateAudioSend(LocalUser localUser, Channel channel, DatagramSocket sendSocket, String address, int port, Editor editor) {
        if (editor == null) {
            editor = new Editor();
            if (localUser == null) editor.haveAccordClient().setOptions(new Options()
                    .setInputDevice(AudioSystem.getMixerInfo()[0]).setInputVolume(1f));
            else if (localUser != null && localUser.getAccordClient() == null)
                editor.haveAccordClient().setOptions(new Options()
                        .setInputDevice(AudioSystem.getMixerInfo()[0]).setInputVolume(1f)).setLocalUser(localUser);
            else editor.setAccordClient(localUser.getAccordClient());

        }
        if (localUser == null) {
            localUser = editor.haveLocalUser("username", "userKey")
                    .setAccordClient(editor.getAccordClient().setOptions(new Options()
                            .setInputDevice(AudioSystem.getMixerInfo()[0]).setInputVolume(1f)));
        }
        if (channel == null) {
            channel = editor.getChannelManager().haveChannel("channelID", "channelName", "text", false,
                    editor.getCategoryManager().haveCategory("categoryID", "categoryName",
                            editor.haveServer(localUser, "serverID", "serverName")),
                    Json.createArrayBuilder().add(localUser.getName()).add("username2").build(),
                    Json.createArrayBuilder().add(localUser.getName()).add("username2").build());
        }
        if (sendSocket == null) {
            try {
                sendSocket = new DatagramSocket(port);
            } catch (SocketException e) {
                Assert.fail(e.getMessage());
            }
        }
        if (address == null) {
            address = "localhost";
        }
        Assert.assertNotNull(localUser);
        Assert.assertNotNull(channel);
        Assert.assertNotNull(sendSocket);
        Assert.assertNotNull(address);
        Assert.assertNotNull(editor);
        return new AudioSend(localUser, channel, sendSocket, address, port, editor);
    }

    @Test
    public void testRun() {
        AudioSend audioSend = generateAudioSend(null, null, null, null, 0, null);
        audioSend.start();
        Assert.assertEquals(Thread.State.RUNNABLE, audioSend.getState());
        audioSend.stopSending();
        Assert.assertEquals(Thread.State.RUNNABLE, audioSend.getState());
        audioSend.startSending();
        Assert.assertEquals(Thread.State.RUNNABLE, audioSend.getState());
        try {
            audioSend.join();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(Thread.State.TERMINATED, audioSend.getState());
    }

    @Test
    public void testSetShouldSend() {
        AudioSend audioSend = generateAudioSend(null, null, null, null, 0, null);
        audioSend.setShouldSend(false);
    }
}
