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

    private String language = "language/Language";

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
     * Erzeugt eine Instanz der Einstellungen oder liefert eine vorhandene
     * zurück.
     *
     * @return eine Instanz der Einstellungen
     */
    public static LanguagePreferences getLanguagePreferences() {
        if (instance == null) {
            instance = new LanguagePreferences();
        }
        return instance;
    }

    /**
     * Setzt die verwendete Sprache
     *
     * @param language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Liefert die verwendete Sprache zurück
     *
     * @return die verwendete Sprache
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
