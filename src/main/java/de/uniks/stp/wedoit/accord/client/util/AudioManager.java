package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
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
        audioConnection.getAudioReceive().muteUser(user.getName());
    }
}
