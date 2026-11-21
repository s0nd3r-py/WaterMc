"""Verify final bundle/class bytes and cold/lazy DFU with actual historical conversions."""
import argparse
import hashlib
import io
import json
from pathlib import Path
import subprocess
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument("--jar", required=True, type=Path)
parser.add_argument("--output", required=True, type=Path)
parser.add_argument("--java-home", required=True, type=Path)
args = parser.parse_args()
jar = args.jar.resolve()
output = args.output.resolve()
output.mkdir(parents=True, exist_ok=True)
runtime = output / "runtime"
runtime.mkdir(exist_ok=True)
classpath = []
verified = []
root = Path(__file__).resolve().parents[2]
with zipfile.ZipFile(jar) as bundle:
    server_path = "META-INF/versions/1.21.11/watermc-1.21.11.jar"
    with zipfile.ZipFile(io.BytesIO(bundle.read(server_path))) as server:
        for name in (
            "org/watermc/bootstrap/LazyDataFixer", "org/watermc/bootstrap/LegacyPluginRemapping",
            "org/watermc/storage/WaterSectionStorage", "org/watermc/storage/SectionPacketCache",
            "org/watermc/storage/NativeRegionOutputStream", "org/watermc/network/SharedPacketCompressionCache",
            "net/minecraft/network/CompressionEncoder", "net/minecraft/network/CompressionDecoder",
            "net/minecraft/network/Connection", "net/minecraft/network/CipherEncoder",
            "net/minecraft/network/Varint21LengthFieldPrepender", "net/minecraft/SharedConstants",
            "net/minecraft/world/level/chunk/PalettedContainer", "net/minecraft/server/MinecraftServer",
            "net/minecraft/util/datafix/DataFixers", "net/minecraft/world/entity/Mob"):
            path = name + ".class"
            compiled = root / "watermc-server/build/classes/java/main" / path
            assert path in server.namelist() and compiled.is_file(), "Missing packaged/compiled class: " + path
            verified.append(path)
        assert "org/dreeam/leaf/world/EntityCollisionCache.class" not in server.namelist()
    for name in bundle.namelist():
        if name.endswith(".jar") and (name.startswith("META-INF/libraries/") or name == server_path):
            target = (runtime / name).resolve()
            if not target.is_relative_to(runtime):
                raise ValueError("Invalid bundled path")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(bundle.read(name))
            classpath.append(target.as_posix())
classpath.sort(key=lambda name: 0 if "/META-INF/versions/" in name else 1)
classpath_text = ";".join(classpath)
class_list = output / "verify-classes.txt"
class_list.write_text("\n".join(verified) + "\n", encoding="utf-8")
classes = output / "probe-classes"
classes.mkdir(exist_ok=True)
def quoted(path):
    return '"' + str(path).replace("\\", "/") + '"'
compile_args = output / "javac.args"
compile_args.write_text("-proc:none\n-encoding UTF-8\n-classpath\n" + quoted(classpath_text) + "\n-d\n" + quoted(classes) + "\n" + quoted(Path(__file__).with_name("PackagedCoreProbe.java")) + "\n", encoding="utf-8")
run_args = output / "java.args"
run_args.write_text("-Xmx1G\n--add-modules=jdk.incubator.vector\n--enable-native-access=ALL-UNNAMED\n-Djava.io.tmpdir=D:/water-mc/tmp\n-classpath\n" + quoted(classes.as_posix() + ";" + classpath_text) + "\nPackagedCoreProbe\n" + quoted(runtime / server_path) + "\n" + quoted(root / "watermc-server/build/classes/java/main") + "\n" + quoted(class_list) + "\n", encoding="utf-8")
with (output / "probe.log").open("w", encoding="utf-8") as log:
    subprocess.run([str(args.java_home / "bin/javac.exe"), "@" + str(compile_args)], cwd=output, stdout=log, stderr=subprocess.STDOUT, check=True, timeout=120)
    subprocess.run([str(args.java_home / "bin/java.exe"), "@" + str(run_args)], cwd=output, stdout=log, stderr=subprocess.STDOUT, check=True, timeout=300)
summary = {"artifact": str(jar), "sha256": hashlib.sha256(jar.read_bytes()).hexdigest(), "verified_packaged_classes": verified, "probe_passed": True, "class_comparison": "ASM canonical class bytes (all attributes retained; constant-pool layout and JVM declaring field owners normalized)"}
(output / "summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
print(json.dumps({"probe_passed": True, "verified_classes": len(verified), "sha256": summary["sha256"]}))
