package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;

public class AudioManager {

    private final Editor editor;
    AudioConnection audioConnection;

    public AudioManager(Editor editor){
        this.editor = editor;
    }

    public void initAudioConnection(Channel channel){
        audioConnection = new AudioConnection(editor.getLocalUser(), channel);
        audioConnection.startConnection();
    }

    public void muteUser(User user){
        user.setMuted(true);
        audioConnection.getAudioReceive().muteUser(user.getName());
    }

    public void unmuteUser(User user){
        user.setMuted(false);
        audioConnection.getAudioReceive().unmuteUser(user.getName());
    }

    public void muteYourself(LocalUser yourself) {
        yourself.setMuted(true);
        audioConnection.getAudioSend().stopSending();
    }

    public void unmuteYourself(LocalUser yourself) {
        yourself.setMuted(false);
        audioConnection.getAudioSend().startSending();
    }

    public void closeAudioConnection() {
        if (audioConnection != null) {
            audioConnection.close();
            this.audioConnection = null;
        }
    }
}
