package de.uniks.stp.wedoit.accord.client.network;

import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class AudioStream {

    private final boolean bigEndian = false;
    private final float bitRate = 48000.0f;
    private final int port = 33100;
    private final int sampleSize = 16;
    private final int channels = 1;
    private final String address = "cranberry.uniks.de";
    private MulticastSocket sendSocket;
    private MulticastSocket receiveSocketGroup;

    public void connecting() {

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
            byte[] data = new byte[1279]; //1024?

            InetAddress inetAddress = InetAddress.getByName(this.address);
            // ? DatagramSocket
            this.sendSocket = new MulticastSocket();
            while (true) {
                line.read(data, 0, data.length);
                datagramPacket = new DatagramPacket(data, data.length, inetAddress, port);
                this.sendSocket.send(datagramPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public void receiving() {
        // audio once decoded from packet - send to speaker
        AudioInputStream audioInputStream;
        AudioFormat audioFormat;

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            // create multicast group - multiple listeners possible
            InetAddress inetAddress = InetAddress.getByName(this.address);
            this.receiveSocketGroup = new MulticastSocket(this.port);
            this.receiveSocketGroup.joinGroup(inetAddress);

            byte[] receiveData = new byte[1279]; //1024? (or 4096, 1024)

            // how java saves digital version of the audio
            audioFormat = new AudioFormat(this.bitRate, this.sampleSize, this.channels, true, this.bigEndian);

            // datalines to connect to speakers and play sound from them (converting data into sound)
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);

            sourceDataLine.start();

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());

            while(true){
                // blocking call - will not precede until received packet
                this.receiveSocketGroup.receive(receivePacket);
                audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, receivePacket.getLength());
                toSpeaker(receivePacket.getData(), sourceDataLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void toSpeaker (byte[] soundBytes, SourceDataLine sourceDataLine) {
        try {
            sourceDataLine.write(soundBytes, 0, soundBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        // close connection
        if (sendSocket != null) {
            this.sendSocket.close();
        }
        if (receiveSocketGroup != null) {
            this.receiveSocketGroup.close();
        }
    }
}
