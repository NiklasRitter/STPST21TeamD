package de.uniks.stp.wedoit.accord.client.util;

import de.uniks.stp.wedoit.accord.client.model.Options;

public class ResourceManager {
    public static void saveOptions(Options options) {
        PreferenceManager.saveDarkmode(options.isDarkmode());
    }

    public static Options loadOptions() {
        Options options = new Options();
        options.setDarkmode(PreferenceManager.loadDarkmode());
        options.listeners().addPropertyChangeListener(Options.PROPERTY_DARKMODE, PreferenceManager.darkmodeListener);
        return options;
    }
}
