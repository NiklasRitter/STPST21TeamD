package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import static de.uniks.stp.wedoit.accord.client.Constants.COM_DARKMODE;

public class PreferenceManager {
    public static PropertyChangeListener darkmodeListener = PreferenceManager::onDarkmodeChanged;

    public static boolean loadDarkmode() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(COM_DARKMODE, false);
    }

    public static void saveDarkmode(boolean darkmode) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(COM_DARKMODE, darkmode);
    }

    public static void onDarkmodeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Boolean) {
            boolean darkmode = (boolean) propertyChangeEvent.getNewValue();

            StageManager.changeDarkmode(darkmode);

            saveDarkmode(darkmode);
        }
    }
}
