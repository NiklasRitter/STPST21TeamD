package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import static de.uniks.stp.wedoit.accord.client.Constants.COM_DARKMODE;

public class PreferenceManager {
    public static PropertyChangeListener darkmodeListener = PreferenceManager::onDarkmodeChanged;

    /**
     * Loads the darkmode preference from the Registry.
     *
     * @return The value of the darkmode preference.
     */
    public static boolean loadDarkmode() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(COM_DARKMODE, false);
    }

    /**
     * Saves the darkmode preference to the Registry.
     *
     * @param darkmode The value of the darkmode preference.
     */
    public static void saveDarkmode(boolean darkmode) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(COM_DARKMODE, darkmode);
    }

    /**
     * Called when the darkmode preference of the Options change.
     * <p>
     * Sets the darkmode in the StageManager and saves it using saveDarkmode.
     *
     * @param propertyChangeEvent The called event.
     */
    public static void onDarkmodeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Boolean) {
            boolean darkmode = (boolean) propertyChangeEvent.getNewValue();

            StageManager.changeDarkmode(darkmode);

            saveDarkmode(darkmode);
        }
    }
}