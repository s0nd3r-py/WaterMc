package me.s0nd3r.watermc.storage;

import com.github.luben.zstd.Zstd;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.zip.DataFormatException;

/** Bulk compression on the existing compression workers. Closing the target commits an Anvil chunk. */
public final class NativeRegionOutputStream extends OutputStream {
    public enum Format { ZLIB, ZSTD }
    public static final int LEVEL = Math.clamp(Integer.getInteger("watermc.region-compression-level", 3), 1, 3);
    private static final ThreadLocal<byte[]> TRANSFER = ThreadLocal.withInitial(() -> new byte[8192]);
    private final OutputStream target;
    private final Format format;
    private final IntFunction<VelocityCompressor> factory;
    private RawBuffer raw = new RawBuffer();
    private boolean closed;

    public NativeRegionOutputStream(OutputStream target, Format format) {
        this(target, format, level -> Natives.compress.get().create(level));
    }
    NativeRegionOutputStream(OutputStream target, Format format, IntFunction<VelocityCompressor> factory) {
        this.target = Objects.requireNonNull(target);
        this.format = Objects.requireNonNull(format);
        this.factory = Objects.requireNonNull(factory);
    }
    private void writable(int length) throws IOException {
        if (this.closed) throw new IOException("Region compressor is closed");
        if (length < 0 || (long) this.raw.size() + length > net.minecraft.world.level.chunk.storage.RegionFile.MAX_CHUNK_SIZE) {
            throw new IOException("Uncompressed region chunk exceeds maximum size");
        }
    }
    @Override public void write(int value) throws IOException { this.writable(1); this.raw.write(value); }
    @Override public void write(byte[] bytes, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        this.writable(length); this.raw.write(bytes, offset, length);
    }
    @Override public void flush() throws IOException { this.writable(0); }
    @Override public void close() throws IOException {
        if (this.closed) return;
        this.closed = true;
        try {
            if (this.format == Format.ZSTD) this.zstd();
            else this.zlib();
            // Never close/commit a target after a failed encode or write.
            this.target.close();
        } finally { this.raw = null; }
    }
    private void zstd() throws IOException {
        byte[] output = new byte[Math.toIntExact(Zstd.compressBound(this.raw.size()))];
        long length = Zstd.compressByteArray(output, 0, output.length, this.raw.array(), 0, this.raw.size(), LEVEL);
        if (Zstd.isError(length)) throw new IOException("ZSTD: " + Zstd.getErrorName(length));
        this.target.write(output, 0, Math.toIntExact(length));
    }
    private void zlib() throws IOException {
        ByteBuf input = Unpooled.wrappedBuffer(this.raw.array(), 0, this.raw.size());
        try (VelocityCompressor compressor = this.factory.apply(LEVEL)) {
            ByteBuf compatible = MoreByteBufUtils.ensureCompatible(UnpooledByteBufAllocator.DEFAULT, compressor, input);
            try {
                ByteBuf output = MoreByteBufUtils.preferredBuffer(UnpooledByteBufAllocator.DEFAULT, compressor, this.raw.size() + 64);
                try {
                    compressor.deflate(compatible, output);
                    byte[] transfer = TRANSFER.get();
                    while (output.isReadable()) {
                        int count = Math.min(output.readableBytes(), transfer.length);
                        output.readBytes(transfer, 0, count);
                        this.target.write(transfer, 0, count);
                    }
                } finally { output.release(); }
            } finally { compatible.release(); }
        } catch (DataFormatException failure) { throw new IOException("Region deflate failed", failure); }
        finally { input.release(); }
    }
    private static final class RawBuffer extends ByteArrayOutputStream {
        RawBuffer() { super(8192); }
        byte[] array() { return this.buf; }
    }
}
