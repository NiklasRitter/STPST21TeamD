package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import javax.json.Json;
import javax.json.JsonObject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.CHANNEL;
import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioSend extends Thread{

    private final boolean bigEndian = false;
    private final float bitRate = 48000.0f;
    private final int port = 33100;
    private final int sampleSize = 16;
    private final int channels = 1;
    private final String address = "cranberry.uniks.de";
    private MulticastSocket sendSocket;

    private final LocalUser localUser;
    private final Channel channel;
    private boolean shouldSend;

    public AudioSend(LocalUser localUser, Channel channel) {
        this.localUser = localUser;
        this.channel = channel;
    }

    @Override
    public void run() {
        byte[] metaData = createMetaData(localUser, channel);

        // default IPv6, but UPD multicasting not available at IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");

        DatagramPacket datagramPacket;

        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;

        // how java saves digital version of the audio
        AudioFormat audioFormat = new AudioFormat(encoding, this.bitRate, this.sampleSize, this.channels,
                (this.sampleSize / 8) * this.channels, this.bitRate, this.bigEndian);

        // actual mic connection - format according to audioFormat --- then send as array of bytes (packet) over network
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Data Line not supported");
        }

        try {
            // get microphone data from
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            // open thread - sample microphone
            line.open(audioFormat);
            // save it into byte array
            byte[] readData = new byte[1279];
            // copy metadata into readData
            System.arraycopy(metaData, 0, readData, 0, 255);

            InetAddress inetAddress = InetAddress.getByName(this.address);

            this.sendSocket = new MulticastSocket();
            // this.sendSocket = new DatagramSocket();
            while(true) {
                line.read(readData, 255, 1024);
                datagramPacket = new DatagramPacket(readData, readData.length, inetAddress, port);
                this.sendSocket.send(datagramPacket);

                byte[] testData = new byte[1024];
                System.arraycopy(readData, 255, testData, 0, 1024);
                System.out.println(Arrays.toString(testData));
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

    //TODO does join also kill socket connection?
}
