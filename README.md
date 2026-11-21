<img src="public/image/watermc_banner_v2.png" alt="WaterMc">
<div align="center">

[![Download](https://img.shields.io/github/v/release/s0nd3r-py/WaterMc?label=download&style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://github.com/s0nd3r-py/WaterMc/releases/latest)⠀
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.11-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://github.com/s0nd3r-py/WaterMc/releases)⠀
[![Java](https://img.shields.io/badge/java-21-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://adoptium.net/)⠀
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](LICENSE.md)

**WaterMc** is a [Leaf](https://github.com/Winds-Studio/Leaf) fork for Minecraft 1.21.11, tuned for fast world generation, fast startup and crowded servers.
</div>

> [!WARNING]
> WaterMc moves server files into a new layout on the first start (`config/`, `data/`, `worlds/`). Take a backup **before** switching to it.

**English** | [Русский](public/readme/README_RU.md)

## 💧 Features
- **Based on [Leaf](https://github.com/Winds-Studio/Leaf)** (Paper → Purpur → Gale → Leaf): Bukkit, Spigot and Paper plugins work unchanged
- **Faster world generation**: more chunks per second than Leaf, and much faster spawn point selection on new worlds
- **Fast startup**: own launcher, lazy DataFixerUpper, own plugin remapper cache
- **Crowds and farms**: cheaper entity pushing, smarter mob AI, fewer wasted block searches
- **Built-in ViaVersion and ViaBackwards** (optional), your own copy in `plugins/` always wins
- **Raytrace entity culling** (optional): entities hidden behind walls are not sent, which also blinds ESP cheats
- **Clean server folder**: `config/`, `data/`, `worlds/`, `logs/` instead of thirty files in the root
- **Admin tools**: `/watermc inspect`, `/watermc drain`, plugin and Netty watchdogs, security audit
- **WaterScript**: built-in scripting with events, commands, timers and variables
- **Fixes**: villager trade item dupe (1.21.6+), End portal/platform ignoring `BlockDestroyEvent` cancellation

### 🚀 Startup and plugin loading
- **Own launcher** (`me.s0nd3r.watermc.launcher`) replaces the stock bundler bootstrap. It remembers the size, modification time and SHA-256 of every library in `libraries/.watermc-launcher` and skips files that did not change, so a warm start does not re-hash the whole `libraries/` folder. On Java older than 21 it prints a clear "Java 21 required" message instead of a stack trace.
- **Own plugin remapper cache** (`plugins/.water-remapped`). Spigot plugins are remapped to Mojang names as in Paper, but the jar hashes are cached in `hashes.tsv`, the index is rewritten only when it changes, and plugins that only use the Bukkit/Paper API are not remapped at all.
- **Lazy mappings**: the Spigot↔Mojang mapping tables (tens of MB) are read only when the first plugin actually needs them. With modern plugins they are never loaded.
- **Lazy DataFixerUpper**: conversion schemas are built only when the server meets data from an older Minecraft version.
- **No GUI**: the Swing/AWT console window and its whole class tree are removed.

### 🧩 Taken from mods and other forks
| Source | What WaterMc uses |
|---|---|
| [Lithium](https://github.com/CaffeineMC/lithium) | Brain memory change counter (`Behavior.hasRequiredMemories` is cached until a memory appears or disappears), non-POI block searches for `MoveToBlockGoal`, `RemoveBlockGoal`, hoglin and piglin sensors, retracting piston collision shapes |
| [DivineMC](https://github.com/BX-Team/DivineMC) | Raytrace entity culling, parallel sensor phase, portal search prefetch, async `/locate`, command block parse cache |
| [ModernFix](https://github.com/embeddedt/ModernFix) | Early stronghold reject: chunks that can never hold a stronghold skip the ring calculation |
| [ServerCore](https://github.com/Wesley1808/ServerCore) | Breeding cap and dynamic view/simulation distance by MSPT |
| [Krypton](https://github.com/astei/krypton) | Strings are encoded straight into the packet buffer (`Utf8String.write`) without a temporary buffer |
| [FerriteCore](https://github.com/malte0811/FerriteCore) | An emptied item component patch (`PatchedDataComponentMap`) shares one immutable map |
| [threadtweak](https://github.com/getchoo/threadtweak) | Chunk worker and I/O threads can run below the main thread priority |
| Leaf 26.x branches | Entity activation through a k-d tree, max stack size cache, `PoiAccess` visited-section bitset, chunk packet heightmaps in `EnumMap`, faster `ClientboundCommandsPacket`, block destruction cancellation fix and more |

All ported code was rewritten for the 1.21.11 base; authors and licenses are listed in [`docs/inherited-attribution.md`](docs/inherited-attribution.md).

### 🔧 Classes rewritten in WaterMc
- `Climate.RTree`: biome search keeps node bounds in one flat `long[14]` with an unrolled distance loop; results match vanilla bit for bit
- `MoonriseCommon`: new chunk worker formula (4 cores → 2 threads, 8 → 5, 16 → 9), and all cores generate chunks while worlds are prepared at startup
- `ConcentricRingsStructurePlacement`: radial bounds of stronghold rings computed once per placement
- `ChunkEntitySlices`: section entity lists keep a flat `double[]` copy of bounding boxes, so neighbour searches do not touch entity objects
- `LevelTicks`: scheduled ticks remember the nearest due time and skip whole container scans until then
- `LivingEntity` / `Entity`: health read from a plain field, `getTeam()` answers at once for mobs without a team, push queries stop once enough neighbours are found
- `ExperienceOrb`: merging stops at the first match and looks orbs up by entity type
- `ActivationRange`, `Brain`, `Behavior`, `MoveToBlockGoal`, `PatchedDataComponentMap`, `Utf8String`, `PalettedContainer.getAll`
- Player data lists (`ops`, `whitelist`, bans, user cache) moved from JSON files to an append-only journal `data/*.wdb` with CRC32C per record

### 📦 Libraries
- **Guava Cache → [Caffeine](https://github.com/ben-manes/caffeine) 3.2.3** everywhere in the server: profiles and skins, NBT encoding cache, block shapes, block patterns and more
- `SimpleDateFormat` → `DateTimeFormatter` (one shared formatter in `PlayerList` was used from several threads), `StringBuffer` → `StringBuilder`, `IntArraySet` → `IntOpenHashSet` in `PalettedContainer.getAll`
- Up-to-date stack: Netty 4.2.15, fastutil 8.5.15, Log4j 2.25.3, ASM 9.9.1, LMAX Disruptor 4.0.0, zstd-jni
- Region files compressed by native libdeflate (Velocity natives), optional ZSTD regions

## 📥 Download
Get the latest build from [GitHub Releases](https://github.com/s0nd3r-py/WaterMc/releases/latest). On the first start the jar downloads the vanilla server from Mojang and applies WaterMc on top of it, the same way Paper and Leaf do.

```bash
java -Xms4G -Xmx4G -jar watermc-paperclip-1.21.11.jar --nogui
```

## 📄 Documentation
- [What WaterMc changes compared to Leaf](docs/about-watermc.md)
- [Configuration keys added in this release](docs/release-description.md)
- [WaterScript](docs/scripting.md)

## 📦 Building
WaterMc keeps its Minecraft changes as patches against Leaf, so no Mojang code is stored in this repository. You need JDK 21 and Git.

```bash
./scripts/setup-source.sh      # Windows: .\scripts\setup-source.ps1
./gradlew :watermc-server:createMojmapPaperclipJar
```

`setup-source` checks out Leaf at the pinned commit, lets Leaf's own tooling decompile Minecraft and apply its patches, and then applies [`patches/`](patches) on top.

## ⚖️ License
WaterMc is licensed under [GPL-3.0](LICENSE.md). Code taken from upstream projects keeps its original license and authorship; see [`licenses/`](licenses) and [`docs/inherited-attribution.md`](docs/inherited-attribution.md).

## 📜 Credits
WaterMc stands on the work of these projects:
- [Leaf](https://github.com/Winds-Studio/Leaf), [Gale](https://github.com/GaleMC/Gale), [Purpur](https://github.com/PurpurMC/Purpur), [Paper](https://github.com/PaperMC/Paper)
- [DivineMC](https://github.com/BX-Team/DivineMC), [Lithium](https://github.com/CaffeineMC/lithium), [ModernFix](https://github.com/embeddedt/ModernFix), [ServerCore](https://github.com/Wesley1808/ServerCore), [Krypton](https://github.com/astei/krypton), [FerriteCore](https://github.com/malte0811/FerriteCore), [threadtweak](https://github.com/getchoo/threadtweak)
- [ViaVersion](https://github.com/ViaVersion/ViaVersion) and [ViaBackwards](https://github.com/ViaVersion/ViaBackwards), [OcclusionCulling](https://github.com/LogisticsCraft/OcclusionCulling)
