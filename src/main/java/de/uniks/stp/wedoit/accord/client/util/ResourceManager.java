package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.Options;

public class ResourceManager {

    /**
     * Save all options using the PreferenceManager.
     *
     * @param options The options to be saved.
     */
    public static void saveOptions(Options options) {
        PreferenceManager.saveDarkmode(options.isDarkmode());
    }

    /**
     * Load all options using the PreferenceManager.
     * <p>
     * Add necessary PropertyChangeListener.
     *
     * @return The loaded options.
     */
    public static Options loadOptions() {
        Options options = new Options();
        options.setDarkmode(PreferenceManager.loadDarkmode());
        options.listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, PreferenceManager.darkmodeListener);
        return options;
    }
}
