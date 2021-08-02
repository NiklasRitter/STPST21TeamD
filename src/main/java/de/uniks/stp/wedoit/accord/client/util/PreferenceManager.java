package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.StageManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;

import static de.uniks.stp.wedoit.accord.client.constants.Preferences.*;

public class PreferenceManager {

    public PropertyChangeListener rememberMeListener = this::onRememberMeChanged;
    public PropertyChangeListener usernameListener = this::onUsernameChanged;
    private StageManager stageManager;
    public PropertyChangeListener systemVolumeListener = this::onSystemVolumeChanged;
    public PropertyChangeListener languageListener = this::onLanguageChanged;
    public PropertyChangeListener darkModeListener = this::onDarkModeChanged;
    public PropertyChangeListener passwordListener = this::onPasswordChanged;
    public PropertyChangeListener chatFontSizeListener = this::onChatFontSizeChanged;
    public PropertyChangeListener audioRootMeanSquareListener = this::onAudioRootMeanSquareChanged;

    /**
     * Loads the dark mode preference from the Registry.
     *
     * @return The value of the dark mode preference.
     */
    public boolean loadDarkMode() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getBoolean(DARK_MODE, false);
    }

    /**
     * Saves the darkMode preference to the Registry.
     *
     * @param darkMode The value of the darkMode preference.
     */
    public void saveDarkMode(boolean darkMode) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putBoolean(DARK_MODE, darkMode);
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
     * Saves the SystemVolume preference to the Registry.
     *
     * @param systemVolume The value of the SystemVolume preference.
     */
    public void saveSystemVolume(float systemVolume) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.putFloat(SYSTEM_VOLUME, systemVolume);
    }

    /**
     * Loads the SystemVolume preference from the Registry.
     *
     * @return The value of the SystemVolume preference.
     */
    public float loadSystemVolume() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.getFloat(SYSTEM_VOLUME, 100f);
    }

    /**
     * Loads the dark mode preference from the Registry.
     *
     * @return The value of the dark mode preference.
     */
    public String loadUsername() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);

            return preferences.get(USERNAME, "");
        } catch (Exception e) {
            System.err.println("Error while loading username:");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Saves the login preference to the Registry.
     *
     * @param username The value of the dark mode preference.
     */
    public void saveUsername(String username) {
        if (username != null) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(StageManager.class);

                preferences.put(USERNAME, username);
            } catch (Exception e) {
                System.err.println("Error while saving username:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the dark mode preference from the Registry.
     *
     * @return The value of the dark mode preference.
     */
    public String loadPassword() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            String encrypted = preferences.get(PASSWORD, "");
            return stageManager.getEditor().decryptData(encrypted);
        } catch (Exception e) {
            System.err.println("Error while loading password:");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Saves the login preference to the Registry.
     *
     * @param password The value of the password preference.
     */
    public void savePassword(String password) {
        if (password != null) {
            try {
                String encrypted = stageManager.getEditor().encrypt(password);
                saveEncryptedPassword(encrypted);
            } catch (Exception e) {
                System.err.println("Error while saving password:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads the chatFontSize preference from the Registry.
     *
     * @return The value of the chatFontSize preference.
     */
    public int loadChatFontSize() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            return preferences.getInt(CHAT_FONT_SIZE, 12);
        } catch (Exception e) {
            System.err.println("Error while loading chat font size:");
            e.printStackTrace();
            return 12;
        }
    }

    /**
     * Saves the chatFontSize preference to the Registry.
     *
     * @param chatFontSize The value of the chatFontSize preference.
     */
    public void saveChatFontSize(int chatFontSize) {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            preferences.putInt(CHAT_FONT_SIZE, chatFontSize);
        } catch (Exception e) {
            System.err.println("Error while saving chat font size:");
            e.printStackTrace();
        }
    }

    /**
     * Loads the audioRootMeanSquare preference from the Registry.
     *
     * @return The value of the audioRootMeanSquare preference.
     */
    public double loadAudioRootMeanSquare() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            return preferences.getDouble(AUDIO_ROOT_MEAN_SQUARE, 0);
        } catch (Exception e) {
            System.err.println("Error while loading audio root mean square:");
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Saves the audioRootMeanSquare preference to the Registry.
     *
     * @param audioRootMeanSquare The value of the audioRootMeanSquare preference.
     */
    public void saveAudioRootMeanSquare(double audioRootMeanSquare) {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            preferences.putDouble(AUDIO_ROOT_MEAN_SQUARE, audioRootMeanSquare);
        } catch (Exception e) {
            System.err.println("Error while saving password:");
            e.printStackTrace();
        }
    }

    /**
     * Saves the login preference to the Registry.
     *
     * @param encrypted The value of the encrypted password.
     */
    public void saveEncryptedPassword(String encrypted) {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        preferences.put(PASSWORD, encrypted);
    }

    /**
     * Loads the initializationVector from the Registry.
     *
     * @return The value of the initializationVector.
     */
    public String loadInitializationVector() {
        Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
        return preferences.get(INITIALIZATION_VECTOR, "");
    }

    /**
     * Saves the initializationVector to the Registry.
     *
     * @param initializationVector The value of initializationVector.
     */
    public void saveInitializationVector(String initializationVector) {
        if (initializationVector != null && !initializationVector.isEmpty()) {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            preferences.put(INITIALIZATION_VECTOR, initializationVector);
        }
    }

    /**
     * Called when the dark mode preference of the Options change.
     * <p>
     * Sets the dark mode in the StageManager and saves it using saveDarkMode.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onDarkModeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Boolean) {
            boolean darkMode = (boolean) propertyChangeEvent.getNewValue();

            this.stageManager.changeDarkmode(darkMode);

            saveDarkMode(darkMode);
        }
    }

    private void onLanguageChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof String) {
            String language = (String) propertyChangeEvent.getNewValue();

            this.stageManager.changeLanguage(language);

            saveLanguage(language);
        }
    }

    private void onSystemVolumeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Float) {
            float systemVolume = (float) propertyChangeEvent.getNewValue();

            this.stageManager.getEditor().getAccordClient().getOptions().setSystemVolume(systemVolume);

            saveSystemVolume(systemVolume);
        }
    }

    private void onChatFontSizeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Integer) {
            int chatFontSize = (int) propertyChangeEvent.getNewValue();

            this.stageManager.getEditor().getAccordClient().getOptions().setChatFontSize(chatFontSize);

            saveChatFontSize(chatFontSize);
        }
    }

    private void onAudioRootMeanSquareChanged(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getNewValue() instanceof Double) {
            double audioRootMeanSquare = (double) propertyChangeEvent.getNewValue();

            this.stageManager.getEditor().getAccordClient().getOptions().setAudioRootMeanSquare(audioRootMeanSquare);

            saveAudioRootMeanSquare(audioRootMeanSquare);
        }
    }

    public void saveLanguage(String language) {
        if (language != null) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(StageManager.class);

                preferences.put(LANGUAGE, language);
            } catch (Exception e) {
                System.err.println("Error while saving language!");
                e.printStackTrace();
            }
        }
    }


    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
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

    public String loadLanguage() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);

            return preferences.get(LANGUAGE, "");
        } catch (Exception e) {
            System.err.println("Error while loading language!");
            e.printStackTrace();
            return "";
        }
    }

    public Mixer.Info loadOutputDevice() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            String device = preferences.get(OUTPUT_DEVICE, "");
            for (Mixer.Info m : AudioSystem.getMixerInfo()) {
                if (m.getName().equals(device) && m.getDescription().equals("Direct Audio Device: DirectSound Playback")) {
                    return m;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while loading output device!");
            e.printStackTrace();
        }
        return null;
    }

    public void saveOutputDevice(String outputDevice) {
        if (outputDevice != null) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
                preferences.put(OUTPUT_DEVICE, outputDevice);
            } catch (Exception e) {
                System.err.println("Error while saving outputDevice!");
                e.printStackTrace();
            }
        }
    }

    public Mixer.Info loadInputDevice() {
        try {
            Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
            String device = preferences.get(INPUT_DEVICE, "");
            for (Mixer.Info m : AudioSystem.getMixerInfo()) {
                if (m.getName().equals(device) && m.getDescription().equals("Direct Audio Device: DirectSound Capture")) {
                    return m;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while loading input device!");
            e.printStackTrace();
        }
        return null;
    }

    public void saveInputDevice(String inputDevice) {
        if (inputDevice != null) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(StageManager.class);
                preferences.put(INPUT_DEVICE, inputDevice);
            } catch (Exception e) {
                System.err.println("Error while saving inputDevice!");
                e.printStackTrace();
            }
        }
    }
}