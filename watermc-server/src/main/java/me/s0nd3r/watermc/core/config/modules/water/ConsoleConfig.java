package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.console.ConsoleTranslator;
import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;
import org.slf4j.LoggerFactory;

/** Язык вывода консоли: en, ru или uk. Файл журнала всегда пишется на английском. */
public class ConsoleConfig extends ConfigModules {

    public static String language = "en";

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".console";
    }

    @Override
    public void onLoaded() {
        final String requested = config.getString(getBasePath() + ".language", language, """
            [WaterMc] Console output language: en (English), ru (Русский), uk (Українська).
            Only the terminal is translated; logs/latest.log stays in English for bug reports.
            The -Dwatermc.console.language=<lang> startup flag overrides this value
            and also translates the lines printed before this file is read.""");
        final String previous = ConsoleTranslator.language();
        language = ConsoleTranslator.setLanguage(requested);
        if (!language.equals(previous)) {
            LoggerFactory.getLogger("WaterMc").info("Console language: {}", language);
        }
    }
}
