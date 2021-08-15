package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class AudioConnection {

    private final LocalUser localUser;
    private final Channel channel;
    private final Editor editor;
    private AudioSend sendingThread;
    private AudioReceive receivingThread;
    private DatagramSocket audioSocket;
    private String url;
    private int port;
    private PropertyChangeListener outputDeviceListener = this::handleOutputDeviceChange;
    private PropertyChangeListener inputDeviceListener = this::handleInputDeviceChange;

    public AudioConnection(LocalUser localUser, Channel channel, Editor editor) {
        this.localUser = localUser;
        this.channel = channel;
        this.editor = editor;
        this.localUser.getAccordClient().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_OUTPUT_DEVICE, outputDeviceListener);
        this.localUser.getAccordClient().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_INPUT_DEVICE, inputDeviceListener);
    }

    public void startConnection(String url, int port) {
        try {
            this.url = url;
            this.port = port;
            this.audioSocket = createSocket();
            startSendingAudio(url, port);
            startReceivingAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSendingAudio(String url, int port) {
        if (audioSocket != null) {
            this.sendingThread = new AudioSend(localUser, channel, audioSocket, url, port, editor);
            this.sendingThread.start();
        }
    }

    public void startReceivingAudio() {
        if (audioSocket != null) {
            List<User> audioMembers = channel.getAudioMembers();
            ArrayList<String> connectedUser = new ArrayList<>();
            for (User member : audioMembers) {
                connectedUser.add(member.getName());
            }
            this.receivingThread = new AudioReceive(localUser, audioSocket, connectedUser);
            this.receivingThread.init();
            this.receivingThread.start();
        }
    }

    public void stop() {
        this.localUser.getAccordClient().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_OUTPUT_DEVICE, outputDeviceListener);
        this.localUser.getAccordClient().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_OUTPUT_DEVICE, inputDeviceListener);
        this.outputDeviceListener = null;
        this.inputDeviceListener = null;
        stopReceivingAudio();
        stopSendingAudio();
        if (audioSocket != null) audioSocket.close();
        audioSocket = null;
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

    private void handleOutputDeviceChange(PropertyChangeEvent propertyChangeEvent) {
        stopReceivingAudio();
        startReceivingAudio();
    }

    private void handleInputDeviceChange(PropertyChangeEvent propertyChangeEvent) {
        stopSendingAudio();
        startSendingAudio(url, port);
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
