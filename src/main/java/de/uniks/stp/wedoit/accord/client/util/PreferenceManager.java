package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import static de.uniks.stp.wedoit.accord.client.constants.Preferences.DARKMODE;

public class PreferenceManager {

    private StageManager stageManager;
    public PropertyChangeListener darkmodeListener = this::onDarkmodeChanged;

    /**
     * Loads the darkmode preference from the Registry.
     *
     * @return The value of the darkmode preference.
     */
    public static boolean loadDarkmode() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(DARKMODE, false);
    }

    /**
     * Saves the darkmode preference to the Registry.
     *
     * @param darkmode The value of the darkmode preference.
     */
    public static void saveDarkmode(boolean darkmode) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(DARKMODE, darkmode);
    }

    /**
     * Called when the darkmode preference of the Options change.
     * <p>
     * Sets the darkmode in the StageManager and saves it using saveDarkmode.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onDarkmodeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Boolean) {
            boolean darkmode = (boolean) propertyChangeEvent.getNewValue();

            this.stageManager.changeDarkmode(darkmode);

            saveDarkmode(darkmode);
        }
    }

    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }
}