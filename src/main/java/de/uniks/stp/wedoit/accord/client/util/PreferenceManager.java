package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import static de.uniks.stp.wedoit.accord.client.constants.Preferences.*;

public class PreferenceManager {
    public PropertyChangeListener darkmodeListener = this::onDarkmodeChanged;
    public PropertyChangeListener rememberMeListener = this::onRememberMeChanged;
    public PropertyChangeListener passwordListener = this::onPasswordChanged;
    public PropertyChangeListener usernameListener = this::onUsernameChanged;

    /**
     * Loads the darkmode preference from the Registry.
     *
     * @return The value of the darkmode preference.
     */
    public boolean loadDarkmode() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(DARKMODE, false);
    }

    /**
     * Saves the darkmode preference to the Registry.
     *
     * @param darkmode The value of the darkmode preference.
     */
    public void saveDarkmode(boolean darkmode) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(DARKMODE, darkmode);
    }

    /**
     * Saves the remember me preference to the Registry.
     *
     * @param rememberMe The value of the remember me preference.
     */
    public void saveRememberMe(boolean rememberMe) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(REMEMBER_ME, rememberMe);
    }

    /**
     * Loads the remember me preference from the Registry.
     *
     * @return The value of the remember me preference.
     */
    public boolean loadRememberMe() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(REMEMBER_ME, false);
    }

    /**
     * Loads the darkmode preference from the Registry.
     *
     * @return The value of the darkmode preference.
     */
    public String loadUsername() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.get(USERNAME, "");
    }

    /**
     * Saves the login preference to the Registry.
     *
     * @param username The value of the darkmode preference.
     */
    public void saveUsername(String username) {
        if (username != null && !username.isEmpty()) {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            preferences.put(USERNAME, username);
        }
    }

    /**
     * Loads the darkmode preference from the Registry.
     *
     * @return The value of the darkmode preference.
     */
    public String loadPassword() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.get(PASSWORD, "");
    }

    /**
     * Saves the login preference to the Registry.
     *
     * @param password The value of the darkmode preference.
     */
    public void savePassword(String password) {
        if (password != null && !password.isEmpty()) {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            preferences.put(PASSWORD, password);
        }
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

            StageManager.changeDarkmode(darkmode);

            saveDarkmode(darkmode);
        }
    }

    /**
     * Called when the rememberMe preference of the Options change.
     * <p>
     * Saves the rememberMe using saveRememberMe.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onRememberMeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Boolean) {
            boolean rememberMe = (boolean) propertyChangeEvent.getNewValue();

            saveRememberMe(rememberMe);
        }
    }

    /**
     * Called when the password of the LocalUser change.
     * <p>
     * Saves the password using savePassword.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onPasswordChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof String) {
            String password = (String) propertyChangeEvent.getNewValue();

            savePassword(password);
        }
    }

    /**
     * Called when the username of the LocalUser change.
     * <p>
     * Saves the username using saveUsername.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onUsernameChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof String) {
            String username = (String) propertyChangeEvent.getNewValue();

            saveUsername(username);
        }
    }
}