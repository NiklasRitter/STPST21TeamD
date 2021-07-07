package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;

public class AudioManager {

    private final Editor editor;
    private AudioConnection audioConnection;

    public AudioManager(Editor editor){
        this.editor = editor;
    }

    public void initAudioConnection(Channel channel){
        if(audioConnection == null){
            audioConnection = new AudioConnection(editor.getLocalUser(), channel);
        }
        audioConnection.startConnection("cranberry.uniks.de", 33100);
    }

    public void muteUser(User user){
        user.setMuted(true);
        audioConnection.getAudioReceive().muteUser(user.getName());
    }

    public void unmuteUser(User user){
        user.setMuted(false);
        audioConnection.getAudioReceive().unmuteUser(user.getName());
    }

    public void closeAudioConnection() {
        if(audioConnection != null){
            audioConnection.close();
            this.audioConnection = null;
        }
    }

    public AudioConnection getAudioConnection() {
        return audioConnection;
    }

    public void setAudioConnection(AudioConnection audioConnection) {
        this.audioConnection = audioConnection;
    }

}
