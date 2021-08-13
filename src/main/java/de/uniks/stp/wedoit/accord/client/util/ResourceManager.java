package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.AccordClient;
import de.uniks.stp.wedoit.accord.client.model.LocalUser;
import de.uniks.stp.wedoit.accord.client.model.Options;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public class ResourceManager {
    private PreferenceManager preferenceManager;
    public PropertyChangeListener localUserListener = this::onLocalUserChange;

    /**
     * Add all necessary propertyChangeListeners from options.
     *
     * @param clientModel The model of the Client.
     */
    public void start(AccordClient clientModel) {
        if (clientModel.getOptions() != null) {
            loadOptions(clientModel.getOptions());
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, preferenceManager.darkModeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_LANGUAGE, preferenceManager.languageListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_REMEMBER_ME, preferenceManager.rememberMeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_SYSTEM_VOLUME, preferenceManager.systemVolumeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_INPUT_VOLUME, preferenceManager.inputVolumeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_CHAT_FONT_SIZE, preferenceManager.chatFontSizeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_ZOOM_LEVEL, preferenceManager.zoomLevelListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().addPropertyChangeListener(Options.PROPERTY_AUDIO_ROOT_MEAN_SQUARE, preferenceManager.audioRootMeanSquareListener);
        }
        if (clientModel.getLocalUser() != null) {
            loadLocalUser(clientModel.getLocalUser());
            Objects.requireNonNull(clientModel.getLocalUser()).listeners().addPropertyChangeListener(LocalUser.PROPERTY_PASSWORD, preferenceManager.passwordListener);
            Objects.requireNonNull(clientModel.getLocalUser()).listeners().addPropertyChangeListener(LocalUser.PROPERTY_NAME, preferenceManager.usernameListener);
        }
        Objects.requireNonNull(clientModel).listeners().addPropertyChangeListener(AccordClient.PROPERTY_LOCAL_USER, localUserListener);
    }

    /**
     * Remove all propertyChangeListeners from options.
     *
     * @param clientModel The model of the Client.
     */
    public void stop(AccordClient clientModel) {
        if (clientModel.getOptions() != null) {
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_DARKMODE, preferenceManager.darkModeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_LANGUAGE, preferenceManager.languageListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_REMEMBER_ME, preferenceManager.rememberMeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_SYSTEM_VOLUME, preferenceManager.systemVolumeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_INPUT_VOLUME, preferenceManager.systemVolumeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_CHAT_FONT_SIZE, preferenceManager.chatFontSizeListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_ZOOM_LEVEL, preferenceManager.zoomLevelListener);
            Objects.requireNonNull(clientModel.getOptions()).listeners().removePropertyChangeListener(Options.PROPERTY_AUDIO_ROOT_MEAN_SQUARE, preferenceManager.audioRootMeanSquareListener);
        }
        if (clientModel.getLocalUser() != null) {
            Objects.requireNonNull(clientModel.getLocalUser()).listeners().removePropertyChangeListener(LocalUser.PROPERTY_PASSWORD, preferenceManager.passwordListener);
            Objects.requireNonNull(clientModel.getLocalUser()).listeners().removePropertyChangeListener(LocalUser.PROPERTY_NAME, preferenceManager.usernameListener);
        }
        Objects.requireNonNull(clientModel).listeners().removePropertyChangeListener(AccordClient.PROPERTY_LOCAL_USER, localUserListener);
    }

    /**
     * Called when the local user of the AccordClient is changed.
     * <p>
     * Updates the propertyChangeListeners.
     *
     * @param propertyChangeEvent The called event.
     */
    public void onLocalUserChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getOldValue() instanceof LocalUser) {
            LocalUser oldLocalUser = (LocalUser) propertyChangeEvent.getOldValue();

            oldLocalUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_PASSWORD,
                    preferenceManager.passwordListener);
            oldLocalUser.listeners().removePropertyChangeListener(LocalUser.PROPERTY_NAME,
                    preferenceManager.usernameListener);
        }
        if (propertyChangeEvent.getNewValue() instanceof LocalUser) {
            LocalUser newLocalUser = (LocalUser) propertyChangeEvent.getNewValue();

            newLocalUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_PASSWORD,
                    preferenceManager.passwordListener);
            newLocalUser.listeners().addPropertyChangeListener(LocalUser.PROPERTY_NAME,
                    preferenceManager.usernameListener);
        }
    }

    /**
     * Save all options using the PreferenceManager.
     *
     * @param options The options to be saved.
     */
    public void saveOptions(Options options) {
        preferenceManager.saveDarkMode(options.isDarkmode());
        preferenceManager.saveRememberMe(options.isRememberMe());
        preferenceManager.saveLanguage(options.getLanguage());
        preferenceManager.saveSystemVolume(options.getSystemVolume());
        preferenceManager.saveInputVolume(options.getInputVolume());
        preferenceManager.saveAudioRootMeanSquare(options.getAudioRootMeanSquare());
        preferenceManager.saveChatFontSize(options.getChatFontSize());
        preferenceManager.saveZoomLevel(options.getZoomLevel());
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public void setPreferenceManager(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
    }

    /**
     * Load all options using the PreferenceManager.
     * <p>
     * Add necessary PropertyChangeListener.
     *
     * @param options The Options the values should be loaded into.
     */
    public void loadOptions(Options options) {
        Objects.requireNonNull(options).setDarkmode(preferenceManager.loadDarkMode());
        Objects.requireNonNull(options).setLanguage(preferenceManager.loadLanguage());
        Objects.requireNonNull(options).setRememberMe(preferenceManager.loadRememberMe());
        Objects.requireNonNull(options).setSystemVolume(preferenceManager.loadSystemVolume());
        Objects.requireNonNull(options).setInputVolume(preferenceManager.loadInputVolume());
        Objects.requireNonNull(options).setAudioRootMeanSquare(preferenceManager.loadAudioRootMeanSquare());
        Objects.requireNonNull(options).setChatFontSize(preferenceManager.loadChatFontSize());
        Objects.requireNonNull(options).setZoomLevel(preferenceManager.loadZoomLevel());
    }

    /**
     * Save username and password using the PreferenceManager.
     *
     * @param localUser The localUser to be saved.
     */
    public void saveLocalUser(LocalUser localUser) {
        preferenceManager.savePassword(localUser.getPassword());
        preferenceManager.saveUsername(localUser.getName());
    }

    /**
     * Load username and password using the PreferenceManager.
     *
     * @param localUser The localUser the values should be loaded into.
     */
    public void loadLocalUser(LocalUser localUser) {
        Objects.requireNonNull(localUser).setPassword(preferenceManager.loadPassword());
        Objects.requireNonNull(localUser).setName(preferenceManager.loadUsername());
    }

    /**
     * If no InitializationVector is available it gets created otherwise the existing vector gets returned
     *
     * @return InitializationVector string
     */
    public String getOrCreateInitializationVector() throws NoSuchAlgorithmException {
        String IVString = preferenceManager.loadInitializationVector();
        if (IVString.isEmpty()) {
            byte[] iv = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            IVString = new String(iv);
            preferenceManager.saveInitializationVector(IVString);
        }
        return IVString;
    }

}
