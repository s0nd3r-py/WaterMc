package me.s0nd3r.watermc.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.zip.CRC32C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Журнал записей {@code .wdb}: ключ → полезная нагрузка, только дозапись, с периодической компакцией.
 *
 * <h2>Формат</h2>
 * <pre>
 * header : magic "WDB1" (4) | version u16 | reserved u16
 * record : op u8 | keyLength i32 | key UTF-8 | payloadLength i32 | payload | crc32c i32
 * </pre>
 * {@code op}: 1 = PUT, 2 = REMOVE, 3 = CLEAR. CRC32C покрывает op, длины, ключ и нагрузку.
 *
 * <h2>Гарантии</h2>
 * <ul>
 *     <li>Добавление/удаление записи — одна дозапись, а не перезапись всего файла.</li>
 *     <li>Все записи всех журналов идут через один поток в порядке постановки, поэтому параллельные
 *     сохранения не перемешиваются (прежнее асинхронное JSON-сохранение могло писать файл из нескольких потоков).</li>
 *     <li>Оборванный при аварии хвост (неполная запись или неверный CRC) отбрасывается при открытии,
 *     все предыдущие записи сохраняются.</li>
 *     <li>Компакция пишет временный файл, делает fsync и атомарно заменяет журнал.</li>
 * </ul>
 */
