package me.s0nd3r.watermc.storage;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.BufferPreference;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.zip.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RegionCompressionTest {
    @TempDir Path directory;
    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
        System.out.println("WATER_REGION_CODEC zlib=" + Natives.compress.getLoadedVariant()
            + " zstd=" + "zstd-jni-1.5.7-9" + " level=" + NativeRegionOutputStream.LEVEL);
    }
    private static CompoundTag data(int size) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version());
        tag.putString("marker", "WaterMc codec equivalence");
        byte[] bytes = new byte[size]; new Random(121126).nextBytes(bytes); tag.putByteArray("payload", bytes);
        return tag;
    }
    private static byte[] nbt(CompoundTag tag) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.write(tag, new DataOutputStream(output)); return output.toByteArray();
    }
    @Test void allFormatsRoundTripTheSameNbtAndZstdUsesTheCustomAnvilMarker() throws Exception {
        CompoundTag tag = data(32768);
        for (RegionFileVersion codec : new RegionFileVersion[]{RegionFileVersion.VERSION_GZIP, RegionFileVersion.VERSION_DEFLATE,
            RegionFileVersion.VERSION_NONE, RegionFileVersion.VERSION_LZ4, RegionFileVersion.VERSION_ZSTD}) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(codec.wrap(bytes))) { NbtIo.write(tag, output); }
            if (codec == RegionFileVersion.VERSION_ZSTD) {
                assertEquals(127, codec.getId());
                assertEquals("watermc:zstd", new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())).readUTF());
            }
            try (DataInputStream input = new DataInputStream(codec.wrap(new ByteArrayInputStream(bytes.toByteArray())))) {
                assertEquals(tag, NbtIo.read(input));
            }
        }
    }
    @Test void actualRegionFilesReadLegacyGzipAndZlibWithoutRewritingThem() throws Exception {
        CompoundTag tag = data(8192); byte[] raw = nbt(tag);
        for (int id : new int[]{1, 2}) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (OutputStream stream = id == 1 ? new GZIPOutputStream(bytes) : new DeflaterOutputStream(bytes)) { stream.write(raw); }
            byte[] compressed = bytes.toByteArray();
            int sectors = (compressed.length + 5 + 4095) / 4096;
            ByteBuffer file = ByteBuffer.allocate(8192 + sectors * 4096);
            file.putInt(0, (2 << 8) | sectors); file.position(8192);
            file.putInt(compressed.length + 1).put((byte) id).put(compressed);
            Path path = this.directory.resolve("r." + id + ".0.mca"); Files.write(path, file.array());
            try (RegionFile region = new RegionFile(new RegionStorageInfo("test", Level.OVERWORLD, "chunk"), path,
                this.directory, RegionFileVersion.VERSION_ZSTD, false);
                 DataInputStream input = region.getChunkDataInputStream(new ChunkPos(0, 0))) {
                assertNotNull(input); assertEquals(tag, NbtIo.read(input));
            }
        }
    }
    @Test void nativeRegionWritesSurviveReopenIncludingExternalZstdChunks() throws Exception {
        for (RegionFileVersion codec : new RegionFileVersion[]{RegionFileVersion.VERSION_DEFLATE, RegionFileVersion.VERSION_ZSTD}) {
            CompoundTag tag = data(codec == RegionFileVersion.VERSION_ZSTD ? 1_100_000 : 16384);
            Path folder = Files.createDirectory(this.directory.resolve("format" + codec.getId()));
            Path path = folder.resolve("r.0.0.mca");
            RegionStorageInfo info = new RegionStorageInfo("test", Level.OVERWORLD, "chunk");
            try (RegionFile region = new RegionFile(info, path, folder, codec, false)) {
                try (DataOutputStream output = region.getChunkDataOutputStream(new ChunkPos(0, 0))) { NbtIo.write(tag, output); }
            }
            try (RegionFile region = new RegionFile(info, path, folder, RegionFileVersion.VERSION_NONE, false);
                 DataInputStream input = region.getChunkDataInputStream(new ChunkPos(0, 0))) {
                assertNotNull(input); assertEquals(tag, NbtIo.read(input));
            }
            if (codec == RegionFileVersion.VERSION_ZSTD) assertTrue(Files.exists(folder.resolve("c.0.0.mcc")));
        }
    }
    @Test void unknownCustomCodecFailsExplicitly() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); new DataOutputStream(bytes).writeUTF("other:codec");
        assertThrows(IOException.class, () -> RegionFileVersion.VERSION_CUSTOM.wrap(new ByteArrayInputStream(bytes.toByteArray())));
    }
    @Test void failedCompressionReleasesNativeBuffersWithoutCommittingTheTarget() throws Exception {
        final ByteBuf[] captured = new ByteBuf[2]; boolean[] closed = {false};
        VelocityCompressor broken = new VelocityCompressor() {
            @Override public BufferPreference preferredBufferType() { return BufferPreference.HEAP_REQUIRED; }
            @Override public void inflate(ByteBuf in, ByteBuf out, int size) { throw new UnsupportedOperationException(); }
            @Override public void deflate(ByteBuf in, ByteBuf out) throws DataFormatException {
                captured[0] = in; captured[1] = out; throw new DataFormatException("injected failure");
            }
            @Override public void close() { closed[0] = true; }
        };
        int[] commits = {0}; OutputStream target = new ByteArrayOutputStream() { @Override public void close() { ++commits[0]; } };
        NativeRegionOutputStream stream = new NativeRegionOutputStream(target, NativeRegionOutputStream.Format.ZLIB, level -> broken);
        stream.write(new byte[100]); assertThrows(IOException.class, stream::close); stream.close();
        assertTrue(closed[0]); assertEquals(0, commits[0]);
        assertEquals(0, captured[0].refCnt()); assertEquals(0, captured[1].refCnt());
        assertThrows(IOException.class, () -> stream.write(1));
    }
    @Test void failedTargetWriteDoesNotCommitAndSuccessfulCloseIsIdempotent() throws Exception {
        int[] commits = {0};
        OutputStream broken = new OutputStream() {
            @Override public void write(int value) throws IOException { throw new IOException("injected disk failure"); }
            @Override public void close() { ++commits[0]; }
        };
        NativeRegionOutputStream failed = new NativeRegionOutputStream(broken, NativeRegionOutputStream.Format.ZSTD);
        failed.write(1); assertThrows(IOException.class, failed::close); assertEquals(0, commits[0]);
        NativeRegionOutputStream success = new NativeRegionOutputStream(new ByteArrayOutputStream() {
            @Override public void close() { ++commits[0]; }
        }, NativeRegionOutputStream.Format.ZSTD);
        success.write(1); success.close(); success.close(); assertEquals(1, commits[0]);
    }
}
