package me.s0nd3r.watermc.console;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;

/**
 * {@code %waterMsg{%msg}} — текст сообщения, переведённый на язык консоли WaterMc.
 *
 * <p>Используется только в шаблоне терминала: файл журнала пишется без перевода. Переводчик живёт в основном
 * коде сервера и находится при создании конвертера; если его нет (например, конфигурация log4j используется
 * отдельно), текст выводится как есть.</p>
 */
@Plugin(name = "waterMsg", category = PatternConverter.CATEGORY)
@ConverterKeys({"waterMsg"})
public final class TranslateConverter extends LogEventPatternConverter {

    private static final MethodHandle TRANSLATE = findTranslator();

    private final List<PatternFormatter> formatters;

    private TranslateConverter(final List<PatternFormatter> formatters) {
        super("waterMsg", null);
        this.formatters = formatters;
    }

    private static MethodHandle findTranslator() {
        try {
            final Class<?> translator = Class.forName("me.s0nd3r.watermc.console.ConsoleTranslator", true, TranslateConverter.class.getClassLoader());
            return MethodHandles.publicLookup().findStatic(translator, "translate", MethodType.methodType(String.class, String.class));
        } catch (final ReflectiveOperationException | LinkageError ex) {
            return null;
        }
    }

    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        final int start = toAppendTo.length();
        for (final PatternFormatter formatter : this.formatters) {
            formatter.format(event, toAppendTo);
        }
        if (TRANSLATE == null) {
            return;
        }
        final String original = toAppendTo.substring(start);
        final String translated;
        try {
            translated = (String) TRANSLATE.invokeExact(original);
        } catch (final Throwable ex) {
            return; // журнал важнее перевода
        }
        if (translated != original) {
            toAppendTo.setLength(start);
            toAppendTo.append(translated);
        }
    }

    public static TranslateConverter newInstance(final Configuration config, final String[] options) {
        if (options.length != 1) {
            LOGGER.error("waterMsg expects exactly one option, got " + options.length);
            return null;
        }
        final PatternParser parser = PatternLayout.createPatternParser(config);
        return new TranslateConverter(parser.parse(options[0]));
    }
}
