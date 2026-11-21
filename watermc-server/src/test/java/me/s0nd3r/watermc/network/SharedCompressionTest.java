package me.s0nd3r.watermc.network;

import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.BufferPreference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;
import net.minecraft.network.CipherEncoder;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.VarInt;
import net.minecraft.network.Varint21LengthFieldPrepender;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedCompressionTest {
    private static byte[] payload(int salt) {
        byte[] raw = new byte[16384]; new Random(salt).nextBytes(raw); return raw;
    }
    private static byte[] decode(ByteBuf compressed) throws Exception {
        ByteBuf view = compressed.duplicate(); int length = VarInt.read(view);
        byte[] bytes = ByteBufUtil.getBytes(view);
        if (length == 0) return bytes;
        byte[] raw = new InflaterInputStream(new ByteArrayInputStream(bytes)).readAllBytes();
        assertEquals(length, raw.length); return raw;
    }
    private static ByteBuf root(ByteBuf buffer) {
        while (buffer.unwrap() != null) buffer = buffer.unwrap(); return buffer;
    }
    /** Регрессия: хэш direct-буфера раньше требовал --add-exports java.base/sun.nio.ch и отключал игроков. */
    @Test void defaultHashWorksOnDirectBuffersWithoutJvmFlags() {
        final byte[] raw = payload(7);
        final java.nio.ByteBuffer direct = java.nio.ByteBuffer.allocateDirect(raw.length).put(raw).flip();
        final java.nio.ByteBuffer heap = java.nio.ByteBuffer.wrap(raw);
        final long h = assertDoesNotThrow(() -> SharedPacketCompressionCache.defaultHash(direct));
        assertEquals(h, SharedPacketCompressionCache.defaultHash(heap), "same bytes -> same hash for direct and heap buffers");
        assertEquals(0, direct.position(), "hashing must not move the buffer position");
        assertEquals(raw.length, direct.limit());
        assertNotEquals(h, SharedPacketCompressionCache.defaultHash(java.nio.ByteBuffer.wrap(payload(8))));
        final java.nio.ByteBuffer slice = direct.duplicate().position(100).limit(200);
        assertEquals(SharedPacketCompressionCache.defaultHash(java.nio.ByteBuffer.wrap(raw, 100, 100)), SharedPacketCompressionCache.defaultHash(slice),
            "only position..limit is hashed");
    }

    @Test void twoObserversRetainOneDirectFrameUntilTheLastRelease() throws Exception {
        SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20);
        EmbeddedChannel a = new EmbeddedChannel(new CompressionEncoder(null, 0, cache));
        EmbeddedChannel b = new EmbeddedChannel(new CompressionEncoder(null, 0, cache));
        byte[] raw = payload(1); ByteBuf first = null, second = null, backing = null;
        try {
            assertTrue(a.writeOutbound(Unpooled.wrappedBuffer(raw))); first = a.readOutbound();
            assertTrue(b.writeOutbound(Unpooled.wrappedBuffer(raw))); second = b.readOutbound();
            assertTrue(first.isDirect()); assertTrue(first.isReadOnly());
            assertSame(root(first), root(second)); backing = root(first);
            assertEquals(3, backing.refCnt()); assertArrayEquals(raw, decode(first)); assertArrayEquals(raw, decode(second));
            cache.close(); assertEquals(2, backing.refCnt());
            first.release(); first = null; assertEquals(1, backing.refCnt());
            second.release(); second = null; assertEquals(0, backing.refCnt());
        } finally {
            if (first != null) first.release(); if (second != null) second.release();
            cache.close(); a.finishAndReleaseAll(); b.finishAndReleaseAll();
        }
    }
    @Test void framePrefixUsesGatheringComponentsWithoutCopyingTheCompressedBody() throws Exception {
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20)) {
            EmbeddedChannel a = new EmbeddedChannel(new Varint21LengthFieldPrepender(), new CompressionEncoder(null, 0, cache));
            EmbeddedChannel b = new EmbeddedChannel(new Varint21LengthFieldPrepender(), new CompressionEncoder(null, 0, cache));
            ByteBuf first = null, second = null;
            try {
                byte[] raw = payload(2); a.writeOutbound(Unpooled.wrappedBuffer(raw)); b.writeOutbound(Unpooled.wrappedBuffer(raw));
                first = a.readOutbound(); second = b.readOutbound();
                CompositeByteBuf ca = assertInstanceOf(CompositeByteBuf.class, first), cb = assertInstanceOf(CompositeByteBuf.class, second);
                assertTrue(ca.isDirect()); assertEquals(2, ca.numComponents());
                assertSame(root(ca.component(1)), root(cb.component(1)));
                ByteBuf view = first.duplicate(); int size = VarInt.read(view); assertEquals(size, view.readableBytes());
                assertArrayEquals(raw, decode(view));
            } finally {
                if (first != null) first.release(); if (second != null) second.release();
                a.finishAndReleaseAll(); b.finishAndReleaseAll();
            }
        }
    }
    @Test void hashCollisionsNeverShareDifferentPlayerPayloads() throws Exception {
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20, bytes -> 0L)) {
            EmbeddedChannel channel = new EmbeddedChannel(new CompressionEncoder(null, 0, cache));
            try {
                for (int salt : new int[]{3, 4, 3, 5}) {
                    byte[] raw = payload(salt); channel.writeOutbound(Unpooled.wrappedBuffer(raw)); ByteBuf output = channel.readOutbound();
                    try { assertArrayEquals(raw, decode(output)); } finally { output.release(); }
                }
            } finally { channel.finishAndReleaseAll(); }
        }
    }
    @Test void identicalSlicesWithDifferentReaderOffsetsShareButUnrelatedBytesDoNot() throws Exception {
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20)) {
            byte[] raw = payload(6); ByteBuf a = Unpooled.wrappedBuffer(raw), b = Unpooled.buffer(raw.length + 7);
            b.writeZero(7).writeBytes(raw).readerIndex(7); AtomicInteger loads = new AtomicInteger();
            ByteBuf first = null, second = null;
            try {
                first = cache.acquire(a, () -> { loads.incrementAndGet(); return a.copy(); });
                second = cache.acquire(b, () -> { loads.incrementAndGet(); return b.copy(); });
                assertEquals(1, loads.get()); assertSame(root(first), root(second));
                assertEquals(0, a.readerIndex()); assertEquals(7, b.readerIndex());
            } finally { if (first != null) first.release(); if (second != null) second.release(); a.release(); b.release(); }
        }
    }
    @Test void perClientEncryptionNeverMutatesTheSharedPlaintext() throws Exception {
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20)) {
            XorCipher x = new XorCipher(0x34), y = new XorCipher(0x71);
            EmbeddedChannel plainChannel = new EmbeddedChannel(new CompressionEncoder(null, 0, cache));
            EmbeddedChannel a = new EmbeddedChannel(new CipherEncoder(x), new Varint21LengthFieldPrepender(), new CompressionEncoder(null, 0, cache));
            EmbeddedChannel b = new EmbeddedChannel(new CipherEncoder(y), new Varint21LengthFieldPrepender(), new CompressionEncoder(null, 0, cache));
            ByteBuf plain = null, first = null, second = null;
            try {
                byte[] raw = payload(7); plainChannel.writeOutbound(Unpooled.wrappedBuffer(raw)); plain = plainChannel.readOutbound();
                byte[] snapshot = ByteBufUtil.getBytes(plain);
                a.writeOutbound(Unpooled.wrappedBuffer(raw)); b.writeOutbound(Unpooled.wrappedBuffer(raw));
                first = a.readOutbound(); second = b.readOutbound();
                assertFalse(ByteBufUtil.equals(first, second)); assertNotSame(root(first), root(second));
                x.process(first); y.process(second); assertTrue(ByteBufUtil.equals(first, second));
                ByteBuf view = first.duplicate(); assertEquals(VarInt.read(view), view.readableBytes()); assertArrayEquals(raw, decode(view));
                assertArrayEquals(snapshot, ByteBufUtil.getBytes(plain)); assertArrayEquals(raw, decode(plain));
            } finally {
                if (plain != null) plain.release(); if (first != null) first.release(); if (second != null) second.release();
                plainChannel.finishAndReleaseAll(); a.finishAndReleaseAll(); b.finishAndReleaseAll();
            }
            assertTrue(x.closed); assertTrue(y.closed);
        }
    }
    @Test void cacheCloseAndEvictionRacesPreservePayloadAndReleaseEveryOutput() throws Exception {
        SharedPacketCompressionCache cache = new SharedPacketCompressionCache(64 << 10);
        CountDownLatch start = new CountDownLatch(1); CompletableFuture<?>[] work = new CompletableFuture<?>[8];
        for (int worker = 0; worker < work.length; ++worker) {
            int seed = worker; CompletableFuture<Void> done = new CompletableFuture<>(); work[worker] = done;
            Thread.ofPlatform().start(() -> {
                try {
                    start.await();
                    for (int i = 0; i < 100; ++i) {
                        ByteBuf source = Unpooled.wrappedBuffer(payload(seed + i % 4));
                        try {
                            ByteBuf output = cache.acquire(source, source::copy);
                            try { assertTrue(ByteBufUtil.equals(source, output)); } finally { output.release(); }
                        } finally { source.release(); }
                        if (seed == 0 && i == 50) cache.close();
                        if (i % 10 == 0) cache.cleanUp();
                    }
                    done.complete(null);
                } catch (Throwable failure) { done.completeExceptionally(failure); }
            });
        }
        start.countDown();
        try { CompletableFuture.allOf(work).get(30, TimeUnit.SECONDS); } finally { cache.close(); }
    }
    @Test void disabledCacheAndFailedLoaderHaveNoHiddenOwnership() throws Exception {
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(0)) {
            ByteBuf source = Unpooled.wrappedBuffer(payload(9)); AtomicInteger loads = new AtomicInteger();
            try {
                for (int i = 0; i < 3; ++i) {
                    ByteBuf output = cache.acquire(source, () -> { loads.incrementAndGet(); return source.copy(); });
                    assertEquals(1, output.refCnt()); assertTrue(output.release()); assertEquals(0, output.refCnt());
                }
                assertEquals(3, loads.get());
            } finally { source.release(); }
        }
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20)) {
            ByteBuf source = Unpooled.wrappedBuffer(payload(10));
            try { assertThrows(java.io.IOException.class, () -> cache.acquire(source, () -> { throw new java.io.IOException("injected"); })); }
            finally { source.release(); }
        }
    }
    @Test void nativeAndJavaEncodersDecodeIdenticallyAcrossThresholdChanges() throws Exception {
        for (boolean nativeCodec : new boolean[]{false, true}) {
            try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(1 << 20)) {
                CompressionEncoder encoder = new CompressionEncoder(nativeCodec ? com.velocitypowered.natives.util.Natives.compress.get().create(3) : null, 256, cache);
                EmbeddedChannel channel = new EmbeddedChannel(encoder);
                try {
                    for (int size : new int[]{0, 1, 255, 256, 4096, 16384}) {
                        for (int threshold : new int[]{0, 256, 20000}) {
                            encoder.setThreshold(threshold); byte[] raw = Arrays.copyOf(payload(11), size);
                            channel.writeOutbound(Unpooled.wrappedBuffer(raw)); ByteBuf output = channel.readOutbound();
                            try { assertArrayEquals(raw, decode(output)); } finally { output.release(); }
                        }
                    }
                } finally { channel.finishAndReleaseAll(); }
            }
        }
    }
    @Test void oversizedFramingAndCipherFailureReleaseTheirInputs() {
        EmbeddedChannel frame = new EmbeddedChannel(new Varint21LengthFieldPrepender()); ByteBuf oversized = Unpooled.buffer(1 << 21).writeZero(1 << 21);
        try { assertThrows(io.netty.handler.codec.EncoderException.class, () -> frame.writeOutbound(oversized)); assertEquals(0, oversized.refCnt()); }
        finally { frame.finishAndReleaseAll(); }
        ByteBuf input = Unpooled.directBuffer(16).writeZero(16).asReadOnly();
        EmbeddedChannel cipher = new EmbeddedChannel(new CipherEncoder(new XorCipher(0) {
            @Override public void process(ByteBuf buffer) { throw new IllegalStateException("injected"); }
        }));
        try { assertThrows(io.netty.handler.codec.EncoderException.class, () -> cipher.writeOutbound(input)); assertEquals(0, input.refCnt()); }
        finally { cipher.finishAndReleaseAll(); }
    }
    @Test void compressionHandlersOwnSeparateContextsAcrossThresholdUpdates() throws Exception {
        AtomicInteger encoderClosed = new AtomicInteger(), decoderClosed = new AtomicInteger();
        var encoderCompressor = trackedCompressor(encoderClosed);
        var decoderCompressor = trackedCompressor(decoderClosed);
        var decoder = new net.minecraft.network.CompressionDecoder(decoderCompressor, 256, true);
        try (SharedPacketCompressionCache cache = new SharedPacketCompressionCache(0)) {
            EmbeddedChannel channel = new EmbeddedChannel(decoder, new CompressionEncoder(encoderCompressor, 256, cache));
            try {
                for (int i = 0; i < 100; ++i) decoder.setThreshold(i, false);
                assertEquals(0, encoderClosed.get()); assertEquals(0, decoderClosed.get());
                channel.pipeline().remove(decoder); assertEquals(1, decoderClosed.get()); assertEquals(0, encoderClosed.get());
            } finally { channel.finishAndReleaseAll(); }
            assertEquals(1, decoderClosed.get()); assertEquals(1, encoderClosed.get());
        }
    }
    private static com.velocitypowered.natives.compression.VelocityCompressor trackedCompressor(AtomicInteger closed) {
        return new com.velocitypowered.natives.compression.VelocityCompressor() {
            @Override public BufferPreference preferredBufferType() { return BufferPreference.HEAP_REQUIRED; }
            @Override public void inflate(ByteBuf input, ByteBuf output, int size) { throw new UnsupportedOperationException(); }
            @Override public void deflate(ByteBuf input, ByteBuf output) { throw new UnsupportedOperationException(); }
            @Override public void close() { closed.incrementAndGet(); }
        };
    }
    private static class XorCipher implements VelocityCipher {
        private final int key; boolean closed;
        XorCipher(int key) { this.key = key; }
        @Override public BufferPreference preferredBufferType() { return BufferPreference.DIRECT_REQUIRED; }
        @Override public void process(ByteBuf buffer) {
            assertFalse(buffer.isReadOnly()); assertEquals(1, buffer.nioBufferCount());
            for (int i = buffer.readerIndex(); i < buffer.writerIndex(); ++i) buffer.setByte(i, buffer.getByte(i) ^ this.key);
        }
        @Override public void close() { this.closed = true; }
    }
}
