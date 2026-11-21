package me.s0nd3r.watermc.storage;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectionContainerCompatibilityTest {
    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        PalettedContainer.WATER_FLAT_SECTION_STORAGE = true; // these tests cover the opt-in flat storage
    }

    @org.junit.jupiter.api.AfterAll
    static void restoreDefault() {
        PalettedContainer.WATER_FLAT_SECTION_STORAGE = false;
    }

    @Test
    void packedStorageIsTheDefaultForBlockSections() {
        PalettedContainer.WATER_FLAT_SECTION_STORAGE = false;
        try {
            PalettedContainer<BlockState> states = container();
            for (int i = 0; i < 64; i++) {
                states.set(i & 15, (i >> 4) & 15, i % 7, i % 3 == 0 ? Blocks.STONE.defaultBlockState() : Blocks.DIRT.defaultBlockState());
            }
            assertFalse(states.data.storage() instanceof WaterSectionStorage, "default must be the packed Paper/Leaf storage (2x less heap per chunk)");
            assertEquals(Blocks.STONE.defaultBlockState(), states.get(0, 0, 0));
            assertEquals(Blocks.DIRT.defaultBlockState(), states.get(1, 0, 1));
        } finally {
            PalettedContainer.WATER_FLAT_SECTION_STORAGE = true;
        }
    }

    private static Strategy<BlockState> strategy() { return Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY); }
    private static PalettedContainer<BlockState> container() { return new PalettedContainer<>(Blocks.AIR.defaultBlockState(), strategy(), null); }

    @Test
    void flatStatesPreserveMutatorsCopiesRecreationAndDiskPacking() {
        PalettedContainer<BlockState> states = container();
        assertInstanceOf(WaterSectionStorage.class, states.data.storage());
        assertTrue(((WaterSectionStorage)states.data.storage()).isCompact());
        BlockState[] values = {Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), Blocks.AIR.defaultBlockState()};
        for (int index = 0; index < 4096; ++index) {
            int x = index & 15, y = index >> 8, z = index >> 4 & 15;
            assertSame(Blocks.AIR.defaultBlockState(), states.getAndSet(x, y, z, values[index & 3]));
            assertSame(values[index & 3], states.get(index));
        }
        PalettedContainer<BlockState> copy = states.copy();
        copy.set(0, 0, 0, Blocks.DIRT.defaultBlockState());
        assertSame(values[0], states.get(0));
        assertSame(Blocks.AIR.defaultBlockState(), states.recreate().get(0));
        PalettedContainerRO.PackedData<BlockState> packed = states.pack(strategy());
        PalettedContainer<BlockState> decoded = PalettedContainer.unpack(strategy(), packed, Blocks.AIR.defaultBlockState(), null).result().orElseThrow();
        assertInstanceOf(WaterSectionStorage.class, decoded.data.storage());
        for (int i = 0; i < 4096; ++i) assertSame(states.get(i), decoded.get(i));
    }

    @Test
    void readsLegacyLocalPaletteAndWritesEquivalentCompactPackets() {
        BlockState[] values = {Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState()};
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeByte(4);
            buffer.writeVarInt(values.length);
            for (BlockState value : values) buffer.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(value));
            SimpleBitStorage legacy = new SimpleBitStorage(4, 4096);
            for (int i = 0; i < 4096; ++i) legacy.set(i, i & 3);
            buffer.writeFixedSizeLongArray(legacy.getRaw());
            PalettedContainer<BlockState> states = container();
            states.read(buffer);
            assertEquals(0, buffer.readableBytes());
            for (int i = 0; i < 4096; ++i) assertSame(values[i & 3], states.get(i));
            buffer.clear();
            states.write(buffer, null, 0);
            assertEquals(states.getSerializedSize(), buffer.readableBytes());
            int bits = buffer.readUnsignedByte();
            assertEquals(4, bits);
            int paletteSize = buffer.readVarInt();
            assertEquals(values.length, paletteSize);
            int[] ids = new int[paletteSize];
            for (int i = 0; i < paletteSize; ++i) ids[i] = buffer.readVarInt();
            SimpleBitStorage network = new SimpleBitStorage(bits, 4096);
            buffer.readFixedSizeLongArray(network.getRaw());
            assertEquals(0, buffer.readableBytes());
            for (int i = 0; i < 4096; ++i) assertEquals(Block.BLOCK_STATE_REGISTRY.getId(values[i & 3]), ids[network.get(i)]);
            buffer.clear();
            states.write(buffer, null, 0);
            PalettedContainer<BlockState> roundTrip = container();
            roundTrip.read(buffer);
            for (int i = 0; i < 4096; ++i) assertSame(states.get(i), roundTrip.get(i));
        } finally { buffer.release(); }
    }

    @Test
    void presetStatesAndSectionCountersRemainCorrect() {
        BlockState[] presets = {Blocks.DIAMOND_ORE.defaultBlockState(), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()};
        PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), strategy(), presets);
        LevelChunkSection section = new LevelChunkSection(states, null);
        assertTrue(section.hasOnlyAir());
        section.setBlockState(1, 2, 3, Blocks.WATER.defaultBlockState());
        assertTrue(section.hasFluid());
        section.setBlockState(2, 2, 3, presets[0]);
        section.recalcBlockCounts();
        assertFalse(section.hasOnlyAir());
        assertTrue(section.hasFluid());
        assertSame(presets[0], section.getBlockState(2, 2, 3));
        section.setBlockState(1, 2, 3, Blocks.AIR.defaultBlockState());
        section.setBlockState(2, 2, 3, Blocks.AIR.defaultBlockState());
        assertTrue(section.hasOnlyAir());
        assertFalse(section.hasFluid());
    }

    @Test
    void biomeAndSmallCustomRegistriesKeepTheirPaletteFormat() {
        IdMapper<String> map = new IdMapper<>();
        map.add("first"); map.add("second");
        PalettedContainer<String> biomes = new PalettedContainer<>("first", Strategy.createForBiomes(map), null);
        biomes.set(1, 2, 3, "second");
        assertEquals("second", biomes.get(1, 2, 3));
        assertFalse(biomes.data.storage() instanceof WaterSectionStorage);
        PalettedContainer<String> custom = new PalettedContainer<>("first", Strategy.createForBlockStates(map), null);
        custom.set(1, 2, 3, "second");
        assertEquals("second", custom.get(1, 2, 3));
        assertFalse(custom.data.storage() instanceof WaterSectionStorage);
    }
    @Test
    void flatMutatorsDoNotAcquireThePaletteMonitor() throws Exception {
        PalettedContainer<BlockState> states = container();
        java.util.concurrent.CompletableFuture<Void> completed = new java.util.concurrent.CompletableFuture<>();
        Thread worker;
        synchronized (states) {
            worker = Thread.ofPlatform().start(() -> {
                try {
                    states.set(1, 2, 3, Blocks.STONE.defaultBlockState());
                    assertSame(Blocks.STONE.defaultBlockState(), states.getAndSet(1, 2, 3, Blocks.DIRT.defaultBlockState()));
                    completed.complete(null);
                } catch (Throwable failure) { completed.completeExceptionally(failure); }
            });
            completed.get(10, java.util.concurrent.TimeUnit.SECONDS);
        }
        worker.join();
        assertSame(Blocks.DIRT.defaultBlockState(), states.get(1, 2, 3));
    }

    @Test
    void homogeneousDiskSectionsWithUnusedPaletteEntriesStayCompact() {
        BlockState air = Blocks.AIR.defaultBlockState(), stone = Blocks.STONE.defaultBlockState();
        SimpleBitStorage storage = new SimpleBitStorage(4, 4096);
        for (int i = 0; i < 4096; ++i) storage.set(i, 1);
        PalettedContainerRO.PackedData<BlockState> packed = new PalettedContainerRO.PackedData<>(
            List.of(air, stone), java.util.Optional.of(java.util.Arrays.stream(storage.getRaw())), 4);
        PalettedContainer<BlockState> states = PalettedContainer.unpack(strategy(), packed, air,
            new BlockState[]{Blocks.DIAMOND_ORE.defaultBlockState()}).result().orElseThrow();
        assertTrue(((WaterSectionStorage)states.data.storage()).isCompact());
        assertSame(air, states.recreate().get(0));
        LevelChunkSection section = new LevelChunkSection(states, null);
        assertFalse(section.hasOnlyAir());
        assertFalse(section.hasFluid());
        for (int i = 0; i < 4096; ++i) assertSame(stone, states.get(i));
    }

    @Test
    void uniformPacketsUseSingleValueEncodingAndGlobalPacketsRoundTrip() {
        for (BlockState value : new BlockState[]{Blocks.AIR.defaultBlockState(), Blocks.STONE.defaultBlockState()}) {
            PalettedContainer<BlockState> states = new PalettedContainer<>(value, strategy(), null);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                states.write(buffer, null, 0);
                assertEquals(states.getSerializedSize(), buffer.readableBytes());
                assertEquals(0, buffer.readUnsignedByte());
                assertEquals(Block.BLOCK_STATE_REGISTRY.getId(value), buffer.readVarInt());
                assertEquals(0, buffer.readableBytes());
                assertTrue(((WaterSectionStorage)states.data.storage()).isCompact());
            } finally { buffer.release(); }
        }
        PalettedContainer<BlockState> states = container();
        for (int i = 0; i < 1024; ++i) states.set(i & 15, i >> 8, i >> 4 & 15, Block.BLOCK_STATE_REGISTRY.byId(i));
        java.util.Set<BlockState> discovered = new it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet<>();
        states.getAll(discovered::add);
        assertEquals(1024, discovered.size());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            states.write(buffer, null, 0);
            assertEquals(states.getSerializedSize(), buffer.readableBytes());
            assertTrue(buffer.getUnsignedByte(0) > 8);
            PalettedContainer<BlockState> decoded = container();
            decoded.read(buffer);
            assertEquals(0, buffer.readableBytes());
            for (int i = 0; i < 4096; ++i) assertSame(states.get(i), decoded.get(i));
        } finally { buffer.release(); }
    }

    @Test
    void antiXrayMetadataDescribesAnIndependentPacketPaletteWithReplacementIds() {
        BlockState replacement = Blocks.DIAMOND_ORE.defaultBlockState();
        PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), strategy(), new BlockState[]{replacement});
        states.set(1, 2, 3, Blocks.STONE.defaultBlockState());
        net.minecraft.world.level.chunk.LevelChunk chunk = org.mockito.Mockito.mock(net.minecraft.world.level.chunk.LevelChunk.class);
        org.mockito.Mockito.when(chunk.getSectionsCount()).thenReturn(1);
        io.papermc.paper.antixray.ChunkPacketInfo<BlockState> info = new io.papermc.paper.antixray.ChunkPacketInfo<>(null, chunk);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            states.write(buffer, info, 0);
            assertEquals(buffer.getUnsignedByte(0), info.getBits(0));
            assertArrayEquals(new BlockState[]{replacement}, info.getPresetValues(0));
            int replacementId = info.getPalette(0).idFor(replacement, net.minecraft.world.level.chunk.PaletteResize.noResizeExpected());
            assertTrue(replacementId < (1 << info.getBits(0)));
            assertSame(replacement, info.getPalette(0).valueFor(replacementId));
            assertEquals(info.getBits(0), buffer.readUnsignedByte());
            int paletteSize = buffer.readVarInt();
            for (int i = 0; i < paletteSize; ++i) buffer.readVarInt();
            assertEquals(buffer.readerIndex(), info.getIndex(0));
            byte[] bytes = new byte[buffer.writerIndex()];
            buffer.getBytes(0, bytes);
            io.papermc.paper.antixray.BitStorageWriter writer = new io.papermc.paper.antixray.BitStorageWriter();
            writer.setBuffer(bytes);
            writer.setBits(info.getBits(0));
            writer.setIndex(info.getIndex(0));
            writer.write(replacementId);
            writer.flush();
            FriendlyByteBuf obfuscated = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
            try {
                PalettedContainer<BlockState> decoded = container();
                decoded.read(obfuscated);
                assertSame(replacement, decoded.get(0));
            } finally { obfuscated.release(); }
            states.set(0, 0, 0, Blocks.WATER.defaultBlockState());
            assertEquals(paletteSize, info.getPalette(0).getSize());
        } finally { buffer.release(); }
    }

    private static volatile BlockState allocationSink;

    @Test
    void getBlockStateAndFlatMutatorsAllocateNoBytesAfterWarmup() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported());
        bean.setThreadAllocatedMemoryEnabled(true);
        PalettedContainer<BlockState> states = container();
        states.set(0, 0, 0, Blocks.STONE.defaultBlockState());
        LevelChunkSection section = new LevelChunkSection(states, null);
        for (int i = 0; i < 20; ++i) measureContainerAllocations(bean, section, states);
        for (int i = 0; i < 5; ++i) assertEquals(0, measureContainerAllocations(bean, section, states));
    }

    private static long measureContainerAllocations(com.sun.management.ThreadMXBean bean, LevelChunkSection section, PalettedContainer<BlockState> states) {
        BlockState stone = Blocks.STONE.defaultBlockState(), dirt = Blocks.DIRT.defaultBlockState();
        long before = bean.getCurrentThreadAllocatedBytes();
        BlockState result = stone;
        for (int i = 0; i < 100_000; ++i) {
            int x = i & 15, y = i >> 8 & 15, z = i >> 4 & 15;
            result = section.getBlockState(x, y, z);
            states.set(x, y, z, stone);
            result = states.getAndSet(x, y, z, dirt);
        }
        allocationSink = result;
        return bean.getCurrentThreadAllocatedBytes() - before;
    }

    @Test
    void homogeneousOreLoadedWithPresetsStillAllowsAntiXrayRewriting() {
        BlockState ore = Blocks.DIAMOND_ORE.defaultBlockState(), stone = Blocks.STONE.defaultBlockState();
        PalettedContainerRO.PackedData<BlockState> packed = new PalettedContainerRO.PackedData<>(List.of(ore), java.util.Optional.empty(), 0);
        PalettedContainer<BlockState> states = PalettedContainer.unpack(strategy(), packed,
            Blocks.AIR.defaultBlockState(), new BlockState[]{stone}).result().orElseThrow();
        assertTrue(((WaterSectionStorage)states.data.storage()).isCompact());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            states.write(buffer, null, 0);
            assertTrue(buffer.readUnsignedByte() > 0);
            int size = buffer.readVarInt();
            boolean hasOre = false, hasStone = false;
            for (int i = 0; i < size; ++i) {
                int id = buffer.readVarInt();
                hasOre |= id == Block.BLOCK_STATE_REGISTRY.getId(ore);
                hasStone |= id == Block.BLOCK_STATE_REGISTRY.getId(stone);
            }
            assertTrue(hasOre && hasStone);
        } finally { buffer.release(); }
    }
    @Test
    void packetSnapshotsAreReusedUntilAnyFlatMutationAndCopiesAreIndependent() throws Exception {
        PalettedContainer<BlockState> states = container();
        var method = PalettedContainer.class.getDeclaredMethod("networkData"); method.setAccessible(true);
        Object first = method.invoke(states); assertSame(first, method.invoke(states));
        states.set(0, 0, 0, Blocks.AIR.defaultBlockState()); assertSame(first, method.invoke(states));
        states.set(0, 0, 0, Blocks.STONE.defaultBlockState());
        Object second = method.invoke(states); assertNotSame(first, second); assertSame(second, method.invoke(states));
        states.data.storage().set(123, Block.BLOCK_STATE_REGISTRY.getId(Blocks.WATER.defaultBlockState()));
        Object third = method.invoke(states); assertNotSame(second, third);
        PalettedContainer<BlockState> copy = states.copy();
        copy.set(0, 0, 0, Blocks.DIRT.defaultBlockState()); assertSame(third, method.invoke(states));
        FriendlyByteBuf wire = new FriendlyByteBuf(Unpooled.buffer());
        try {
            states.write(wire, null, 0); PalettedContainer<BlockState> decoded = container(); decoded.read(wire);
            assertSame(Blocks.STONE.defaultBlockState(), decoded.get(0));
            assertSame(Blocks.WATER.defaultBlockState(), decoded.get(123));
        } finally { wire.release(); }
    }

    @Test
    void uniformDiskPackingUsesOneEntryWithoutGlobalSizedTransientPalette() {
        PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.STONE.defaultBlockState(), strategy(), null);
        var packed = states.pack(strategy());
        assertEquals(List.of(Blocks.STONE.defaultBlockState()), packed.paletteEntries());
        assertTrue(packed.storage().isEmpty());
        assertTrue(((WaterSectionStorage) states.data.storage()).isCompact());
    }
    private static volatile int packetSizeSink;
    @Test
    void warmedSectionSizeAndBlockReadsAllocateZeroBytes() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported());
        bean.setThreadAllocatedMemoryEnabled(true);
        PalettedContainer<BlockState> states = container(); states.set(1, 2, 3, Blocks.STONE.defaultBlockState());
        states.getSerializedSize();
        for (int i = 0; i < 20; ++i) measurePacketSize(bean, states);
        for (int i = 0; i < 5; ++i) assertEquals(0L, measurePacketSize(bean, states));
        System.out.println("WATER_SECTION_PACKET_CACHE warmed_size_and_reads_bytes=0");
    }
    private static long measurePacketSize(com.sun.management.ThreadMXBean bean, PalettedContainer<BlockState> states) {
        long before = bean.getCurrentThreadAllocatedBytes(); int sum = 0;
        for (int i = 0; i < 10000; ++i) { sum += states.getSerializedSize(); if (states.get(1, 2, 3) != Blocks.STONE.defaultBlockState()) throw new AssertionError(); }
        packetSizeSink = sum; return bean.getCurrentThreadAllocatedBytes() - before;
    }
    @Test
    void blockMutationsAfterPacketCachingRemainAllocationFree() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported()); bean.setThreadAllocatedMemoryEnabled(true);
        PalettedContainer<BlockState> states = container(); states.set(1, 2, 3, Blocks.STONE.defaultBlockState());
        for (int i = 0; i < 20; ++i) { states.getSerializedSize(); measureCachedMutation(bean, states); }
        for (int i = 0; i < 5; ++i) { states.getSerializedSize(); assertEquals(0L, measureCachedMutation(bean, states)); }
        System.out.println("WATER_SECTION_PACKET_CACHE warmed_mutation_bytes=0");
    }
    private static long measureCachedMutation(com.sun.management.ThreadMXBean bean, PalettedContainer<BlockState> states) {
        BlockState air = Blocks.AIR.defaultBlockState(), stone = Blocks.STONE.defaultBlockState();
        long before = bean.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < 10000; ++i) states.getAndSet(1, 2, 3, (i & 1) == 0 ? air : stone);
        return bean.getCurrentThreadAllocatedBytes() - before;
    }
}
