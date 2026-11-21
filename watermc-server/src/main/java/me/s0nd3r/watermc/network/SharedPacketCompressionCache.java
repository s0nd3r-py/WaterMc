package me.s0nd3r.watermc.network;

import com.github.benmanes.caffeine.cache.Cache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToLongFunction;
import me.s0nd3r.watermc.util.WaterCaches;

/** Exact wire bytes are the sharing boundary, after per-player Anti-Xray and protocol translation. */
public final class SharedPacketCompressionCache implements AutoCloseable {
    public static final SharedPacketCompressionCache GLOBAL = new SharedPacketCompressionCache(
        Math.max(0L, Long.getLong("watermc.shared-compression-cache-bytes", 64L << 20)));
    public static final int MINIMUM_BYTES = Math.max(256, Integer.getInteger("watermc.shared-compression-min-bytes", 4096));
    private final Cache<Key, Frame> cache;
    private final ToLongFunction<ByteBuffer> hash;
    private final long maximumBytes;
    private volatile boolean closed;

    public SharedPacketCompressionCache(long maximumBytes) {
        this(maximumBytes, SharedPacketCompressionCache::defaultHash);
    }

    private static final ThreadLocal<java.util.zip.CRC32C> CRC = ThreadLocal.withInitial(java.util.zip.CRC32C::new);

    /**
     * Хэш содержимого буфера (позиция и лимит не меняются). CRC32C штатно поддерживает direct-буферы и реализован
     * процессорным интринсиком; в отличие от zero-allocation-hashing ему не нужен доступ к {@code sun.nio.ch}
     * (без {@code --add-exports} тот бросал IllegalAccessError в Netty-энкодере и отключал игрока).
     * Хэш только выбирает кандидата: делиться пакетом разрешает побайтовое сравнение, поэтому коллизия даёт лишь промах.
     */
    static long defaultHash(final ByteBuffer bytes) {
        final java.util.zip.CRC32C crc = CRC.get();
        crc.reset();
        crc.update(bytes.duplicate());
        return crc.getValue();
    }
    SharedPacketCompressionCache(long maximumBytes, ToLongFunction<ByteBuffer> hash) {
        this.maximumBytes = Math.max(0L, maximumBytes);
        this.hash = hash;
        this.cache = WaterCaches.newBuilder().maximumWeight(this.maximumBytes)
            .weigher((Key key, Frame value) -> value.weight())
            .expireAfterAccess(Duration.ofSeconds(2))
            .evictionListener((Key key, Frame value, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
                if (value != null) value.releaseOwner();
            })
            .removalListener((Key key, Frame value, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
                if (value != null) value.releaseOwner();
            }).build();
    }
    @FunctionalInterface public interface Encoder { ByteBuf encode() throws Exception; }

    /** Caller owns one reference; the source is borrowed and its indices are unchanged. */
    public ByteBuf acquire(ByteBuf source, Encoder encoder) throws Exception {
        int length = source.readableBytes();
        if (this.closed || this.maximumBytes == 0 || length > this.maximumBytes / 2) return encoder.encode();
        ByteBuffer view = source.nioBuffer(source.readerIndex(), length);
        Key key = new Key(this.hash.applyAsLong(view), length);
        Frame frame;
        try {
            frame = this.cache.get(key, ignored -> {
                try {
                    ByteBuf encoded = encoder.encode();
                    try {
                        byte[] raw = new byte[length];
                        source.getBytes(source.readerIndex(), raw);
                        ByteBuf direct = Unpooled.directBuffer(encoded.readableBytes(), encoded.readableBytes());
                        try {
                            direct.writeBytes(encoded, encoded.readerIndex(), encoded.readableBytes());
                            return new Frame(raw, direct.asReadOnly());
                        } catch (Throwable failure) { direct.release(); throw failure; }
                    } finally { encoded.release(); }
                } catch (Exception failure) { throw new EncodingFailure(failure); }
            });
        } catch (EncodingFailure failure) { throw (Exception) failure.getCause(); }
        if (this.closed) { frame.releaseOwner(); this.cache.invalidate(key); return encoder.encode(); }
        // Hash and size alone never authorize sharing (including different per-player obfuscation).
        if (view.mismatch(frame.raw) != -1) return encoder.encode();
        try { return frame.encoded.retainedDuplicate(); }
        catch (IllegalReferenceCountException evicted) { return encoder.encode(); }
    }
    public void cleanUp() { this.cache.cleanUp(); }
    @Override public void close() {
        this.closed = true;
        this.cache.asMap().values().forEach(Frame::releaseOwner);
        this.cache.invalidateAll();
        this.cache.cleanUp();
    }
    private record Key(long hash, int length) {}
    private static final class EncodingFailure extends RuntimeException {
        EncodingFailure(Exception cause) { super(cause); }
    }
    private static final class Frame {
        final ByteBuffer raw;
        final ByteBuf encoded;
        final AtomicBoolean released = new AtomicBoolean();
        Frame(byte[] raw, ByteBuf encoded) { this.raw = ByteBuffer.wrap(raw).asReadOnlyBuffer(); this.encoded = encoded; }
        int weight() { return (int) Math.min(Integer.MAX_VALUE, (long) this.raw.remaining() + this.encoded.readableBytes() + 512); }
        void releaseOwner() { if (this.released.compareAndSet(false, true)) this.encoded.release(); }
    }
}
