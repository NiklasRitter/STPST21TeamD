package de.uniks.stp.wedoit.accord.client.language;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LanguagePreferences {

    private static final Logger logger = Logger.getLogger(LanguagePreferences.class.getName());

    static {
        logger.setLevel(Level.FINE);
    }

    private static LanguagePreferences instance = null;

    private String language;

    String s = "English";

    private LanguagePreferences() {

        switch (s) {
            case "Deutsch":
                language = "language/Language_de_DE";
                break;
            case "فارسی":
                language = "language/Language_fa_IR";
                break;
            case "English":
            default:
                language = "language/Language";
        }

    }

    /**
     * Creates an instance of the settings or returns an existing one.
     *
     * @return an instance of the settings
     */
    public static LanguagePreferences getLanguagePreferences() {
        if (instance == null) {
            instance = new LanguagePreferences();
        }
        return instance;
    }

    /**
     * Sets the language used
     *
     * @param language to be set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return the used language
     */
    public String getLanguage() {
        return language;
    }

    public Locale getCurrentLocale(String languageURL) {
        Locale currentLocale;
        switch (languageURL) {
            case "language/Language_de_DE":
                currentLocale = new Locale("de_DE");
                break;
            case "language/Language_fa_IR":
                currentLocale = new Locale("fa_IR");
                break;
            case "language/Language":
            default:
                currentLocale = new Locale("en_GB");
                break;
        }

        return currentLocale;
    }
}