public final class WaterRecordLog implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");

    public static final int MAGIC = 0x57444231; // "WDB1"
    public static final short FORMAT_VERSION = 1;
    public static final int HEADER_SIZE = 8;
    static final byte OP_PUT = 1;
    static final byte OP_REMOVE = 2;
    static final byte OP_CLEAR = 3;
    private static final int MAX_FIELD = 64 * 1024 * 1024;

    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "WaterMc Data Writer");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        return thread;
    });
    private static final Set<WaterRecordLog> OPEN = ConcurrentHashMap.newKeySet();

    private final Path path;
    private final boolean syncEachWrite;
    // Доступ только из потока записи.
    private FileChannel channel;
    private long liveRecordsOnDisk;
    private long recordsOnDisk;
    private volatile boolean closed;

    private WaterRecordLog(final Path path, final boolean syncEachWrite, final FileChannel channel, final long liveRecords, final long records) {
        this.path = path;
        this.syncEachWrite = syncEachWrite;
        this.channel = channel;
        this.liveRecordsOnDisk = liveRecords;
        this.recordsOnDisk = records;
    }

    /** Результат открытия: актуальное содержимое и сам журнал для дальнейших записей. */
    public record Opened(WaterRecordLog log, Map<String, byte[]> entries, boolean truncatedTail) {
    }

    /** Содержимое журнала без открытия на запись. */
    public record Contents(Map<String, byte[]> entries, long validBytes, long records, boolean truncatedTail) {
    }

    /**
     * Открывает (или создаёт) журнал. Оборванный хвост обрезается.
     *
     * @param syncEachWrite делать ли fsync после каждой записи (для редко меняющихся, но важных списков — банов, OP)
     */
    public static Opened open(final Path path, final boolean syncEachWrite) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        final Contents contents = Files.exists(path) ? read(path) : null;
        final FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        try {
            if (contents == null || channel.size() < HEADER_SIZE) {
                channel.truncate(0);
                channel.write(header(), 0);
                channel.force(true);
            } else if (contents.truncatedTail()) {
                LOGGER.warn("{}: truncated tail after an unclean shutdown detected; {} records recovered", path, contents.records());
                channel.truncate(contents.validBytes());
                channel.force(true);
            }
            channel.position(channel.size());
        } catch (final IOException | RuntimeException ex) {
            channel.close();
            throw ex;
        }
        final Map<String, byte[]> entries = contents == null ? new LinkedHashMap<>() : contents.entries();
        final WaterRecordLog log = new WaterRecordLog(path, syncEachWrite, channel, entries.size(), contents == null ? 0 : contents.records());
        OPEN.add(log);
        return new Opened(log, entries, contents != null && contents.truncatedTail());
    }

    /** Читает журнал. Порядок ключей — порядок последней записи PUT. */
    public static Contents read(final Path path) throws IOException {
        final byte[] data = Files.readAllBytes(path);
        final ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        if (data.length < HEADER_SIZE) {
            if (data.length == 0) {
                return new Contents(new LinkedHashMap<>(), 0, 0, false);
            }
            throw new IOException(path + ": файл слишком короткий для заголовка .wdb");
        }
        if (buf.getInt() != MAGIC) {
            throw new IOException(path + ": это не файл .wdb (неверная сигнатура)");
        }
        final short version = buf.getShort();
        if (version != FORMAT_VERSION) {
            throw new IOException(path + ": неподдерживаемая версия .wdb " + version);
        }
        buf.getShort();
        final LinkedHashMap<String, byte[]> entries = new LinkedHashMap<>();
        long records = 0;
        long valid = HEADER_SIZE;
        boolean truncated = false;
        final CRC32C crc = new CRC32C();
        while (buf.hasRemaining()) {
            final int start = buf.position();
            try {
                final byte op = buf.get();
                final int keyLength = buf.getInt();
                if (keyLength < 0 || keyLength > MAX_FIELD || keyLength > buf.remaining()) {
                    truncated = true;
                    break;
                }
                final byte[] key = new byte[keyLength];
                buf.get(key);
                final int payloadLength = buf.getInt();
                if (payloadLength < 0 || payloadLength > MAX_FIELD || payloadLength > buf.remaining()) {
                    truncated = true;
                    break;
                }
                final byte[] payload = new byte[payloadLength];
                buf.get(payload);
                final int storedCrc = buf.getInt();
                crc.reset();
                crc.update(data, start, buf.position() - 4 - start);
                if ((int) crc.getValue() != storedCrc) {
                    truncated = true;
                    break;
                }
                final String keyString = new String(key, StandardCharsets.UTF_8);
                switch (op) {
                    case OP_PUT -> {
                        entries.remove(keyString); // сохраняем порядок последней записи
                        entries.put(keyString, payload);
                    }
                    case OP_REMOVE -> entries.remove(keyString);
                    case OP_CLEAR -> entries.clear();
                    default -> {
                        truncated = true;
                    }
                }
                if (truncated) {
                    break;
                }
                records++;
                valid = buf.position();
            } catch (final java.nio.BufferUnderflowException ex) {
                truncated = true;
                break;
            }
        }
        return new Contents(entries, valid, records, truncated);
    }

    /** Атомарно записывает полный снимок (используется миграцией и компакцией). */
    public static void writeSnapshot(final Path path, final Map<String, byte[]> entries) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        final Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try (FileChannel out = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeFully(out, header());
            for (final Map.Entry<String, byte[]> entry : entries.entrySet()) {
                writeFully(out, encode(OP_PUT, entry.getKey(), entry.getValue()));
            }
            out.force(true);
        }
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException ex) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path path() {
        return this.path;
    }

    public CompletableFuture<Void> put(final String key, final byte[] payload) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(payload, "payload");
        return this.submit(() -> {
            this.append(encode(OP_PUT, key, payload));
            this.liveRecordsOnDisk++;
        });
    }

    public CompletableFuture<Void> remove(final String key) {
        Objects.requireNonNull(key, "key");
        return this.submit(() -> this.append(encode(OP_REMOVE, key, new byte[0])));
    }

    public CompletableFuture<Void> clear() {
        return this.submit(() -> {
            this.append(encode(OP_CLEAR, "", new byte[0]));
            this.liveRecordsOnDisk = 0;
        });
    }

    /**
     * Компакция по снимку, который строится уже в потоке записи. Все изменения карты, сделанные до постановки
     * соответствующих PUT/REMOVE, попадают в снимок; записи, поставленные после, применяются поверх (идемпотентно).
     */
    public CompletableFuture<Void> compact(final Supplier<Map<String, byte[]>> snapshot) {
        return this.submit(() -> {
            final Map<String, byte[]> entries = snapshot.get();
            this.channel.force(true);
            this.channel.close();
            boolean replaced = false;
            try {
                writeSnapshot(this.path, entries);
                replaced = true;
            } finally {
                // Если замена не удалась (например, на Windows файл открыт другим процессом), прежний журнал цел:
                // открываем его снова и продолжаем дозапись — ни одно изменение не теряется.
                this.channel = FileChannel.open(this.path, StandardOpenOption.READ, StandardOpenOption.WRITE);
                this.channel.position(this.channel.size());
                if (replaced) {
                    this.liveRecordsOnDisk = entries.size();
                    this.recordsOnDisk = entries.size();
                } else {
                    Files.deleteIfExists(this.path.resolveSibling(this.path.getFileName() + ".tmp"));
                }
            }
        });
    }

    /** Нужна ли компакция: журнал вырос более чем вдвое относительно числа живых записей. */
    public boolean shouldCompact(final int liveEntries) {
        return this.recordsOnDisk > 64 && this.recordsOnDisk > 2L * Math.max(liveEntries, 1);
    }

    public CompletableFuture<Void> flush() {
        return this.submit(() -> this.channel.force(true));
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        try {
            this.submit(() -> {
                this.channel.force(true);
                this.channel.close();
            }).get(60, TimeUnit.SECONDS);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while closing " + this.path, ex);
        } catch (final java.util.concurrent.ExecutionException | TimeoutException ex) {
            throw new IOException("Failed to close " + this.path, ex);
        } finally {
            this.closed = true;
            OPEN.remove(this);
        }
    }

    /** Дожидается записи всех журналов на диск (остановка сервера, /save-all flush, drain). */
    public static void flushAll(final long timeout, final TimeUnit unit) {
        final CompletableFuture<?>[] futures = OPEN.stream().map(WaterRecordLog::flush).toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).get(timeout, unit);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (final Exception ex) {
            LOGGER.error("Could not wait for data/*.wdb writes to finish", ex);
        }
    }

    public static Set<WaterRecordLog> openLogs() {
        return Collections.unmodifiableSet(OPEN);
    }

    private interface IoAction {
        void run() throws IOException;
    }

    private CompletableFuture<Void> submit(final IoAction action) {
        if (this.closed) {
            return CompletableFuture.failedFuture(new IOException(this.path + " is closed"));
        }
        final CompletableFuture<Void> future = new CompletableFuture<>();
        WRITER.execute(() -> {
            try {
                action.run();
                future.complete(null);
            } catch (final Throwable t) {
                LOGGER.error("Write error in {}", this.path, t);
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private void append(final ByteBuffer record) throws IOException {
        writeFully(this.channel, record);
        this.recordsOnDisk++;
        if (this.syncEachWrite) {
            this.channel.force(false);
        }
    }

    private static void writeFully(final FileChannel channel, final ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static ByteBuffer header() {
        final ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.BIG_ENDIAN);
        header.putInt(MAGIC).putShort(FORMAT_VERSION).putShort((short) 0);
        return header.flip();
    }

    static ByteBuffer encode(final byte op, final String key, final byte[] payload) {
        final byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        final ByteBuffer buf = ByteBuffer.allocate(1 + 4 + keyBytes.length + 4 + payload.length + 4).order(ByteOrder.BIG_ENDIAN);
        buf.put(op).putInt(keyBytes.length).put(keyBytes).putInt(payload.length).put(payload);
        final CRC32C crc = new CRC32C();
        crc.update(buf.array(), 0, buf.position());
        buf.putInt((int) crc.getValue());
        return buf.flip();
    }
}
