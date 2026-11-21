import com.mojang.serialization.Dynamic;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.watermc.bootstrap.LazyDataFixer;

/** Run against the extracted final distribution, not Gradle's loose classes. */
public final class PackagedCoreProbe {
    private record Metadata(String parent, String[] interfaces, java.util.Set<String> fields) {}
    private static final java.util.Map<String, Metadata> TYPES = new java.util.HashMap<>();
    private static final java.util.Map<String, String> FIELD_OWNERS = new java.util.HashMap<>();
    private static Metadata metadata(String name) {
        return TYPES.computeIfAbsent(name, key -> {
            try (var input = PackagedCoreProbe.class.getClassLoader().getResourceAsStream(key + ".class")) {
                if (input == null) throw new IllegalStateException("Missing field-owner metadata: " + key);
                var reader = new org.objectweb.asm.ClassReader(input);
                var fields = new java.util.HashSet<String>();
                reader.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                    @Override public org.objectweb.asm.FieldVisitor visitField(int access, String field, String descriptor, String signature, Object value) {
                        fields.add(field + descriptor); return null;
                    }
                }, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG | org.objectweb.asm.ClassReader.SKIP_FRAMES);
                return new Metadata(reader.getSuperName(), reader.getInterfaces(), fields);
            } catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
        });
    }
    private static String findField(String owner, String field, String descriptor) {
        Metadata type = metadata(owner);
        if (type.fields.contains(field + descriptor)) return owner;
        for (String face : type.interfaces) {
            String resolved = findField(face, field, descriptor);
            if (resolved != null) return resolved;
        }
        return type.parent == null ? null : findField(type.parent, field, descriptor);
    }
    private static String fieldOwner(String owner, String field, String descriptor) {
        return FIELD_OWNERS.computeIfAbsent(owner + ":" + field + descriptor, key -> {
            String resolved = findField(owner, field, descriptor);
            if (resolved == null) throw new IllegalStateException("Unresolved field: " + key);
            return resolved;
        });
    }
    private static byte[] canonical(byte[] bytes) {
        var writer = new org.objectweb.asm.ClassWriter(0);
        // Paperweight retargets inherited field refs to their declaring owner. Resolve both
        // representations by JVM field lookup; field names/types/opcodes and all attributes stay.
        new org.objectweb.asm.ClassReader(bytes).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9, writer) {
            @Override public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    @Override public void visitFieldInsn(int opcode, String owner, String field, String type) {
                        super.visitFieldInsn(opcode, fieldOwner(owner, field, type), field, type);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        int compared = 0;
        try (var jar = new java.util.zip.ZipFile(args[0])) {
            for (String name : java.nio.file.Files.readAllLines(java.nio.file.Path.of(args[2]))) {
                byte[] compiled = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(args[1]).resolve(name));
                byte[] packaged;
                try (var input = jar.getInputStream(jar.getEntry(name))) { packaged = input.readAllBytes(); }
                if (!Arrays.equals(canonical(compiled), canonical(packaged))) throw new AssertionError("Packaged class structure differs: " + name);
                ++compared;
            }
        }
        System.out.println("WATER_PACKAGED_CLASSES asm_canonical_match=" + compared);
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        LazyDataFixer fixer = (LazyDataFixer) DataFixers.getDataFixer();
        if (fixer.isInitialized()) throw new AssertionError("DFU initialized by modern Bootstrap");
        int current = net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version();
        Dynamic<?> modern = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
        if (fixer.update(References.CHUNK, modern, current, current) != modern) throw new AssertionError("Modern data copied");
        DataFixers.optimize(Set.of(References.CHUNK)).join();
        if (fixer.isInitialized()) throw new AssertionError("DFU initialized by modern update/optimize");
        CompoundTag options = new CompoundTag(); options.putString("lang", "EN_US");
        var converted = fixer.update(References.OPTIONS, new Dynamic<>(NbtOps.INSTANCE, options), 500, current);
        if (!converted.get("lang").asString("").equals("en_us") || !fixer.isInitialized()) throw new AssertionError("Legacy language conversion failed");
        CompoundTag item = new CompoundTag(); item.putShort("id", (short) 1); item.putByte("Count", (byte) 1); item.putShort("Damage", (short) 0);
        var stack = fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, item), 99, current);
        if (!stack.get("id").asString("").equals("minecraft:stone") || stack.get("count").asInt(0) != 1) throw new AssertionError("Legacy item conversion failed: " + stack);
        System.out.println("WATER_PACKAGED_DFU modern_lazy=true legacy_options=en_us legacy_item=minecraft:stone count=1 legacy_initialized=true");
        byte[] payload = new byte[16384]; new java.util.Random(121126).nextBytes(payload);
        for (var format : new RegionFileVersion[]{RegionFileVersion.VERSION_ZSTD, RegionFileVersion.VERSION_DEFLATE}) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (var out = format.wrap(bytes)) { out.write(payload); }
            try (var in = format.wrap(new ByteArrayInputStream(bytes.toByteArray()))) {
                if (!Arrays.equals(payload, in.readAllBytes())) throw new AssertionError("Packaged codec mismatch");
            }
        }
        Class.forName("org.jctools.queues.MpscUnboundedArrayQueue");
        if (org.watermc.bootstrap.LegacyPluginRemapping.enabled()) throw new AssertionError("Remapper unexpectedly enabled");
        System.out.println("WATER_PACKAGED_CODECS zstd_native_roundtrip=true zlib_provider=" + com.velocitypowered.natives.util.Natives.compress.getLoadedVariant() + " jctools_present=true legacy_remapper=false");
    }
}
