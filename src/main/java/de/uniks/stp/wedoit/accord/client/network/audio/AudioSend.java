package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.CHANNEL;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioSend extends Thread {

    private final DatagramSocket sendSocket;

    private final LocalUser localUser;
    private final Channel channel;
    private final String address;
    private final int port;
    private final Editor editor;
    AtomicBoolean shouldSend;
    private TargetDataLine line;


    public AudioSend(LocalUser localUser, Channel channel, DatagramSocket sendSocket, String address, int port, Editor editor) {
        this.localUser = localUser;
        this.channel = channel;
        this.sendSocket = sendSocket;
        this.shouldSend = new AtomicBoolean();
        this.shouldSend.set(true);
        this.address = address;
        this.port = port;
        this.editor = editor;
    }

    @Override
    public void run() {
        byte[] metaData = createMetaData(localUser, channel);

        // default IPv6, but UPD multicasting not available at IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");

        DatagramPacket datagramPacket;

        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

        int channels = 1;
        int sampleSize = 16;
        float bitRate = 48000.0f;
        boolean bigEndian = false;
        AudioFormat audioFormat = new AudioFormat(encoding, bitRate, sampleSize, channels,
                (sampleSize / 8) * channels, bitRate, bigEndian);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            Mixer.Info inputDevice = this.localUser.getAccordClient().getOptions().getInputDevice();
            if (inputDevice != null) {
                line = (TargetDataLine) AudioSystem.getMixer(inputDevice).getLine(info);
            } else {
                line = (TargetDataLine) AudioSystem.getLine(info);
            }
            line.open(audioFormat);
            byte[] readData = new byte[1279];
            System.arraycopy(metaData, 0, readData, 0, 255);

            line.start();
            InetAddress inetAddress = InetAddress.getByName(address);

            while (shouldSend.get()) {
                int b = line.read(readData, 255, 1024);

                for (int i = 255; i < readData.length; i++) {
                    readData[i] = (byte) (readData[i] * editor.getAccordClient().getOptions().getInputVolume());
                }

                datagramPacket = new DatagramPacket(readData, readData.length, inetAddress, port);

                if (line.isRunning() && editor.calculateRMS(readData, b) > editor.getAccordClient().getOptions().getAudioRootMeanSquare()) {
                    this.sendSocket.send(datagramPacket);
                }
            }
            if (line.isRunning()) {
                line.stop();
                line.flush();
            }
            if (line.isOpen()) {
                line.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] createMetaData(LocalUser localUser, Channel channel) {
        JsonObject metaData = Json.createObjectBuilder()
                .add(CHANNEL, channel.getId())
                .add(NAME, localUser.getName())
                .build();

        byte[] metaDataByte = new byte[255];
        System.arraycopy(metaData.toString().getBytes(), 0, metaDataByte, 0, metaData.toString().getBytes().length);

        return metaDataByte;
    }

    public void setShouldSend(boolean value) {
        this.shouldSend.set(value);
    }

    public void stopSending() {
        if (line != null) {
            this.line.stop();
            this.line.flush();
        }
    }

    public void startSending() {
        if (line != null) {
            this.line.start();
        }
    }
}
