package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class AudioReceive extends Thread{

    private final boolean bigEndian = false;
    private final float bitRate = 48000.0f;
    private final int port = 33100;
    private final int sampleSize = 16;
    private final int channels = 1;
    private final String address = "cranberry.uniks.de";
    private DatagramSocket receiveSocketGroup;

    private DatagramSocket testSocket;

    private final LocalUser localUser;
    private final Channel channel;

    public AudioReceive(LocalUser localUser, Channel channel, DatagramSocket testSocket) {
        this.localUser = localUser;
        this.channel = channel;
        this.testSocket = testSocket;
    }

    @Override
    public void run() {
        // audio once decoded from packet - send to speaker
        AudioInputStream audioInputStream;
        AudioFormat audioFormat;

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            // create multicast group - multiple listeners possible
            InetAddress inetAddress = InetAddress.getByName(this.address);
            // this.receiveSocketGroup = new MulticastSocket(this.port);
            // this.receiveSocketGroup.joinGroup(inetAddress);
            // this.receiveSocketGroup = new DatagramSocket(this.port);

            byte[] receiveData = new byte[1279]; //1024? (or 4096, 1024)

            // how java saves digital version of the audio
            audioFormat = new AudioFormat(this.bitRate, this.sampleSize, this.channels, true, this.bigEndian);

            // datalines to connect to speakers and play sound from them (converting data into sound)
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);

            // own source data line for every user?
            // get metadata for every user - into map

            sourceDataLine.start();

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());

            while(true){
                // blocking call - will not precede until received packet
                // this.receiveSocketGroup.receive(receivePacket);
                this.testSocket.receive(receivePacket);
                audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, receivePacket.getLength());

                byte[] receivedAudio = new byte[1024];

                System.arraycopy(receivePacket.getData(), 255, receivedAudio, 0, 1024);

                toSpeaker(receivedAudio, sourceDataLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toSpeaker (byte[] soundBytes, SourceDataLine sourceDataLine) {
        try {
            //System.out.println(Arrays.toString(soundBytes));
            sourceDataLine.write(soundBytes, 0, soundBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
