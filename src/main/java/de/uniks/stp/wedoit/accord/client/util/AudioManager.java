package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.Channel;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.User;
import de.uniks.stp.wedoit.accord.client.network.audio.AudioConnection;

import javax.sound.sampled.BooleanControl;
import java.util.List;

public class AudioManager {

    private final Editor editor;
    private AudioConnection audioConnection;

    public AudioManager(Editor editor) {
        this.editor = editor;
    }

    public void initAudioConnection(Channel channel) {
        if (audioConnection == null) {
            audioConnection = new AudioConnection(editor.getLocalUser(), channel);
        }
        audioConnection.startConnection("cranberry.uniks.de", 33100);
    }

    public void muteUser(User user) {
        user.setMuted(true);
        audioConnection.getAudioReceive().muteUser(user.getName());
    }

    public void unmuteUser(User user) {
        user.setMuted(false);
        audioConnection.getAudioReceive().unmuteUser(user.getName());
    }

    public void muteAllUsers(List<User> users) {
        for (User user : users) {
            if (!user.isMuted() && !user.getName().equals(editor.getLocalUser().getName())) {
                muteUser(user);
            }
        }
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
            LocalUser localUser = this.editor.getLocalUser();
            if (localUser.isMuted()) {
                // to make sure, localUser receives data, if alone in chat
                // otherwise .receive will not end
                unmuteYourself(localUser);
            }
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
