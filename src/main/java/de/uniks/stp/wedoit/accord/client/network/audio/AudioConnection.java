package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

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
        List<User> audioMembers = channel.getAudioMembers();
        ArrayList<String> connectedUser = new ArrayList<>();
        for (User member: audioMembers) {
            connectedUser.add(member.getName());
        }
        this.receivingThread = new AudioReceive(localUser, channel, testSocket, connectedUser);
        this.receivingThread.start();
    }


}
