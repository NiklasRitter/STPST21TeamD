package de.uniks.stp.wedoit.accord.client.language;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

public class LanguagePreferencesAndLanguageResolverTest {

    @Before
    public void LanguagePreferencesAndLanguageResolver() {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale("language/Language"));
        LanguageResolver.load();
    }

    @Test
    public void testGetCurrentLocale() {
        Locale locale = LanguagePreferences.getLanguagePreferences().getCurrentLocale("language/Language_fa_IR");

        Assert.assertEquals(locale.getLanguage(), "fa_ir");
    }

    @Test
    public void testGetString() {
        Assert.assertEquals(LanguageResolver.getString("NEW_SERVERNAME"), "New server name");
        Assert.assertEquals(Locale.getDefault().getLanguage(), "en_gb");
    }

    @Test
    public void testLoad() {
        Locale.setDefault(LanguagePreferences.getLanguagePreferences().getCurrentLocale("language/Language_fa_IR"));
        LanguageResolver.load();
        Assert.assertEquals(LanguageResolver.getString("NEW_SERVERNAME"), "نام جدید سرور");
        Assert.assertEquals(Locale.getDefault().getLanguage(), "fa_ir");
    }

}
