package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
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
    private final Editor editor;

    private DatagramSocket audioSocket;

    public AudioConnection(LocalUser localUser, Channel channel, Editor editor) {
        this.localUser = localUser;
        this.channel = channel;
        this.editor = editor;
    }

    public void startConnection(String url, int port) {
        try {
            this.audioSocket = createSocket();
            startSendingAudio(url, port);
            startReceivingAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSendingAudio(String url, int port) {
        this.sendingThread = new AudioSend(localUser, channel, audioSocket, url, port, editor);
        this.sendingThread.start();
    }

    public void startReceivingAudio() {
        List<User> audioMembers = channel.getAudioMembers();
        ArrayList<String> connectedUser = new ArrayList<>();
        for (User member : audioMembers) {
            connectedUser.add(member.getName());
        }
        this.receivingThread = new AudioReceive(localUser, audioSocket, connectedUser);
        this.receivingThread.init();
        this.receivingThread.start();
    }

    public void close() {
        stopReceivingAudio();
        stopSendingAudio();
        audioSocket.close();
    }

    private void stopSendingAudio() {
        if (this.sendingThread != null) {
            if (this.sendingThread.isAlive()) {
                try {
                    sendingThread.setShouldSend(false);
                    sendingThread.join();
                } catch (InterruptedException e) {
                    System.err.println("Error on closing sendConnection");
                    e.printStackTrace();
                }
            }
        }
    }

    private void stopReceivingAudio() {
        if (this.receivingThread != null) {
            if (this.receivingThread.isAlive()) {
                try {
                    receivingThread.setShouldReceive(false);
                    receivingThread.terminate();
                    receivingThread.join();
                } catch (InterruptedException e) {
                    System.err.println("Error on closing receivingConnection");
                    e.printStackTrace();
                }
            }
        }
    }

    protected DatagramSocket createSocket() {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datagramSocket;
    }

    public AudioReceive getAudioReceive() {
        return receivingThread;
    }

    public AudioSend getAudioSend() {
        return sendingThread;
    }

    public Channel getChannel() {
        return this.channel;
    }
}
