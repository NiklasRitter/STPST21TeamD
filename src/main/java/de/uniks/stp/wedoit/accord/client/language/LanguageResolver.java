package de.uniks.stp.wedoit.accord.client.language;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasse für die Lokalisation der Anwendung
 * Alle Zugriffe auf die Sprachdateien sollen in dieser Klasse gebündelt werden.
 * Zugriff ist nur mit den hier zur Verfügung gestellten Methoden erlaubt. Diese
 * kümmern sich unter anderem auch um fehlende Ressourcen u.ä.
 * <p>
 * Idealerweise sollte für den Zugriff die Methode getString (String keyInLangFile)
 * benutzt werden. Diese fragt ob es zu einem Key einen Eintrag im aktiven Sprachbundle
 * gibt und gibt ihn ggf. zurück. Sollte der Key weder im aktiven Sprachbundle noch
 * per automatischem Fallback im Default-Bundle gefunden werden, gibt die Methode stattdessen
 * den übergebenen Key in eckigen Klammern zurück.
 * <p>
 * Auf Wunsch kann die Sprachdatei stattdessen mit der Methode getString (String keyInLangFile, String alternativeText)
 * abgefragt werden. Diese verhält sich grundsätzlich ähnlich wie die oben beschriebene
 * nur der Text der zurückgegeben wird wenn sich ein Key nicht in der Sprachdatei finden
 * lässt wird hier explizit vom Aufrufer angegeben. Unter der Prämisse das die Sprachdatei
 * gepflegt wird und für alle gültigen Keys wenigstens ein Eintrag im Default bundle existiert
 * ist der Gebrauch dieser Methode jedoch nur bedingt sinnvoll. Sie wird primär aus
 * historischen Gründen weiterhin zur Verfügung gestellt und sollte langsam aus der
 * Anwendung gephased werden.
 * <p>
 * Es wird explizit KEIN Caching der Resource Bundles implementiert, da diese Art
 * der Funktionalität bereits zuverläßig in der JVM von Oracle eingebaut wurde.
 * <p>
 * Darüber hinaus sollte jedes Modul eine eigene Sprachdatei und einen eigenen LanguageResolver
 * haben, welcher dann Keys zuerst in der lokalen Sprachdatei sucht und das Base-Bundle
 * nur als Fallback verwendet.
 *
 */
public class LanguageResolver {

    //<editor-fold defaultstate="collapsed" desc="constructor">
    // Suppress default constructor for noninstantiability
    private LanguageResolver() {
        throw new AssertionError();
    }

    //</editor-fold>
    private static final Logger logger = Logger.getLogger(LanguageResolver.class.getName());

    static {
        logger.setLevel(Level.WARNING);
    }

    // Auf true aendern wenn für Tests gespeichert werden soll welche Strings
    // nicht gefunden werden konnten.
    private static final String DEFAULT_BUNDLE_FOR_PDF = "language/Language_en_GB";
    private static volatile ResourceBundle resource;
    private static final List<String> notFoundStrings = new ArrayList<String>();

    /**
     * Sucht einen Key in der Sprachdatei und gibt dessen Wert zurück.
     *
     * @param keyInLangFile Der gesuchte Key
     * @return Der zum gesuchten Key gehörende String oder "[keyInLangFile]" wenn der entsprechende Key keinen Eintrag hat
     */
    public static String getString(String keyInLangFile) {
        String nullString = "";
        return getString(keyInLangFile, nullString);
    }

    /**
     * Sucht einen Key in der Sprachdatei und gibt dessen Wert zurück.
     *
     * @param keyInLangFile   Der gesuchte Key
     * @param alternativeText alternativer Text der ausgegeben wird, wenn kein Eintrag in der Sprachdatei vorhanden ist.
     * @return Der zum gesuchten Key gehörende String oder der Inhalt von alternativeText wenn der entsprechende Key keinen Eintrag hat
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

