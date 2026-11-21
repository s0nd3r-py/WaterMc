package me.s0nd3r.watermc.pluginremap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Нужно ли плагину переотображение вообще.
 *
 * <p>Ремаппер переводит имена классов и членов сервера из Spigot-маппингов в Mojang. Если ни один класс
 * плагина не ссылается на классы сервера, переводить нечего: полный проход ремаппера (построение classpath
 * сервера в именах Spigot, разбор маппингов, перепаковка jar) дал бы те же классы. Отметка о маппингах
 * в манифесте на загрузку классов не влияет: преобразование reflection и старых API выполняется
 * при загрузке одинаково для всех плагинов.</p>
 *
 * <p>Проверка консервативная: ищутся байты {@code net/minecraft/}, {@code com/mojang/} и
 * {@code org/bukkit/craftbukkit/} в каждом {@code .class} целиком: в именах классов, дескрипторах,
 * сигнатурах и даже в строковых константах. Любое совпадение означает «переотображать, как раньше».
 * Ошибка возможна только в безопасную сторону: лишнее переотображение.</p>
 *
 * <p>Отключение: {@code -Dwatermc.remapper.skip-api-only=false}.</p>
 */
public final class NmsReferenceScanner {

    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("watermc.remapper.skip-api-only", "true"));

    private static final byte[][] MARKERS = {
        "net/minecraft/".getBytes(StandardCharsets.US_ASCII),
        "com/mojang/".getBytes(StandardCharsets.US_ASCII),
        "org/bukkit/craftbukkit/".getBytes(StandardCharsets.US_ASCII),
    };

    private NmsReferenceScanner() {
    }

    /** {@code true}, если хоть один класс jar может ссылаться на классы сервера (или jar не удалось прочитать). */
    public static boolean referencesServerInternals(final Path jar) {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            byte[] buffer = new byte[64 * 1024];
            final Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                final long declared = entry.getSize();
                if (declared > buffer.length) {
                    buffer = new byte[(int) Math.min(Integer.MAX_VALUE - 8, Math.max(declared, buffer.length * 2L))];
                }
                int length = 0;
                try (InputStream in = zip.getInputStream(entry)) {
                    int read;
                    while ((read = in.read(buffer, length, buffer.length - length)) > 0) {
                        length += read;
                        if (length == buffer.length) {
                            buffer = java.util.Arrays.copyOf(buffer, buffer.length * 2);
                        }
                    }
                }
                if (containsMarker(buffer, length)) {
                    return true;
                }
            }
            return false;
        } catch (final IOException | RuntimeException ex) {
            return true; // не смогли проверить — переотображаем как раньше
        }
    }

    static boolean containsMarker(final byte[] data, final int length) {
        // Все маркеры начинаются с 'n', 'c' или 'o'; проверяем только эти позиции.
        for (int i = 0; i < length; i++) {
            final byte b = data[i];
            if (b != 'n' && b != 'c' && b != 'o') {
                continue;
            }
            for (final byte[] marker : MARKERS) {
                if (marker[0] == b && regionMatches(data, length, i, marker)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean regionMatches(final byte[] data, final int length, final int offset, final byte[] marker) {
        if (offset + marker.length > length) {
            return false;
        }
        for (int j = 1; j < marker.length; j++) {
            if (data[offset + j] != marker[j]) {
                return false;
            }
        }
        return true;
    }
}
