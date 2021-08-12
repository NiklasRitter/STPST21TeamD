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
        //TODO
        System.out.println(this.editor.getRefreshToken());
    }

    public void terminateRefreshTimer() {
        if (editor.getLocalUser() != null) {
            if (editor.getLocalUser().getRefreshSpotifyAuthTimer() != null) {
                editor.getLocalUser().getRefreshSpotifyAuthTimer().cancel();
                editor.getLocalUser().setRefreshSpotifyAuthTimer(null);
            }
        }
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
    }

    public void localUserCurrentlyPlayingTrackOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (editor.getLocalUser() != null && propertyChangeEvent.getNewValue() instanceof String) {
            editor.getRestManager().updateDescription(this.editor.getLocalUser().getSpotifyCurrentlyPlaying());
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
