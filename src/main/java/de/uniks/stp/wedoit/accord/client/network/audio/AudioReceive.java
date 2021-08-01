package de.uniks.stp.wedoit.accord.client.network.audio;

import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;
import de.uniks.stp.wedoit.accord.client.model.User;
import org.json.JSONObject;

import javax.sound.sampled.*;
import java.beans.PropertyChangeListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.uniks.stp.wedoit.accord.client.constants.JSON.NAME;

public class AudioReceive extends Thread {

    private final DatagramSocket receiveSocket;
    private final LocalUser localUser;
    private final Map<String, SourceDataLine> sourceDataLineMap;
    private final ArrayList<String> connectedUser;
    public PropertyChangeListener systemVolumeListener = this::onSystemVolumeChange;

    AtomicBoolean shouldReceive;

    public AudioReceive(LocalUser localUser, DatagramSocket receiveSocket, ArrayList<String> connectedUser) {
        this.localUser = localUser;
        this.receiveSocket = receiveSocket;
        this.connectedUser = connectedUser;

        this.shouldReceive = new AtomicBoolean();
        this.shouldReceive.set(true);

        this.sourceDataLineMap = new HashMap<>();
    }

    public void init() {
        localUser.getAccordClient().getOptions().listeners().addPropertyChangeListener(Options.PROPERTY_SYSTEM_VOLUME, systemVolumeListener);
    }

    public void terminate() {
        localUser.getAccordClient().getOptions().listeners().removePropertyChangeListener(Options.PROPERTY_SYSTEM_VOLUME, systemVolumeListener);
    }

    @Override
    public void run() {
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

            Mixer.Info outputDevice = this.localUser.getAccordClient().getOptions().getOutputDevice();
            for (String memberName : connectedUser) {
                if (!memberName.equals(localUser.getName())) {
                    SourceDataLine membersSourceDataLine;
                    if(outputDevice != null) {
                        membersSourceDataLine = (SourceDataLine) AudioSystem.getMixer(outputDevice).getLine(dataLineInfo);
                    }
                    else {
                        membersSourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    }
                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();

                    sourceDataLineMap.put(memberName, membersSourceDataLine);
                }
            }
            updateVolume();
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            while (shouldReceive.get()) {
                this.receiveSocket.receive(receivePacket);
                byte[] receivedAudio = new byte[1024];
                byte[] metaDataByte = new byte[255];
                System.arraycopy(receivePacket.getData(), 0, metaDataByte, 0, 255);
                System.arraycopy(receivePacket.getData(), 255, receivedAudio, 0, 1024);

                String audioSender = getAudioSenderName(metaDataByte);

                if (!sourceDataLineMap.containsKey(audioSender) && !audioSender.equals(localUser.getName())) {
                    SourceDataLine membersSourceDataLine;
                    if(outputDevice != null) {
                        membersSourceDataLine = (SourceDataLine) AudioSystem.getMixer(outputDevice).getLine(dataLineInfo);
                    }
                    else {
                        membersSourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    }
                    membersSourceDataLine.open(audioFormat);
                    membersSourceDataLine.start();

                    sourceDataLineMap.put(audioSender, membersSourceDataLine);
                    updateVolume();
                }
                if (!audioSender.equals(localUser.getName())) {
                    this.sourceDataLineMap.get(audioSender).write(receivedAudio, 0, receivedAudio.length);
                }
            }
            for (String name : sourceDataLineMap.keySet()) {
                SourceDataLine audioMemberLine = this.sourceDataLineMap.get(name);
                audioMemberLine.stop();
                audioMemberLine.flush();
                if (audioMemberLine.isOpen()) {
                    audioMemberLine.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setShouldReceive(boolean value) {
        this.shouldReceive.set(value);
    }

    protected String getAudioSenderName(byte[] metaDataByte) {
        String metaDataString = new String(metaDataByte);
        JSONObject metaDataJson = new JSONObject(metaDataString.trim());

        return metaDataJson.getString(NAME);
    }

    public void muteUser(String username) {
        if (sourceDataLineMap.containsKey(username)) {
            sourceDataLineMap.get(username).stop();
            sourceDataLineMap.get(username).flush();
        }
    }

    public void unmuteUser(String username) {
        if (sourceDataLineMap.containsKey(username)) {
            sourceDataLineMap.get(username).start();
        }
    }

    public void onSystemVolumeChange(Object object) {
        updateVolume();
    }

    public void updateVolume() {
        float systemVolume = localUser.getAccordClient().getOptions().getSystemVolume();
        for (String name : sourceDataLineMap.keySet()) {
            SourceDataLine audioMemberLine = this.sourceDataLineMap.get(name);
            if (audioMemberLine.isOpen() && audioMemberLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) audioMemberLine.getControl(FloatControl.Type.MASTER_GAIN);
                float volumeSetting = calculateVolume(systemVolume, volumeControl, name);
                volumeControl.setValue(volumeSetting);
            }
        }
    }

    private User getUser(String userName){
        for(User user : localUser.getUsers()){
            if(user.getName().equals(userName)){
                return user;
            }
        }
        return null;
    }

    private float calculateVolume(float optionsVolume, FloatControl volumeControl, String userName){
        float finalVolume;
        float userVolume = 0;
        User user = getUser(userName);
        if(user != null) {
            userVolume = user.getAudioVolume();
        }
        if (optionsVolume > 0) {
            float volumeSettingOptions = volumeControl.getMaximum() * optionsVolume / 100;
            if (userVolume >= 0) {
                float right = volumeControl.getMaximum() - volumeSettingOptions;
                finalVolume = volumeSettingOptions + right * userVolume / 100;
            }
            else {
                float left = volumeSettingOptions - volumeControl.getMinimum();
                finalVolume = left * userVolume / -100;
                if (finalVolume > volumeSettingOptions){
                    finalVolume = (finalVolume - volumeSettingOptions) * -1;
                }
            }
        }
        else if(optionsVolume < 0) {
            float volumeSettingOptions = volumeControl.getMinimum() * optionsVolume / -100;
            if (userVolume <= 0) {
                float left = volumeControl.getMinimum() - volumeSettingOptions;
                finalVolume = volumeSettingOptions - left * userVolume / 100;
            }
            else {
                float right = (volumeSettingOptions * -1) + volumeControl.getMaximum();
                finalVolume = volumeSettingOptions + right * userVolume / 100;
                if (finalVolume > volumeControl.getMaximum()) {
                    finalVolume = volumeControl.getMaximum();
                }
            }
        }
        else {
            if (userVolume > 0) {
                finalVolume = (volumeControl.getMaximum() / 100) * userVolume;
            }
            else {
                finalVolume = (volumeControl.getMinimum() / -100) * userVolume;
            }
        }
        return finalVolume;
    }

    public Map<String, SourceDataLine> getSourceDataLineMap(){
        return sourceDataLineMap;
    }
}
