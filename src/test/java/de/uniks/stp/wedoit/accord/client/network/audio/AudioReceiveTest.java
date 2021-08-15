package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestFactory;

import javax.json.Json;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioReceiveTest {
    @TestFactory
    public AudioReceive generateAudioReceive(LocalUser localUser, DatagramSocket receiveSocket, ArrayList<String> connectedUser) {

        if (localUser == null) {
            localUser = new LocalUser()
                    .setName("username")
                    .withUsers(new User().setName("username2").setAudioVolume(10))
                    .setAccordClient(new AccordClient()
                            .setOptions(new Options()
                                    .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                    .setSystemVolume(100f)));
        }
        if (receiveSocket == null) {
            try {
                receiveSocket = new DatagramSocket(0);
            } catch (SocketException e) {
                Assert.fail(e.getMessage());
            }
        }
        if (connectedUser == null) {
            connectedUser = new ArrayList<String>();
            connectedUser.add("username2");
        }
        Assert.assertNotNull(localUser);
        Assert.assertNotNull(receiveSocket);
        Assert.assertNotNull(connectedUser);
        return new AudioReceive(localUser, receiveSocket, connectedUser);
    }

    @Test
    public void testRun() {
        AudioReceive audioSend = generateAudioReceive(null, null, null);
        audioSend.start();
        Assert.assertEquals(Thread.State.RUNNABLE, audioSend.getState());
        try {
            audioSend.join();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(Thread.State.TERMINATED, audioSend.getState());
    }

    @Test
    public void testSetShouldReceive() {
        AudioReceive audioSend = generateAudioReceive(null, null, null);
        audioSend.setShouldReceive(false);
    }

    @Test
    public void testGetSourceDataLineMap() {
        AudioReceive audioSend = generateAudioReceive(null, null, null);
        Assert.assertEquals(new HashMap<>(), audioSend.getSourceDataLineMap());
    }

    @Test
    public void testGetAudioSenderName() {
        AudioReceive audioSend = generateAudioReceive(null, null, null);
        Assert.assertEquals("username", audioSend.getAudioSenderName(JsonUtil.stringify(Json.createObjectBuilder().add(NAME, "username").build()).getBytes()));
    }

    @Test
    public void testPropertyChangeListener() {
        LocalUser localUser = new LocalUser()
                .setName("username")
                .withUsers(new User().setName("username2").setAudioVolume(10))
                .setAccordClient(new AccordClient()
                        .setOptions(new Options()
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setSystemVolume(100f)));
        AudioReceive audioSend = generateAudioReceive(localUser, null, null);
        audioSend.init();
        localUser.getAccordClient().getOptions().setSystemVolume(0f);
        audioSend.terminate();
    }

    @Test
    public void testGetUser() {
        String username = "username2";
        User user = new User().setName(username).setAudioVolume(10);
        LocalUser localUser = new LocalUser()
                .setName("username")
                .withUsers(user)
                .setAccordClient(new AccordClient()
                        .setOptions(new Options()
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setSystemVolume(100f)));
        AudioReceive audioSend = generateAudioReceive(localUser, null, null);
        Assert.assertEquals(user, audioSend.getUser(username));
    }

    @Test
    public void testMuteUser() {
        String username = "username2";
        LocalUser localUser = new LocalUser()
                .setName("username")
                .withUsers(new User().setName(username).setAudioVolume(10))
                .setAccordClient(new AccordClient()
                        .setOptions(new Options()
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setSystemVolume(100f)));
        AudioReceive audioSend = generateAudioReceive(localUser, null, null);
        audioSend.muteUser(username);
    }

    @Test
    public void testUnmuteUser() {
        String username = "username2";
        LocalUser localUser = new LocalUser()
                .setName("username")
                .withUsers(new User().setName(username).setAudioVolume(10))
                .setAccordClient(new AccordClient()
                        .setOptions(new Options()
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setSystemVolume(100f)));
        AudioReceive audioSend = generateAudioReceive(localUser, null, null);
        audioSend.unmuteUser(username);
    }

    @Test
    public void testCalculateVolumeUser() {
        String username = "username2";
        FloatControl floatControl = new FloatControlTest();

        LocalUser localUser = new LocalUser()
                .setName("username")
                .withUsers(new User().setName(username).setAudioVolume(10))
                .setAccordClient(new AccordClient()
                        .setOptions(new Options()
                                .setOutputDevice(AudioSystem.getMixerInfo()[0])
                                .setSystemVolume(100f)));
        AudioReceive audioSend = generateAudioReceive(localUser, null, null);
        audioSend.calculateVolume(1, floatControl, username);
        audioSend.calculateVolume(0, floatControl, username);
        audioSend.calculateVolume(-1, floatControl, username);
    }

    class FloatControlTest extends FloatControl {
        protected FloatControlTest() {
            super(Type.MASTER_GAIN,
                    -100f,
                    100f,
                    1f,
                    1,
                    0f,
                    "null",
                    "min",
                    "mid",
                    "max");
        }
    }
}
