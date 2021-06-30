package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import java.net.DatagramSocket;

public class AudioConnection {

    private final LocalUser localUser;
    private final Channel channel;
    private AudioSend sendingThread;
    private AudioReceive receivingThread;

    private DatagramSocket testSocket;

    public AudioConnection(LocalUser localUser, Channel channel) {
        this.localUser = localUser;
        this.channel = channel;
        //TODO localUser, channel or only names?
    }

    public void startConnection(){
        try {
            this.testSocket = new DatagramSocket(33100);
            startSendingAudio();
            startReceivingAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSendingAudio() {
        this.sendingThread = new AudioSend(localUser, channel, testSocket);
        this.sendingThread.start();
    }

    private void startReceivingAudio() {
        this.receivingThread = new AudioReceive(localUser, channel, testSocket);
        this.receivingThread.start();
    }


}
