package de.uniks.stp.wedoit.accord.client.language;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for localization of the application
 * All access to the language files should be bundled in this class.
 * Access is only allowed with the methods provided here.
 * These take care, among other things, of missing resources and the like.
 */
public class LanguageResolver {

    private LanguageResolver() {
        throw new AssertionError();
    }

    private static final Logger logger = Logger.getLogger(LanguageResolver.class.getName());

    static {
        logger.setLevel(Level.WARNING);
    }

    private static volatile ResourceBundle resource;

    /**
     * Searches for a key in the language file and returns its value.
     *
     * @param keyInLangFile The searched key
     * @return The string belonging to the searched key or "[keyInLangFile]" if the corresponding key does not have an entry.
     */
    public static String getString(String keyInLangFile) {
        String nullString = "";
        return getString(keyInLangFile, nullString);
    }

    /**
     * Searches for a key in the language file and returns its value.
     *
     * @param keyInLangFile The searched key
     * @return The string belonging to the searched key or the content of alternativeText if the corresponding key does not have an entry.
     */
    public static String getString(String keyInLangFile, String alternativeText) {
        ResourceBundle thisBundle = getLanguage(); //ResourceBundle.getBundle ("language/Language");

        try {
            return thisBundle.getString(keyInLangFile);
        } catch (MissingResourceException missingEx) {
            return "" + keyInLangFile + "";
        }
    }

    public static ResourceBundle getLanguage() {
        ResourceBundle bundle = resource;
        if (bundle == null) {
            resource = bundle = ResourceBundle.getBundle(getLanguageURL(Locale.getDefault()));
        }
        return bundle;
    }

    public static void load() {
        resource = ResourceBundle.getBundle(getLanguageURL(Locale.getDefault()));
    }

    private static String getLanguageURL(Locale currentLocale) {
        if (currentLocale.equals(new Locale("en_GB"))) {
            return "language/Language";
        } else {
            if (currentLocale.equals(new Locale("de_DE"))) {
                return "language/Language_de_DE";
            } else {
                if (currentLocale.equals(new Locale("fa_IR"))) {
                    return "language/Language_fa_IR";
                } else {
                    return "language/Language";
                }
            }
        }
    }

}

