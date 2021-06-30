package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

public class AudioConnection {

    private final LocalUser localUser;
    private final Channel channel;
    private AudioSend sendingThread;
    private AudioReceive receivingThread;

    public AudioConnection(LocalUser localUser, Channel channel) {
        this.localUser = localUser;
        this.channel = channel;
        //TODO localUser, channel or only names?
    }

    public void startConnection(){
        // startSendingAudio();
        startReceivingAudio();
    }

    private void startSendingAudio() {
        this.sendingThread = new AudioSend(localUser, channel);
        this.sendingThread.start();
    }

    private void startReceivingAudio() {
        this.receivingThread = new AudioReceive(localUser, channel);
        this.receivingThread.start();
    }


}
