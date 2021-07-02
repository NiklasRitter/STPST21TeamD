package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioReceive extends Thread{

    private final DatagramSocket receiveSocket;

    private final LocalUser localUser;
    private final Map<String, SourceDataLine> sourceDataLineMap;
    private final ArrayList<String> connectedUser;

    AtomicBoolean shouldReceive;

    public AudioReceive(LocalUser localUser, DatagramSocket receiveSocket, ArrayList<String> connectedUser) {
        this.localUser = localUser;
        this.receiveSocket = receiveSocket;
        this.connectedUser = connectedUser;

        this.shouldReceive = new AtomicBoolean();
        this.shouldReceive.set(true);

        this.sourceDataLineMap = new HashMap<>();
    }

    @Override
    public void run() {
        AudioInputStream audioInputStream;
        AudioFormat audioFormat;

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {
            byte[] receiveData = new byte[1279];

            int channels = 1;
            int sampleSize = 16;
            float bitRate = 48000.0f;
            boolean bigEndian = false;
            audioFormat = new AudioFormat(bitRate, sampleSize, channels, true, bigEndian);

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

            for (String memberName: connectedUser) {
                if (!memberName.equals(localUser.getName())) {
                    SourceDataLine membersSourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();

                    sourceDataLineMap.put(memberName, membersSourceDataLine);
                }
            }

            while(this.shouldReceive.get()){
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());

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
                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();

                    sourceDataLineMap.put(audioSender, membersSourceDataLine);
                }

                if (!audioSender.equals(localUser.getName())) {
                    this.sourceDataLineMap.get(audioSender).write(receivedAudio, 0, receivedAudio.length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setShouldReceive(boolean value) {
        this.shouldReceive.set(value);
    }
}
