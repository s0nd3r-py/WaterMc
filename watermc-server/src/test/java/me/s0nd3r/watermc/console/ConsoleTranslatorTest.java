package me.s0nd3r.watermc.console;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleTranslatorTest {

    @AfterEach
    void reset() {
        ConsoleTranslator.setLanguage("en");
    }

    @Test
    void englishIsUntouched() {
        ConsoleTranslator.setLanguage("en");
        assertSame("Stopping server", ConsoleTranslator.translate("Stopping server"));
    }

    @Test
    void translatesExactAndParameterizedLines() {
        ConsoleTranslator.setLanguage("ru");
        assertEquals("Остановка сервера", ConsoleTranslator.translate("Stopping server"));
        assertEquals("Steve зашёл на сервер", ConsoleTranslator.translate("Steve joined the game"));
        assertEquals("Сервер не успевает: отставание 5023 мс (100 тиков). Проверьте нагрузку.",
            ConsoleTranslator.translate("Can't keep up! Is the server overloaded? Running 5023ms or 100 ticks behind"));
        // порядок значений в переводе другой
        assertEquals("Загрузка постоянных чанков мира «minecraft:overworld»: 0",
            ConsoleTranslator.translate("Loading 0 persistent chunks for world 'minecraft:overworld'..."));
        assertEquals("Steve [/127.0.0.1:5000] вошёл: сущность 42, мир world, координаты 1.5, 64.0, -3.5",
            ConsoleTranslator.translate("Steve[/127.0.0.1:5000] logged in with entity id 42 at ([world]1.5, 64.0, -3.5)"));
        assertEquals("some plugin line", ConsoleTranslator.translate("some plugin line"));
    }

    @Test
    void foreignLinesWithCommonWordsStayUntouched() {
        ConsoleTranslator.setLanguage("ru");
        final String oshi = "Disabling further attempts to query Paging File.";
        assertSame(oshi, ConsoleTranslator.translate(oshi));
        assertEquals("Включение WaterBench v1.0.0", ConsoleTranslator.translate("Enabling WaterBench v1.0.0"));
        assertEquals("Плагины Bukkit (2):\n - A (1.0)\n - B (2.0)", ConsoleTranslator.translate("Bukkit plugins (2):\n - A (1.0)\n - B (2.0)"));
        assertEquals("Плагин plugins/x.jar переотображён за 12 мс", ConsoleTranslator.translate("Plugin plugins/x.jar remapped in 12 ms"));
    }

    @Test
    void ukrainianWorks() {
        assertEquals("uk", ConsoleTranslator.setLanguage("українська"));
        assertEquals("Зупинка сервера", ConsoleTranslator.translate("Stopping server"));
        assertEquals("Steve вийшов із сервера", ConsoleTranslator.translate("Steve left the game"));
    }

    @Test
    void readyLineIsNeverTranslated() {
        ConsoleTranslator.setLanguage("ru");
        final String done = "Done (12.345s)! For help, type \"help\"";
        assertSame(done, ConsoleTranslator.translate(done));
    }

    @Test
    void unknownLanguageFallsBackToEnglish() {
        assertEquals("en", ConsoleTranslator.setLanguage("klingon"));
        assertEquals("ru", ConsoleTranslator.setLanguage("RU"));
        assertEquals("uk", ConsoleTranslator.setLanguage("ua"));
    }

    @Test
    void tablesCoverTheSameMessagesAndUseValidPlaceholders() throws IOException {
        final Set<String> ru = sources("ru");
        final Set<String> uk = sources("uk");
        assertEquals(ru, uk, "ru and uk must translate the same set of messages");
        assertTrue(ru.size() >= 90, "table size " + ru.size());
        assertEquals(ru.size(), ConsoleTranslator.size("ru"));
        for (final String language : List.of("ru", "uk")) {
            for (final String[] entry : entries(language)) {
                final int values = entry[0].split("\\{}", -1).length - 1;
                final Matcher m = Pattern.compile("\\{(\\d*)}").matcher(entry[1]);
                int sequential = 0;
                while (m.find()) {
                    final int index = m.group(1).isEmpty() ? ++sequential : Integer.parseInt(m.group(1));
                    assertTrue(index >= 1 && index <= values, language + ": " + entry[0] + " => " + entry[1]);
                }
            }
        }
    }

    private static Set<String> sources(final String language) throws IOException {
        final Set<String> out = new LinkedHashSet<>();
        for (final String[] entry : entries(language)) {
            assertTrue(out.add(entry[0]), "duplicate in " + language + ": " + entry[0]);
        }
        return out;
    }

    private static List<String[]> entries(final String language) throws IOException {
        try (InputStream in = ConsoleTranslator.class.getResourceAsStream("/watermc/console/" + language + ".txt")) {
            assertNotNull(in, language);
            final List<String[]> out = new ArrayList<>();
            for (final String raw : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                final String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final int arrow = line.indexOf(" => ");
                assertTrue(arrow > 0, "malformed line in " + language + ": " + line);
                out.add(new String[]{line.substring(0, arrow), line.substring(arrow + 4)});
            }
            return out;
        }
    }
}
