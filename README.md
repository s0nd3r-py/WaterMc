<img src="public/image/watermc_banner.png" alt="WaterMc">
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
- **Fast startup**: own launcher, lazy DataFixerUpper, mappings loaded only when a plugin needs them
- **Crowds and farms**: cheaper entity pushing in mob crowds, Lithium brain-memory cache and non-POI block searches
- **Built-in ViaVersion and ViaBackwards** (optional), your own copy in `plugins/` always wins
- **Raytrace entity culling** (optional): entities hidden behind walls are not sent, which also blinds ESP cheats
- **Clean server folder**: `config/`, `data/`, `worlds/`, `logs/` instead of thirty files in the root
- **Admin tools**: `/watermc inspect`, `/watermc drain`, plugin and Netty watchdogs, security audit that names the plugin behind every OP grant or console command
- **WaterScript**: built-in scripting with events, commands, timers and variables
- **Fixes**: villager trade item dupe (1.21.6+), End portal/platform ignoring `BlockDestroyEvent` cancellation

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
