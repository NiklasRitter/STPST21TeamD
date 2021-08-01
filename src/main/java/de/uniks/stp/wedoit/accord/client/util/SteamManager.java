package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.Editor;
import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import static de.uniks.stp.wedoit.accord.client.constants.UserDescription.STEAM_KEY;

public class SteamManager {
    private final Editor editor;
    private final PropertyChangeListener localUserGameExtraInfoListener = this::localUserGameExtraInfoOnChange;
    private Timer gameExtraInfoTimer;

    public SteamManager(Editor editor) {
        this.editor = editor;
    }

    public void setupSteamTimer() {
        if (editor.getLocalUser() != null) {
            gameExtraInfoTimer = new Timer();
            gameExtraInfoTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    editor.getRestManager().getLocalUserSteamGameExtraInfo();
                }
            }, 0, 60000);
            editor.getLocalUser().setSteamGameExtraInfoTimer(gameExtraInfoTimer);
            editor.getLocalUser().listeners().addPropertyChangeListener(LocalUser.PROPERTY_STEAM_GAME_EXTRA_INFO, localUserGameExtraInfoListener);
            editor.getAccordClient().listeners().addPropertyChangeListener(AccordClient.PROPERTY_LOCAL_USER, localUserListener);
        }
    }

    public void terminateSteamTimer() {
        if (editor.getLocalUser() != null) {
            if (editor.getLocalUser().getSteamGameExtraInfoTimer() != null) {
                editor.getLocalUser().getSteamGameExtraInfoTimer().cancel();
                editor.getLocalUser().setSteamGameExtraInfoTimer(null);
            }
            editor.getLocalUser().listeners().removePropertyChangeListener(localUserGameExtraInfoListener);
            editor.getAccordClient().listeners().removePropertyChangeListener(localUserListener);
        }
        if (gameExtraInfoTimer != null) {
            gameExtraInfoTimer.cancel();
            gameExtraInfoTimer = null;
        }
    }

    public void localUserGameExtraInfoOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (editor.getLocalUser() != null && propertyChangeEvent.getNewValue() instanceof String) {
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
        }
    }

    public void localUserOnChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() != propertyChangeEvent.getOldValue())
            if (propertyChangeEvent.getNewValue() instanceof LocalUser && propertyChangeEvent.getOldValue() instanceof LocalUser)
                if (((LocalUser) propertyChangeEvent.getNewValue()).getId() != null && !((LocalUser) propertyChangeEvent.getNewValue()).getId().equals(((LocalUser) propertyChangeEvent.getOldValue()).getId()))
                    terminateSteamTimer();
                else terminateSteamTimer();
    }

    private final PropertyChangeListener localUserListener = this::localUserOnChange;
}
