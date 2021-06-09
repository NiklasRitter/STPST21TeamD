package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.Options;

public class ResourceManager {
    private final PreferenceManager preferenceManager = new PreferenceManager();

    /**
     * Save all options using the PreferenceManager.
     *
     * @param options The options to be saved.
     */
    public void saveOptions(Options options) {
        preferenceManager.saveDarkmode(options.isDarkmode());
        preferenceManager.saveRememberMe(options.isRememberMe());
    }

    /**
     * Load all options using the PreferenceManager.
     * <p>
     * Add necessary PropertyChangeListener.
     *
     * @return The loaded options.
     */
    public Options loadOptions() {
        Options options = new Options();
        options.setDarkmode(preferenceManager.loadDarkmode());
        options.listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, preferenceManager.darkmodeListener);
        return options;
    }

    public void stop(Options options) {
        options.listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, preferenceManager.darkmodeListener);
    }
}
