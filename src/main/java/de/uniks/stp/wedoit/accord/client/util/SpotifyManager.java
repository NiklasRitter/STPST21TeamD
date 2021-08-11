package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.network.spotify.SpotifyIntegration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

public class SpotifyManager {
    private final Editor editor;
    private final PropertyChangeListener localUserCurrentlyPlayingTrackListener = this::localUserCurrentlyPlayingTrackOnChange;
    private final PropertyChangeListener localUserListener = this::localUserOnChange;
    private Timer trackTimer;
    private Timer refreshTimer;
    private SpotifyIntegration spotifyIntegration;
    private int TRACK_TIMER = 1000;
    private int REFRESH_TIMER = 3540000;

    public SpotifyManager(Editor editor) {
        this.editor = editor;
        this.spotifyIntegration = this.editor.getSpotifyIntegration();
    }

    public void setupTrackTimer() {
        this.spotifyIntegration = this.editor.getSpotifyIntegration();
        if (editor.getLocalUser() != null && spotifyIntegration != null) {
            trackTimer = new Timer();
            trackTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    spotifyIntegration.getUsersCurrentlyPlayingTrack();
                }
            }, 0, TRACK_TIMER);
            editor.getLocalUser().setTrackTimer(trackTimer);
            editor.getLocalUser().listeners().addPropertyChangeListener(LocalUser.PROPERTY_SPOTIFY_CURRENTLY_PLAYING, localUserCurrentlyPlayingTrackListener);
            editor.getAccordClient().listeners().addPropertyChangeListener(AccordClient.PROPERTY_LOCAL_USER, localUserListener);
        }
    }

    public void setupRefreshAuthTimer() {
        this.spotifyIntegration = this.editor.getSpotifyIntegration();
        if (editor.getLocalUser() != null && spotifyIntegration != null) {
            refreshTimer = new Timer();
            refreshTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    spotifyIntegration.reauthorize();
                }
            }, REFRESH_TIMER, REFRESH_TIMER);
            editor.getLocalUser().setRefreshSpotifyAuthTimer(refreshTimer);
        }
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

    public void terminateRefreshTimer() {
        if (editor.getLocalUser() != null) {
            if (editor.getLocalUser().getRefreshSpotifyAuthTimer() != null) {
                editor.getLocalUser().getRefreshSpotifyAuthTimer().cancel();
                editor.getLocalUser().setRefreshSpotifyAuthTimer(null);
            }
        }
        if (trackTimer != null) {
            trackTimer.cancel();
            trackTimer = null;
        }
    }

    public void localUserCurrentlyPlayingTrackOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (editor.getLocalUser() != null && propertyChangeEvent.getNewValue() instanceof String) {
            editor.getRestManager().updateDescription(this.editor.getLocalUser().getSpotifyCurrentlyPlaying());
            //TODO are both needed?
            /*
            // this.editor.changeUserDescription(this.editor.getLocalUser().getId(), this.editor.getLocalUser().getSpotifyCurrentlyPlaying());

            String oldValue = (String) propertyChangeEvent.getOldValue();
            String newValue = (String) propertyChangeEvent.getNewValue();
            String currentDescription = editor.getLocalUser().getDescription();
            if (currentDescription == null) currentDescription = "";
            if (currentDescription.contains(STEAM_KEY)) {
                if (propertyChangeEvent.getNewValue() == null || ((String) propertyChangeEvent.getNewValue()).isEmpty())
                    editor.getRestManager().updateDescription(currentDescription.substring(currentDescription.indexOf(STEAM_KEY), currentDescription.indexOf(STEAM_KEY) + ((String) propertyChangeEvent.getOldValue()).length() + 1));
                else
                    editor.getRestManager().updateDescription(currentDescription.replace(oldValue, newValue));
            } else {
                editor.getRestManager().updateDescription(STEAM_KEY + newValue + currentDescription);
            }
             */
        }
    }

    public void localUserOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue())
            if (propertyChangeEvent.getNewValue() instanceof LocalUser && propertyChangeEvent.getOldValue() instanceof LocalUser) {
                terminateRefreshTimer();
                terminateTrackTimer();
            }
    }
}
