package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.spotify.SpotifyIntegration;

import javax.json.JsonObject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.*;

public class SpotifyManager {
    private final Editor editor;
    private final PropertyChangeListener localUserCurrentlyPlayingTrackListener = this::localUserCurrentlyPlayingTrackOnChange;
    private final PropertyChangeListener localUserListener = this::localUserOnChange;
    private Timer trackTimer;
    private SpotifyIntegration spotifyIntegration;

    public SpotifyManager(Editor editor) {
        this.editor = editor;
        this.spotifyIntegration = this.editor.getSpotifyIntegration();
    }

    public void setupTrackTimer() {
        this.spotifyIntegration = this.editor.getSpotifyIntegration();
        Timer timer = editor.getLocalUser().getTrackTimer();
        if (editor.getLocalUser() != null && spotifyIntegration != null && editor.getLocalUser().getTrackTimer() == null) {
            trackTimer = createTrackTimer();
            editor.getLocalUser().setTrackTimer(trackTimer);
            editor.getLocalUser().listeners().addPropertyChangeListener(LocalUser.PROPERTY_SPOTIFY_CURRENTLY_PLAYING, localUserCurrentlyPlayingTrackListener);
            editor.getAccordClient().listeners().addPropertyChangeListener(AccordClient.PROPERTY_LOCAL_USER, localUserListener);
        }
    }

    public Timer createTrackTimer() {
        Timer trackTimer = new Timer();
        trackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                spotifyIntegration.setUsersCurrentlyPlayingTrack();
            }
        }, 0, 5000);
        return trackTimer;
    }

    public void terminateTrackTimer() {
        if (editor.getLocalUser() != null) {
            if (editor.getLocalUser().getTrackTimer() != null) {
                editor.getLocalUser().getTrackTimer().cancel();
                editor.getLocalUser().setTrackTimer(null);
            }
            editor.getLocalUser().listeners().removePropertyChangeListener(localUserCurrentlyPlayingTrackListener);
            editor.getAccordClient().listeners().removePropertyChangeListener(localUserListener);
        }
        if (trackTimer != null) {
            trackTimer.cancel();
            trackTimer = null;
        }
    }

    public void localUserCurrentlyPlayingTrackOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != null && propertyChangeEvent.getNewValue() instanceof String) {
            String description = editor.getLocalUser().getDescription();
            if (description != null && description.startsWith(CUSTOM_KEY) && description.length() > 1 || description != null && description.startsWith(STEAM_KEY) && description.length() > 1) return;
            if (propertyChangeEvent.getNewValue().equals("null ")) {
                System.out.println(propertyChangeEvent.getNewValue());
                editor.getLocalUser().setDescription(JsonUtil.buildDescription(CUSTOM_KEY, " "));
                return;
            }
            editor.getLocalUser().setDescription(JsonUtil.buildDescription(SPOTIFY_KEY, (String) propertyChangeEvent.getNewValue()));
        }
    }

    public void localUserOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue())
            if (propertyChangeEvent.getNewValue() instanceof LocalUser && propertyChangeEvent.getOldValue() instanceof LocalUser) {
                terminateTrackTimer();
            }
    }
}
