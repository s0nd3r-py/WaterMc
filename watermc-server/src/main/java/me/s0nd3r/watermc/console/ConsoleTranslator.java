package me.s0nd3r.watermc.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Перевод строк консоли на русский и украинский.
 *
 * <p>Переводится только вывод в терминал: файл {@code logs/latest.log} остаётся на английском, чтобы журналы
 * можно было прикладывать к отчётам об ошибках и разбирать сторонними инструментами.</p>
 *
 * <p>Таблицы — {@code watermc/console/<язык>.txt}, по строке на сообщение:
 * {@code английский шаблон => перевод}. {@code {}} в шаблоне — любое значение; в переводе {@code {}} подставляет
 * значения по порядку, {@code {1}}, {@code {2}} — по номеру (если в языке другой порядок слов). Строки,
 * которых нет в таблице, выводятся без изменений.</p>
 *
 * <p>Не переводится строка готовности сервера ({@code Done (…)! For help, type "help"}): по ней панели хостинга
 * определяют, что сервер запущен.</p>
 */
public final class ConsoleTranslator {

    public static final String PROPERTY = "watermc.console.language";
    public static final List<String> LANGUAGES = List.of("en", "ru", "uk");

    private static final Map<String, Table> TABLES = new HashMap<>();
    private static volatile Table active = Table.EMPTY;
    private static volatile String language = "en";

    static {
        final String fromProperty = System.getProperty(PROPERTY);
        if (fromProperty != null) {
            setLanguage(fromProperty);
        }
    }

    private ConsoleTranslator() {
    }

    /** Текущий язык ({@code en}, {@code ru} или {@code uk}). */
    public static String language() {
        return language;
    }

    /**
     * Выбрать язык консоли. Неизвестное значение — английский. Системное свойство {@value #PROPERTY},
     * если задано при запуске, имеет приоритет над настройкой.
     */
    public static synchronized String setLanguage(final String requested) {
        final String fromProperty = System.getProperty(PROPERTY);
        final String normalized = normalize(fromProperty != null ? fromProperty : requested);
        language = normalized;
        active = "en".equals(normalized) ? Table.EMPTY : TABLES.computeIfAbsent(normalized, ConsoleTranslator::load);
        return normalized;
    }

    static String normalize(final String value) {
        final String v = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "ru", "ru_ru", "russian", "русский" -> "ru";
            case "uk", "ua", "uk_ua", "ukrainian", "українська", "украинский" -> "uk";
            default -> "en";
        };
    }

    /** Перевести готовую строку сообщения; без перевода возвращает её же. */
    public static String translate(final String message) {
        final Table table = active;
        if (table == Table.EMPTY || message.startsWith("Done (")) {
            return message; // строку готовности читают панели хостинга
        }
        return table.translate(message);
    }

    /** Число переводов в таблице языка (для тестов и диагностики). */
    static int size(final String language) {
        return "en".equals(normalize(language)) ? 0 : TABLES.computeIfAbsent(normalize(language), ConsoleTranslator::load).size();
    }

    private static Table load(final String language) {
        try (InputStream in = ConsoleTranslator.class.getResourceAsStream("/watermc/console/" + language + ".txt")) {
            if (in == null) {
                return Table.EMPTY;
            }
            return Table.parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().toList());
        } catch (final IOException ex) {
            return Table.EMPTY;
        }
    }

    /** Таблица переводов одного языка. */
    static final class Table {
        static final Table EMPTY = new Table(Map.of(), List.of());

        /** Шаблоны без значений: точное совпадение строки. */
        private final Map<String, String> exact;
        /** Шаблоны со значениями, сгруппированные по постоянному началу строки. */
        private final List<Entry> patterns;

        private Table(final Map<String, String> exact, final List<Entry> patterns) {
            this.exact = exact;
            this.patterns = patterns;
        }

        static Table parse(final List<String> lines) {
            final Map<String, String> exact = new HashMap<>();
            final List<Entry> patterns = new ArrayList<>();
            for (final String raw : lines) {
                final String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final int arrow = line.indexOf(" => ");
                if (arrow <= 0) {
                    continue;
                }
                final String source = line.substring(0, arrow);
                final String target = line.substring(arrow + 4);
                if (!source.contains("{}")) {
                    exact.put(source, target);
                } else {
                    patterns.add(Entry.of(source, target));
                }
            }
            // Длинное постоянное начало проверяется раньше: более точный шаблон выигрывает.
            patterns.sort((a, b) -> Integer.compare(b.prefix.length(), a.prefix.length()));
            return new Table(Collections.unmodifiableMap(exact), List.copyOf(patterns));
        }

        String translate(final String message) {
            if (this.exact.isEmpty() && this.patterns.isEmpty()) {
                return message;
            }
            final String exactHit = this.exact.get(message);
            if (exactHit != null) {
                return exactHit;
            }
            for (final Entry entry : this.patterns) {
                if (!message.startsWith(entry.prefix)) {
                    continue;
                }
                final Matcher matcher = entry.pattern.matcher(message);
                if (matcher.matches()) {
                    return entry.render(matcher);
                }
            }
            return message;
        }

        int size() {
            return this.exact.size() + this.patterns.size();
        }
    }

    private record Entry(String prefix, Pattern pattern, List<Object> parts) {

        static Entry of(final String source, final String target) {
            final String[] pieces = source.split("\\{}", -1);
            final StringBuilder regex = new StringBuilder();
            for (int i = 0; i < pieces.length; i++) {
                regex.append(Pattern.quote(pieces[i]));
                if (i < pieces.length - 1) {
                    regex.append("(.*?)");
                }
            }
            // Шаблон должен покрывать строку целиком; последнее значение может содержать что угодно.
            final Pattern pattern = Pattern.compile(regex.toString(), Pattern.DOTALL);
            return new Entry(pieces[0], pattern, compileTarget(target));
        }

        /** Перевод разбивается на текст и номера значений (0 — следующее по порядку). */
        private static List<Object> compileTarget(final String target) {
            final List<Object> parts = new ArrayList<>();
            final Matcher m = Pattern.compile("\\{(\\d*)}").matcher(target);
            int last = 0;
            int sequential = 1;
            while (m.find()) {
                if (m.start() > last) {
                    parts.add(target.substring(last, m.start()));
                }
                parts.add(m.group(1).isEmpty() ? sequential++ : Integer.parseInt(m.group(1)));
                last = m.end();
            }
            if (last < target.length()) {
                parts.add(target.substring(last));
            }
            return List.copyOf(parts);
        }

        String render(final Matcher matcher) {
            final StringBuilder out = new StringBuilder();
            for (final Object part : this.parts) {
                if (part instanceof Integer group) {
                    if (group >= 1 && group <= matcher.groupCount()) {
                        out.append(matcher.group(group));
                    }
                } else {
                    out.append((String) part);
                }
            }
            return out.toString();
        }
    }
}
