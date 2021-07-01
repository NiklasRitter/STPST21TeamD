package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioReceive extends Thread{

    private final boolean bigEndian = false;
    private final float bitRate = 48000.0f;
    private final int port = 33100;
    private final int sampleSize = 16;
    private final int channels = 1;
    private final String address = "cranberry.uniks.de";

    private DatagramSocket receiveSocket;

    private final LocalUser localUser;
    private final Channel channel;
    private Map<String, SourceDataLine> sourceDataLineMap;
    private ArrayList<String> connectedUser;

    AtomicBoolean shouldReceive;

    public AudioReceive(LocalUser localUser, Channel channel, DatagramSocket receiveSocket, ArrayList<String> connectedUser) {
        this.localUser = localUser;
        this.channel = channel;
        this.receiveSocket = receiveSocket;
        this.sourceDataLineMap = new HashMap<>();

        this.connectedUser = connectedUser;

        this.shouldReceive.set(true);
    }

    @Override
    public void run() {
        // audio once decoded from packet - send to speaker
        AudioInputStream audioInputStream;
        AudioFormat audioFormat;

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            byte[] receiveData = new byte[1279]; //1024? (or 4096, 1024)

            // how java saves digital version of the audio
            audioFormat = new AudioFormat(this.bitRate, this.sampleSize, this.channels, true, this.bigEndian);

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

            for (String memberName: connectedUser) {
                if (!memberName.equals(localUser.getName())) {
                    // datalines to connect to speakers and play sound from them (converting data into sound)
                    SourceDataLine membersSourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();

                    sourceDataLineMap.put(memberName, membersSourceDataLine);
                }
            }

            while(this.shouldReceive.get()){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());

                // blocking call - will not precede until received packet
                this.receiveSocket.receive(receivePacket);
                audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, receivePacket.getLength());

                byte[] receivedAudio = new byte[1024];
                byte[] metaDataByte = new byte[255];
                System.arraycopy(receivePacket.getData(), 0, metaDataByte, 0, 255);
                System.arraycopy(receivePacket.getData(), 255, receivedAudio, 0, 1024);

                String metaDataString = new String(metaDataByte);
                JSONObject metaDataJson = new JSONObject(metaDataString);
                String audioSender = metaDataJson.getString("name");

                if (!sourceDataLineMap.containsKey(audioSender) && !audioSender.equals(localUser.getName())) {
                    SourceDataLine membersSourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

                    sourceDataLineMap.put(audioSender, membersSourceDataLine);

                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();
                }

                if (!audioSender.equals(localUser.getName())) {
                    this.sourceDataLineMap.get(audioSender).write(receivedAudio, 0, receivedAudio.length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toSpeaker (byte[] soundBytes, SourceDataLine sourceDataLine) {
        try {
            // System.out.println(Arrays.toString(soundBytes));
            sourceDataLine.write(soundBytes, 0, soundBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setShouldReceive(boolean value) {
        this.shouldReceive.set(value);
    }
}
