package me.s0nd3r.watermc.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Потокобезопасные замены {@link java.text.SimpleDateFormat}, совместимые по формату
 * с прежними строками в usercache.json, бан-сообщениях и именах резервных копий.
 */
public final class WaterDateFormats {

    /** Формат {@code expiresOn} в usercache.json: {@code 2026-11-12 10:00:00 +0200}. */
    public static final DateTimeFormatter USER_CACHE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);

    /** Срок бана в сообщении отключения: {@code 2026-11-12 at 10:00:00 CET}. */
    public static final DateTimeFormatter BAN_EXPIRATION = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH:mm:ss z", Locale.ROOT);

    /** Компактная метка времени каталога резервной копии конфигурации. */
    public static final DateTimeFormatter BACKUP_STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss", Locale.ROOT);

    /** Время на графике памяти. */
    public static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private WaterDateFormats() {
    }

    public static String format(final DateTimeFormatter formatter, final Date date) {
        return format(formatter, date.toInstant());
    }

    public static String format(final DateTimeFormatter formatter, final Instant instant) {
        return formatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    /**
     * Разбирает дату с явным смещением (формат {@link #USER_CACHE}).
     *
     * @throws DateTimeParseException если строка не соответствует формату
     */
    public static Date parseOffset(final DateTimeFormatter formatter, final String text) {
        return Date.from(OffsetDateTime.parse(text, formatter).toInstant());
    }
}
