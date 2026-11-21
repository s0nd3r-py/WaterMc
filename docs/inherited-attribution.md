# Авторство унаследованных изменений

Ниже сохранены оригинальные заголовки прежних патчей для указания авторства и лицензий. Исполняемые исходники хранятся напрямую. Это архив происхождения, а не действующий процесс разработки. Историческое утверждение об отличии FMA не более чем на 1 ulp исправлено в актуальном Java-коде: при компенсации и переполнении отличие может быть больше.

## leaf-api/paper-patches/features/0001-Purpur-API-Changes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Github Actions <no-reply@github.com>
Date: Wed, 7 Jan 2026 23:55:32 +0000
Subject: [PATCH] Purpur API Changes

Original license: MIT
Original project: https://github.com/PurpurMC/Purpur

Commit: f57bd8659b5c3cbaf03984ee5a8ecdcc0849040a

Patches listed below are removed in this patch, They exists in Gale or Leaf:
* "co/aikar/timings/TimedEventExecutor.java.patch"
* "co/aikar/timings/Timing.java.patch"
* "co/aikar/timings/Timings.java.patch"
* "co/aikar/timings/TimingsCommand.java.patch"
* "com/destroystokyo/paper/util/VersionFetcher.java.patch"
* "org/bukkit/command/defaults/VersionCommand.java.patch"
* "org/bukkit/command/SimpleCommandMap.java.patch" (Timings changes)
* "org/bukkit/plugin/java/JavaPluginLoader.java.patch"
* "org/bukkit/plugin/java/LibraryLoader.java.patch"
* "org/spigotmc/CustomTimingsHandler.java.patch"
```

## leaf-api/paper-patches/features/0002-Rebrand.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 21 May 2024 11:43:49 +0800
Subject: [PATCH] Rebrand

This patch is referring to the following patch:
"Gale branding changes"
By: Martijn Muijsers <martijnmuijsers@live.nl>
As part of: GaleMC (https://github.com/GaleMC/Gale)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-api/paper-patches/features/0003-Gale-configuration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 21:03:04 +0100
Subject: [PATCH] Gale configuration

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Timings v2"
By: Aikar <aikar@aikar.co>
As part of: Paper (https://github.com/PaperMC/Paper)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-api/paper-patches/features/0004-SIMD-support.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 01:21:32 +0100
Subject: [PATCH] SIMD support

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add SIMD utilities"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-api/paper-patches/features/0005-Do-not-log-plugin-library-loads.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 8 Aug 2023 18:39:55 +0200
Subject: [PATCH] Do not log plugin library loads

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add log suppression for LibraryLoader"
By: Krakenied <Krakenied1@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-api/paper-patches/features/0006-Player-canSee-by-entity-UUID.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 23:38:12 +0100
Subject: [PATCH] Player#canSee by entity UUID

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Spread out and optimise player list ticksSpread out and optimise player list ticks"
By: James Lyne <jim+github@not-null.co.uk>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-api/paper-patches/features/0007-Specific-interval-TPS-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 12:57:37 +0100
Subject: [PATCH] Specific interval TPS API

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-api/paper-patches/features/0008-5-second-TPS-average.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 13:00:40 +0100
Subject: [PATCH] 5 second TPS average

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add 5 second tps average in /tps"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-api/paper-patches/features/0009-Last-tick-time-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 20:49:15 +0100
Subject: [PATCH] Last tick time API

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add getLastTickMs api"
By: tr7zw <tr7zw@live.de>
As part of: YAPFA (https://github.com/tr7zw/YAPFA)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* YAPFA copyright *

Copyright 2020 tr7zw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-api/paper-patches/features/0010-Pufferfish-Sentry.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Kevin Raneri <kevin.raneri@gmail.com>
Date: Tue, 9 Nov 2021 14:01:56 -0500
Subject: [PATCH] Pufferfish: Sentry

Original license: GPL-3.0-only
Original project: https://github.com/pufferfish-gg/Pufferfish
```

## leaf-api/paper-patches/features/0011-Remove-Timings.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 23 Sep 2023 03:38:21 -0400
Subject: [PATCH] Remove Timings

Completely remove the Timings, since it wastes too much performance. Use Spark instead.
```

## leaf-api/paper-patches/features/0012-KeYi-Player-Skull-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalgic853 <yuu8583@proton.me>
Date: Sun, 20 Nov 2022 00:20:01 +0800
Subject: [PATCH] KeYi: Player Skull API

Original license: MIT
Original project: https://github.com/KeYiMC/KeYi
```

## leaf-api/paper-patches/features/0013-Slice-Smooth-Teleports.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Cryptite <cryptite@gmail.com>
Date: Sat, 13 Aug 2022 08:58:21 -0500
Subject: [PATCH] Slice: Smooth Teleports

Original license: MIT
Original project: https://github.com/Cryptite/Slice

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-api/paper-patches/features/0014-Better-warning-message-for-discouraging-Maven-Centra.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 30 Jun 2025 02:41:48 +0800
Subject: [PATCH] Better warning message for discouraging Maven Central as CDN

Dreeam's Note:
Also see `Configurable LibraryLoader maven repos` patch,
able to match googleapi mirror urls, to replace to repo url we custom defined.
This able to switch back to maven central repo, to prevent
situation that dependencies exist in maven central, but not in mirror.
```

## leaf-api/paper-patches/features/0015-Configurable-LibraryLoader-maven-repos.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 3 Feb 2024 18:45:53 -0500
Subject: [PATCH] Configurable LibraryLoader maven repos

Add JVM flag `-DLeaf.library-download-repo=link` to choose library download repo link.
e.g. `-DLeaf.library-download-repo=https://maven.aliyun.com/repository/public`
```

## leaf-api/paper-patches/features/0016-Leaves-Replay-Mod-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Lumine1909 <133463833+Lumine1909@users.noreply.github.com>
Date: Sat, 17 May 2025 17:36:22 +0800
Subject: [PATCH] Leaves: Replay Mod API

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by ReplayMod (https://github.com/ReplayMod)
```

## leaf-api/paper-patches/features/0017-PlayerInventoryOverflowEvent.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 19 Feb 2025 00:34:16 -0500
Subject: [PATCH] PlayerInventoryOverflowEvent
```

## leaf-api/paper-patches/features/0018-Raytrace-AntiXray-SDK-integration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: LeeGodSRC <lee20040919@gmail.com>
Date: Sat, 8 Mar 2025 11:40:46 +0800
Subject: [PATCH] Raytrace AntiXray SDK integration

Integrated with Imanity Software's Raytrace AntiXray for better performance

Original project: https://github.com/Imanity-Software/raytrace-antixray-spigot-sdk
```

## leaf-api/paper-patches/features/0019-Fish-Parallel-World-Ticking-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Biquaternions <biquaternions@serlith.net>
Date: Tue, 2 Sep 2025 13:54:37 -0500
Subject: [PATCH] Fish: Parallel World Ticking API

Original license: MIT
Original project: https://github.com/Biquaternions/Fish

This patch provides an API for performance monitoring plugins like PurpurBars.
A better approach would be to include an event when the world is done ticking,
  which will allow to re-use the RollingAverage logic from Spark and Minecraft internals.
However, since every fork developer will want to have said event into their own namespace,
  it will be virtually impossible to provide a universal API.
With this approach only new methods are added into already existing Bukkit API, and the same
  naming conventions as Bukkit were used, which means there's a bigger chance of this API to
  get standarized.
```

## leaf-api/paper-patches/features/0020-Disable-yaml-duplicate-keys-warning-by-default.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 16 Dec 2025 04:44:10 -0500
Subject: [PATCH] Disable yaml duplicate keys warning by default

Disable Yaml's warnOnDuplicateKeys by deafault to
follow the original behavior

It's just a warning output introduced since snakeyaml 2.4,
See https://github.com/snakeyaml/snakeyaml/commit/b16f9485f33136fb30f8472f3ccf0209707c023e

The comment of LoaderOptions#setWarnOnDuplicateKeys is
actually misleading, since this warnOnDuplicateKeys option
is enabled by default when creates new Yaml instance
```

## leaf-api/paper-patches/features/0021-Cache-namespacedKey-toString-and-hash.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Fri, 9 Jan 2026 02:40:25 -0500
Subject: [PATCH] Cache namespacedKey toString and hash

Cache toString and hash to reduce allocations.
Use lazy cache here to prevent cache inconsistency caused by
operations that don't use the constructor, e.g., Gson serialization or Reflection.
```

## leaf-api/paper-patches/features/0022-Fix-paper-tests.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 15 Jan 2026 18:07:51 -0500
Subject: [PATCH] Fix paper tests

snakeyaml deprecated the external util class `Base64Coder` since snakeyaml 2.3
See: https://github.com/snakeyaml/snakeyaml/commit/3f0bedad5917490a1f7ef56a0920b5b93b214ade

It replaced it using Java's built-in Base64 util (java.util.Base64), which is better and more standard.

In here, I use Base64.getMimeDecoder() for the base64 decoding, to be compatible with old data that is using the old
standard (the Base64Coder is using MIME type).
See: https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html#:~:text=MIME

And use the basic Base64 encoder to encode the data.
```

## leaf-api/paper-patches/features/0023-Replace-data-maps-with-optimized-collection.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Mon, 27 Apr 2026 21:14:18 +0300
Subject: [PATCH] Replace data maps with optimized collection
```

## leaf-server/paper-patches/features/0001-Purpur-Server-Paper-Changes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Github Actions <no-reply@github.com>
Date: Wed, 7 Jan 2026 23:55:32 +0000
Subject: [PATCH] Purpur Server Paper Changes

Original license: MIT
Original project: https://github.com/PurpurMC/Purpur

Commit: f57bd8659b5c3cbaf03984ee5a8ecdcc0849040a

Patches listed below are removed in this patch, They exists in Gale or Leaf:
* "Rebrand.patch"
* "com/destroystokyo/paper/Metrics.java.patch"
    - Purpur config files
* "com/destroystokyo/paper/console/PaperConsole.java.patch"
    - Rebrand
* "com/destroystokyo/paper/gui/RAMDetails.java.patch"
    - Add 5 second tps average in /tps
* "io/papermc/paper/ServerBuildInfoImpl.java.patch"
    - Rebrand
* "org/bukkit/craftbukkit/legacy/CraftLegacy.java.patch"
    - Logger settings (suppressing pointless logs)
* "org/bukkit/craftbukkit/scheduler/CraftScheduler.java.patch"
    - Rebrand
* "org/bukkit/craftbukkit/util/CraftMagicNumbers.java.patch"
    - use old Paper version checker
* "org/spigotmc/TicksPerSecondCommand.java.patch"
    - Add 5 second tps average in /tps
* "org/spigotmc/WatchdogThread.java.patch"
    - Rebrand
* "src/main/resources/logo.png"
```

## leaf-server/paper-patches/features/0002-Rebrand.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 16 Sep 2021 20:39:45 -0400
Subject: [PATCH] Rebrand

This patch is referring to the following patch:
"Gale branding changes"
By: Martijn Muijsers <martijnmuijsers@live.nl>
As part of: GaleMC (https://github.com/GaleMC/Gale)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/paper-patches/features/0003-Gale-metrics.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 13:47:57 +0100
Subject: [PATCH] Gale metrics

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0004-Gale-semantic-version.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 9 Aug 2023 19:04:22 +0200
Subject: [PATCH] Gale semantic version

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0005-Gale-configuration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 21:05:47 +0100
Subject: [PATCH] Gale configuration

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Paper config files"
By: Jake Potrebic <jake.m.potrebic@gmail.com>
As part of: Paper (https://github.com/PaperMC/Paper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/paper-patches/features/0006-Set-Gale-permissions-root.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 18:42:45 +0100
Subject: [PATCH] Set Gale permissions root

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0007-Leaf-Commands.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 4 Nov 2024 23:07:27 -0500
Subject: [PATCH] Leaf Commands
```

## leaf-server/paper-patches/features/0008-Leaf-JUnit-test-suite.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 12 Jul 2025 15:33:40 +0800
Subject: [PATCH] Leaf JUnit test suite
```

## leaf-server/paper-patches/features/0009-Simpler-ShapelessRecipe-comparison-for-vanilla.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 16:22:47 +0100
Subject: [PATCH] Simpler ShapelessRecipe comparison for vanilla

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Simpler ShapelessRecipes comparison for Vanilla"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane description *

Paper added a fancy sorting comparison due to Bukkit recipes breaking
the vanilla one, however this is far more advanced than what you need
for all the vanilla recipes.

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

## leaf-server/paper-patches/features/0010-Print-stack-trace-for-plugins-not-shutting-down-task.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 20:28:07 +0100
Subject: [PATCH] Print stack trace for plugins not shutting down tasks

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"More debug for plugins not shutting down tasks"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
```

## leaf-server/paper-patches/features/0011-SIMD-support.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 01:19:12 +0100
Subject: [PATCH] SIMD support

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add SIMD utilities"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/paper-patches/features/0012-Make-book-writing-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 02:11:29 +0100
Subject: [PATCH] Make book writing configurable

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add option to disable books"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

Books are commonly the target of duping-related exploits. If you
anticipate that your server will be an early target of duping attacks in
the event that new exploits are found, you may want to consider removing
the ability for non-privileged players to edit books. This patch allows
you to easily disable books, should you want to preemptively remove this
functionality before additional exploits are found.
```

## leaf-server/paper-patches/features/0013-Do-not-log-legacy-Material-initialization.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 15:03:50 +0100
Subject: [PATCH] Do not log legacy Material initialization

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Logger settings (suppressing pointless logs)"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/paper-patches/features/0014-Do-not-log-Not-Secure-marker.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 15:13:38 +0100
Subject: [PATCH] Do not log Not Secure marker

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0015-Reduce-array-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 11:25:45 +0100
Subject: [PATCH] Reduce array allocations

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

Enum's values returns anew array copy of the enums, this behavior is defined in
`src/jdk.compiler/share/classes/com/sun/tools/javac/comp/Lower.java#visitEnumDef`

This is a defensive programming strategy to prevent enums from being modified. However,
copying is unnecessary if we only have read calls.
So we can cache the values result to avoid useless allocations.

Cached as the array since it does not create iterator on the enhanced for loop,
But the list does, and may spend more time than iterating using the array.

One-time calls are excluded from this patch, since no need.

The JMH benchmark of this patch can be found in SunBox's `CachedEnumValuesForLoop`

This patch is based on the following patch:
"reduce allocs"
By: Simon Gardling <titaniumtown@gmail.com>
As part of: JettPack (https://gitlab.com/Titaniumtown/JettPack)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/paper-patches/features/0016-CraftBukkit-UUID-to-world-map.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 24 Dec 2022 13:09:34 +0100
Subject: [PATCH] CraftBukkit UUID to world map

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Optimize CraftServer.getWorld(UUID)"
By: PureGero <puregero@gmail.com>
As part of: MultiPaper (https://github.com/MultiPaper/MultiPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/paper-patches/features/0017-Specific-interval-TPS-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 12:56:43 +0100
Subject: [PATCH] Specific interval TPS API

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0018-5-second-TPS-average.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 12:42:32 +0100
Subject: [PATCH] 5 second TPS average

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add 5 second tps average in /tps"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/paper-patches/features/0019-Including-5s-in-getTPS.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 24 Feb 2024 01:16:07 -0500
Subject: [PATCH] Including 5s in getTPS()
```

## leaf-server/paper-patches/features/0020-Last-tick-time-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 17:12:08 +0100
Subject: [PATCH] Last tick time API

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add getLastTickMs api"
By: tr7zw <tr7zw@live.de>
As part of: YAPFA (https://github.com/tr7zw/YAPFA)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* YAPFA copyright *

Copyright 2020 tr7zw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-server/paper-patches/features/0021-Show-last-tick-time-in-tps-command.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 21:02:58 +0100
Subject: [PATCH] Show last tick time in /tps command

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add getLastTickMs api"
By: tr7zw <tr7zw@live.de>
As part of: YAPFA (https://github.com/tr7zw/YAPFA)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* YAPFA copyright *

Copyright 2020 tr7zw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-server/paper-patches/features/0022-Pre-compute-VarLong-sizes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 21 Aug 2023 21:46:10 +0200
Subject: [PATCH] Pre-compute VarLong sizes

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following commit:
"Reapply "Optimize varint writing""
By: Andrew Steinborn <git@steinborn.me>
As part of: Velocity (https://github.com/PaperMC/Velocity)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Velocity description *

Inspired by the approach described at the bottom of https://richardstartin.github.io/posts/dont-use-protobuf-for-telemetry

Given that we do a lot of varint writing as well, this should provide a small performance boost for larger/complex packets whilst not regressing hard on smaller packets.

This includes a test to ensure that the behavior is as expected and fixes the initialization loop so that the correct results will be given. Much thanks to @octylFractal for acting as my duck while trying to figure this out.
```

## leaf-server/paper-patches/features/0023-Optimize-VarInt-write-and-VarLong-write.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 22 Aug 2023 21:38:37 +0200
Subject: [PATCH] Optimize VarInt#write and VarLong#write

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following commit:
"Reapply "Optimize varint writing""
By: Andrew Steinborn <git@steinborn.me>
As part of: Velocity (https://github.com/PaperMC/Velocity)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Velocity description *

Inspired by the approach described at the bottom of https://richardstartin.github.io/posts/dont-use-protobuf-for-telemetry

Given that we do a lot of varint writing as well, this should provide a small performance boost for larger/complex packets whilst not regressing hard on smaller packets.

This includes a test to ensure that the behavior is as expected and fixes the initialization loop so that the correct results will be given. Much thanks to @octylFractal for acting as my duck while trying to figure this out.
```

## leaf-server/paper-patches/features/0024-Virtual-thread-support.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 9 Aug 2023 15:04:56 +0200
Subject: [PATCH] Virtual thread support

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/paper-patches/features/0025-Virtual-thread-support-for-chat-executor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 27 Apr 2026 13:02:58 -0400
Subject: [PATCH] Virtual thread support for chat executor
```

## leaf-server/paper-patches/features/0026-Virtual-thread-support-for-bukkit-async-scheduler.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalfinals <yuu8583@proton.me>
Date: Tue, 12 Mar 2024 00:36:29 +0800
Subject: [PATCH] Virtual thread support for bukkit async scheduler
```

## leaf-server/paper-patches/features/0027-Virtual-thread-support-for-folia-async-scheduler.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 27 Apr 2026 13:54:29 -0400
Subject: [PATCH] Virtual thread support for folia async scheduler
```

## leaf-server/paper-patches/features/0028-Virtual-thread-support-for-paper-configuration-pool.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 27 Apr 2026 14:03:48 -0400
Subject: [PATCH] Virtual thread support for paper configuration pool
```

## leaf-server/paper-patches/features/0029-Remove-Timings.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 23 Sep 2023 03:12:14 -0400
Subject: [PATCH] Remove Timings

Completely remove the Timings, since it wastes too much performance. Use Spark instead.
```

## leaf-server/paper-patches/features/0030-KeYi-Player-Skull-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalgic853 <yuu8583@proton.me>
Date: Sun, 20 Nov 2022 00:20:00 +0800
Subject: [PATCH] KeYi: Player Skull API

Original license: MIT
Original project: https://github.com/KeYiMC/KeYi
```

## leaf-server/paper-patches/features/0031-Slice-Smooth-Teleports.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Cryptite <cryptite@gmail.com>
Date: Sat, 13 Aug 2022 08:58:14 -0500
Subject: [PATCH] Slice: Smooth Teleports

Original license: MIT
Original project: https://github.com/Cryptite/Slice

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/paper-patches/features/0032-Leaves-Protocol-Core.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Sun, 2 Feb 2025 13:08:33 +0800
Subject: [PATCH] Leaves: Protocol Core

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

Commit: 180ff387ccfdc3453b71652a1d560b5441f68819
```

## leaf-server/paper-patches/features/0033-Leaves-Replay-Mod-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Tue, 4 Feb 2025 19:45:21 +0800
Subject: [PATCH] Leaves: Replay Mod API

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by ReplayMod (https://github.com/ReplayMod)
```

## leaf-server/paper-patches/features/0034-Skip-event-if-no-listeners.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: lilingfengdev <145678359+lilingfengdev@users.noreply.github.com>
Date: Wed, 17 Jan 2024 18:16:38 +0800
Subject: [PATCH] Skip event if no listeners
```

## leaf-server/paper-patches/features/0035-SparklyPaper-Optimize-canSee-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Thu, 23 Nov 2023 14:36:47 -0300
Subject: [PATCH] SparklyPaper: Optimize "canSee" checks

Original project: https://github.com/SparklyPower/SparklyPaper

The "canSee" checks is in a hot path, invoked by each entity for each player on the server if they are in tracking range, so optimizing it is pretty nice

First, we change the original "HashMap" to fastutil's "Object2ObjectOpenHashMap", because the containsKey throughput is better

Then, we add a "isEmpty()" check before attempting to check if the map contains something

This seems stupid, but it does seem that it improves the performance a bit, and it makes sense, "containsKey(...)" does not attempt to check the map size before attempting to check if the map contains the key

We also create a "canSee" method tailored for "ChunkMap#updatePlayer()", a method without the equals check (the "updatePlayer()" already checks if the entity is the same entity) because the CraftPlayer's `equals()` check is a *bit* expensive compared to only checking the object's identity, and because the identity has already been check, we don't need to check it twice.
```

## leaf-server/paper-patches/features/0036-Don-t-throw-exception-on-missing-ResourceKey-value.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 26 Feb 2024 18:51:02 -0500
Subject: [PATCH] Don't throw exception on missing ResourceKey value
```

## leaf-server/paper-patches/features/0037-Mirai-Configurable-chat-message-signatures.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: etil2jz <81570777+etil2jz@users.noreply.github.com>
Date: Tue, 2 Aug 2022 14:48:12 +0200
Subject: [PATCH] Mirai: Configurable chat message signatures

Fixed & Updated by Dreeam-qwq
Original license: GPL-3.0-only
Original project: https://github.com/Dreeam-qwq/Mirai

Original license: GPL-3.0-only
Original project: https://github.com/etil2jz/Mirai
```

## leaf-server/paper-patches/features/0038-Matter-Secure-Seed.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Apehum <apehumchik@gmail.com>
Date: Thu, 9 Dec 2021 02:18:17 +0800
Subject: [PATCH] Matter: Secure Seed

TODO - Dreeam:
Able to write feature seed in existed level.dat (Done)
Update to BLAKE3

Original license: GPL-3.0-only
Original project: https://github.com/plasmoapp/matter

Co-authored-by: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/paper-patches/features/0039-Faster-random-generator.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Faster random generator

This patch replaces LegacyRandomSource with FasterRandomSource by default,
which is faster in general.

Benchmark results (10,000,000 iterations) (Azul Zulu 23.0.1)

FasterRandomSource (Leaf) (Backed by Xoroshiro128PlusPlus): 51,633,700 ns
LegacyRandomSource (Vanilla): 254,857,500 ns
ThreadUnsafeRandom (Moonrise): 102,265,100 ns
SimpleThreadUnsafeRandom (Moonrise): 97,054,600 ns
```

## leaf-server/paper-patches/features/0040-Configurable-unknown-command-message.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 7 Aug 2024 18:54:01 +0800
Subject: [PATCH] Configurable unknown command message
```

## leaf-server/paper-patches/features/0041-Replace-world-map-with-optimized-collection.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 10 Nov 2024 14:36:18 +0100
Subject: [PATCH] Replace world map with optimized collection
```

## leaf-server/paper-patches/features/0042-Cache-CraftEntityType-minecraftToBukkit-convert.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 28 Jul 2024 17:44:10 +0800
Subject: [PATCH] Cache CraftEntityType#minecraftToBukkit convert

The minecraftToBukkit EntityType convert call is expensive in mob spawn. This convert call is ued for spawn event call,
and the results are always same, thus there is no need to do the convert process every time, just cache it.
Save ~0.16ms per tick, and improve 11660ms -> 60ms in around 1 hour.
```

## leaf-server/paper-patches/features/0043-Multithreaded-Tracker.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: peaches94 <peachescu94@gmail.com>
Date: Sat, 2 Jul 2022 00:35:56 -0500
Subject: [PATCH] Multithreaded Tracker

Original license: GPL-3.0-only
Original project: https://github.com/Bloom-host/Petal

Original license: GPL-3.0-only
Original project: https://github.com/TECHNOVE/Airplane-Experimental

Co-authored-by: Paul Sauve <paul@technove.co>
Co-authored-by: Kevin Raneri <kevin.raneri@gmail.com>
Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Co-authored-by: hayanesuru <hayanesuru@outlook.jp>

This patch refactored from original multithreaded tracker (Petal version),
and is derived from the Airplane fork by Paul Sauve, the tree is like:
Airplane -> Pufferfish(?) -> Petal -> Leaf

The core logic has beed reworked compared to the old one, can handle larger
scale situation better now.
Current impl includes many improvements and fixes we made, such as
plugin compat issues with some NPC plugins using real entity type,
e.g. Citizens.

However we still recommend to use those packet based NPC plugins,
e.g. ZNPC Plus, Adyeshach, Fancy NPC, etc.
```

## leaf-server/paper-patches/features/0044-EMC-Don-t-use-snapshots-for-acquiring-blockstate.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Aikar <aikar@aikar.co>
Date: Thu, 28 Jun 2018 22:13:44 -0400
Subject: [PATCH] EMC: Don't use snapshots for acquiring blockstate

Original license: MIT
Original project: https://github.com/starlis/empirecraft
```

## leaf-server/paper-patches/features/0045-Faster-CraftServer-getworlds-list-creation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Faster CraftServer#getworlds list creation

CraftServer#getWorlds/Bukkit#getWorlds is frequently used in plugins,
replacing ArrayList with Fastutil ObjectArrayList
brings about 40% performance improvement in benchmark.
```

## leaf-server/paper-patches/features/0046-Cache-chunk-key.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 2 Nov 2024 04:15:20 -0400
Subject: [PATCH] Cache chunk key

Cache convert process in ChunkPos to the chunkKey, to avoid unnecessary casting and shift operations.
This patch didn't cahce SectionPos or BlockPos to chunkKey, since they are mutable after creation.

The JMH benchmark of this patch can be found in SunBox's `CacheChunkKey`
```

## leaf-server/paper-patches/features/0047-PlayerInventoryOverflowEvent.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 19 Feb 2025 00:36:20 -0500
Subject: [PATCH] PlayerInventoryOverflowEvent
```

## leaf-server/paper-patches/features/0048-Paper-PR-Fix-MC-117075-Block-Entities-Unload-Lag-Spi.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Sun, 26 Nov 2023 13:02:16 -0300
Subject: [PATCH] Paper PR: Fix MC-117075: Block Entities Unload Lag Spike

Original license: GPL-3.0-only
Original project: https://github.com/SparklyPower/SparklyPaper
Paper pull request: https://github.com/PaperMC/Paper/pull/9970

We replaced the `blockEntityTickers` list with a custom list based on fastutil's `ObjectArrayList` with a small yet huge change for us: A method that allows us to remove a list of indexes from the list.

This is WAY FASTER than using `removeAll` with a list of entries to be removed, because we don't need to calculate the identity of each block entity to be removed, and we can jump directly to where the search should begin, giving a performance boost for small removals (because we don't need to loop thru the entire list to find what element should be removed) and a performance boost for big removals (no need to calculate the identity of each block entity).
```

## leaf-server/paper-patches/features/0049-SparklyPaper-Parallel-world-ticking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Sun, 25 May 2025 21:39:32 -0300
Subject: [PATCH] SparklyPaper: Parallel world ticking

Original project: https://github.com/SparklyPower/SparklyPaper

Co-authored-by: Altiami <yoshimo.kristin@gmail.com>
Co-authored-by: Taiyou06 <kaandindar21@gmail.com>
Co-authored-by: MrlingXD <90316914+wling-art@users.noreply.github.com>

Commit: e2e154fb764650c90bbd23c9cede2b11ba1a08e2
```

## leaf-server/paper-patches/features/0050-Fish-Parallel-World-Ticking-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Biquaternions <biquaternions@serlith.net>
Date: Tue, 2 Sep 2025 13:54:37 -0500
Subject: [PATCH] Fish: Parallel World Ticking API

Original license: MIT
Original project: https://github.com/Biquaternions/Fish

This patch provides an API for performance monitoring plugins like PurpurBars.
A better approach would be to include an event when the world is done ticking,
  which will allow to re-use the RollingAverage logic from Spark and Minecraft internals.
However, since every fork developer will want to have said event into their own namespace,
  it will be virtually impossible to provide a universal API.
With this approach only new methods are added into already existing Bukkit API, and the same
  naming conventions as Bukkit were used, which means there's a bigger chance of this API to
  get standarized.
```

## leaf-server/paper-patches/features/0051-Paper-PR-Throttle-failed-spawn-attempts.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: froobynooby <froobynooby@froobworld.com>
Date: Wed, 17 Jul 2024 18:46:11 +0930
Subject: [PATCH] Paper PR: Throttle failed spawn attempts

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/11099

For example config in paper-world-defaults.yml
```
    spawning-throttle:
      failed-attempts-threshold: 1200
      throttled-ticks-per-spawn:
        ambient: 10 # default value in bukkit.yml tickers-per * 10
        axolotls: 10
        creature: 4000
        monster: 10
        underground_water_creature: 10
        water_ambient: 10
        water_creature: 10
```

This patch adds the option to use longer ticks-per-spawn for a given
mob type in chunks where spawn attempts are consecutively failing.

This behaviour is particularly useful on servers where players build
mob farms. Mob farm designs often require making surrounding chunks
spawnproof, which causes the server to waste CPU cycles trying to spawn mobs in
vain. Throttling spawn attempts in suspected spawnproof chunks improves
performance without noticeably advantaging or disadvantaging the mob farm.
```

## leaf-server/paper-patches/features/0052-Async-playerdata-saving.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <mc@jvavav.com>
Date: Fri, 28 Mar 2025 21:19:19 +0800
Subject: [PATCH] Async playerdata saving
```

## leaf-server/paper-patches/features/0053-async-chunk-sender.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 31 Oct 2025 00:04:42 +0900
Subject: [PATCH] async chunk sender
```

## leaf-server/paper-patches/features/0054-Optimise-player-movement-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 11 May 2025 00:37:44 +0200
Subject: [PATCH] Optimise player movement checks
```

## leaf-server/paper-patches/features/0055-optimise-ReferenceList.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 1 Jun 2025 18:13:24 +0200
Subject: [PATCH] optimise ReferenceList
```

## leaf-server/paper-patches/features/0056-cache-getBiome.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 3 Jun 2025 18:51:44 +0900
Subject: [PATCH] cache getBiome
```

## leaf-server/paper-patches/features/0057-Dump-pwt-thread.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 7 Jun 2025 20:33:16 +0900
Subject: [PATCH] Dump pwt thread
```

## leaf-server/paper-patches/features/0058-Paw-optimization.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 24 Apr 2025 16:36:16 -0400
Subject: [PATCH] Paw optimization

Some random optimizations
```

## leaf-server/paper-patches/features/0059-optimize-despawn.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 26 Jun 2025 00:42:27 +0900
Subject: [PATCH] optimize despawn
```

## leaf-server/paper-patches/features/0060-optimize-mob-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 5 Jul 2025 17:01:43 +0900
Subject: [PATCH] optimize mob spawning
```

## leaf-server/paper-patches/features/0061-Remove-stream-in-CraftWorld-spawnParticle.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 29 Oct 2025 02:13:18 -0400
Subject: [PATCH] Remove stream in CraftWorld#spawnParticle
```

## leaf-server/paper-patches/features/0062-Configurable-vanilla-username-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 12 Oct 2022 14:36:58 -0400
Subject: [PATCH] Configurable vanilla username check
```

## leaf-server/paper-patches/features/0063-Fix-paper-tests.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 15 Jan 2026 18:07:51 -0500
Subject: [PATCH] Fix paper tests

snakeyaml deprecated the external util class `Base64Coder` since snakeyaml 2.3
See: https://github.com/snakeyaml/snakeyaml/commit/3f0bedad5917490a1f7ef56a0920b5b93b214ade

It replaced it using Java's built-in Base64 util (java.util.Base64), which is better and more standard.

In here, I use Base64.getMimeDecoder() for the base64 decoding, to be compatible with old data that is using the old
standard (the Base64Coder is using MIME type).
See: https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html#:~:text=MIME

And use the basic Base64 encoder to encode the data.
```

## leaf-server/paper-patches/features/0064-Better-spark-integration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Pascalpex <pascalpex@gmail.com>
Date: Thu, 12 Feb 2026 14:09:13 +0100
Subject: [PATCH] Better spark integration
```

## leaf-server/paper-patches/features/0065-Leaf-Test-Async-Executor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Thu, 4 Mar 2077 00:00:00 +0800
Subject: [PATCH] Leaf Test: Async Executor
```

## leaf-server/paper-patches/features/0066-Use-optimized-collections-in-CB-classes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Wed, 25 Mar 2026 08:48:21 +0300
Subject: [PATCH] Use optimized collections in CB classes
```

## leaf-server/paper-patches/features/0067-Fix-MC-301114-Combat-Tracker-memory-leak.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-301114 Combat Tracker memory leak

Rewritten using EvictingRingList instead of ArrayDeque.
Superseded Paper PR: https://github.com/PaperMC/Paper/pull/13455
```

## leaf-server/paper-patches/features/0068-Paper-Lazy-set-Entity.projectileSource-in-AbstractPr.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Owen1212055 <23108066+Owen1212055@users.noreply.github.com>
Date: Sat, 4 Apr 2026 20:03:09 -0400
Subject: [PATCH] Paper: Lazy set Entity.projectileSource in
 AbstractProjectile#getShooter

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/5e9a9188c04668e8f6b9ebda1060f295ca9b4baa

Backported from Paper 26.1.1

Fixes Paper#13475
Supersedes Paper#13643
Supersedes Paper#13481
```

## leaf-server/paper-patches/features/0069-Paper-Add-options-to-disable-chunk-and-block-entity-.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Nassim Jahnke <nassim@njahnke.dev>
Date: Sun, 26 Apr 2026 17:09:05 +0200
Subject: [PATCH] Paper: Add options to disable chunk and block entity ticking

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/acfe105b9dd64f4b3179ce2575e3809568974a20

Backported from Paper 26.1.2
```

## leaf-server/paper-patches/features/0070-Create-tag-instance-for-pdc-view-only-when-necessary.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 24 Apr 2026 11:17:23 -0400
Subject: [PATCH] Create tag instance for pdc view only when necessary
```

## leaf-server/paper-patches/features/0071-Optimize-block-entities-count.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 27 May 2026 00:38:04 +0800
Subject: [PATCH] Optimize block entities count

Just use `getTickableTileEntityCount` for `getTileEntityCount` to be simpler, to reduce expensive overhead from Spark's
statistics calls.
```

## leaf-server/minecraft-patches/features/0001-Purpur-Server-Minecraft-Changes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Github Actions <no-reply@github.com>
Date: Wed, 7 Jan 2026 23:55:32 +0000
Subject: [PATCH] Purpur Server Minecraft Changes

Original license: MIT
Original project: https://github.com/PurpurMC/Purpur

Commit: f57bd8659b5c3cbaf03984ee5a8ecdcc0849040a

Patches listed below are removed in this patch, They exists in Gale or Leaf:
* "net/minecraft/CrashReport.java.patch"
    - Rebrand
* "net/minecraft/commands/Commands.java.patch"
    - Skip events if there's no listeners
* "net/minecraft/network/chat/SignedMessageChain.java.patch"
    - Option to disable kick for out of order chat
* "net/minecraft/server/MinecraftServer.java.patch"
    - Add 5 second tps average in /tps
    - Configurable server mod name
* "net/minecraft/server/PlayerAdvancements.java.patch"
    - Logger settings (suppressing pointless logs)
* "net/minecraft/server/gui/StatsComponent.java.patch"
    - Add 5 second tps average in /tps
* "net/minecraft/server/level/WorldGenRegion.java.patch"
    - Logger settings (suppressing pointless logs)
* "net/minecraft/server/network/ServerCommonPacketListenerImpl.java.patch"
    - Alternative Keepalive Handling
* "net/minecraft/server/network/ServerStatusPacketListenerImpl.java.patch"
    - Fix 'outdated server' showing in ping before server fully boots
* "net/minecraft/stats/ServerRecipeBook.java.patch"
    - Logger settings (suppressing pointless logs)
* "net/minecraft/world/entity/ai/goal/RangedBowAttackGoal.java.patch"
    - MC-121706 - Fix mobs not looking up and down when strafing
* "net/minecraft/world/entity/animal/cow/MushroomCow.java.patch"
    - Fix cow rotation when shearing mooshroom
* "net/minecraft/world/entity/animal/fish/WaterAnimal.java.patch"
    - MC-238526 - Fix spawner not spawning water animals correctly
* "net/minecraft/world/level/chunk/storage/RegionFileStorage.java.patch"
    - Rebrand
* "org/purpurmc/purpur/PurpurVersionFetcher.java"
```

## leaf-server/minecraft-patches/features/0002-Rebrand.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 16 Sep 2021 20:39:45 -0400
Subject: [PATCH] Rebrand
```

## leaf-server/minecraft-patches/features/0003-Gale-configuration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 21:05:47 +0100
Subject: [PATCH] Gale configuration

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Paper config files"
By: Jake Potrebic <jake.m.potrebic@gmail.com>
As part of: Paper (https://github.com/PaperMC/Paper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0004-Leaf-config.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 12 Oct 2022 10:42:15 -0400
Subject: [PATCH] Leaf config

Leaf Config v3
including load config, backup old or outdated config, and add config to spark profiler automatically.

TODO - Dreeam:
Add per world config
Update config reload:
    - Use different state to separate different configs reload
    (Or sync reload)
```

## leaf-server/minecraft-patches/features/0005-Leaf-Commands.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 4 Nov 2024 23:07:27 -0500
Subject: [PATCH] Leaf Commands

Currently the config reload is just a simple poc,
if necessary, I will add @DoNotLoad feature in the future, like Luminol

TODOs:
Leaf status command
Leaf config command
Leaf version command enhanced (ability to show the list of new commits based on current version) (optional)

Leaf config
Leaf config only get config value (TODO: check whether work, and whether need to set config value back to keep the key in the config file)
Leaf config convert from toml to yaml
Leaf config v3 move to new key
...
```

## leaf-server/minecraft-patches/features/0006-Configurable-vanilla-profiler.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 8 Jan 2026 04:31:53 -0500
Subject: [PATCH] Configurable vanilla profiler

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

Co-authored-by: Martijn Muijsers <martijnmuijsers@live.nl>

This patch is based on the following patch:
"Remove Mojang Profiler"
By: BillyGalbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0007-Use-platform-math-functions.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 8 Aug 2023 20:43:20 +0200
Subject: [PATCH] Use platform math functions

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Use Math.floor instead of fastfloor"
By: Xymb <xymb@endcrystal.me>
As part of: Kaiiju (https://github.com/KaiijuMC/Kaiiju)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Comparison of floor methods used in Paper *

Measure shown is floored number of milliseconds
(nanoseconds integer-divided by 1_000_000
taken to get the floor of 1000 randomly chosen doubles
(all in the range of [-Integer.MAX_VALUE + 10, Integer.MAX_VALUE - 10])
100_000 times (making it 100_000_000 floor operations total)
and adding it to a total.

We are testing the following methods:
* net.minecraft.util.Mth.floor
* java.lang.Math.floor
* java.lang.StrictMath.floor
* org.apache.commons.math3.util.FastMath.floor
* org.apache.commons.math3.util.FastMath.floor, but with a hot start (see comment in code)
* io.papermc.paper.util.MCUtil.fastFloor

The tests performed clearly show that Math.floor is the fastest.
This is most likely due to java.lang.Math's usage of the @IntrinsicCandidate
annotation, which allows the JVM to use a more optimized implementation at runtime.
However, in the case that there is no intrinsic replacement for Math.floor,
it defers to StrictMath.floor, which relies on a number of native methods, and is
still much faster than the existing Minecraft utility functions.
Therefore, using Math.floor instead of these functions is better regardless.
In Apache Commons Math 4, FastMath.floor has also been removed in favor of Math.floor.

The versions used:
* Windows 10 Home 21H2 19044.3086
* OpenJDK Runtime Environment Temurin-19.0.2+7 (build 19.0.2+7)
* Paper a3c760e6af1e8c7244ef75c6da6e6df278a79e14 on Minecraft 1.20.1
* Apache Commons Math 3.6.1

Results:
Total is of type int                          Total is of type double
----------------------------------------------------------------------------------
Mth.floor                             2113    (double) Mth.floor              2658
(int) Math.floor                      130     Math.floor                      194
(int) StrictMath.floor                835     StrictMath.floor                381
(int) FastMath.floor                  412     FastMath.floor                  376
(int) FastMath.floor with hot start   359     FastMath.floor with hot start   321
MCUtil.fastFloor                      2284    (double) MCUtil.fastFloor       2469

Code is below:
```java
package somepackage;

import io.papermc.paper.util.MCUtil;
import net.minecraft.util.Mth;

import java.util.Random;

public class Main {

    public static void main(String[] args) {

				// IF FastMath.floor with a hot start:
				// FastMath.floor(37485.5);

        var random = new Random(4889338);
        int size = 1000;
        var values = new double[size];
        double bound = Integer.MAX_VALUE - 10;
        for (int i = 0; i < size; i++) {
            values[i] = random.nextDouble(bound * 2) - bound;
        }
        int repeats = 100_000;

        // int total = 0;
				// OR
				// double total = 0;

        long start = System.nanoTime();
        for (int repeat = 0; repeat < repeats; repeat++) {
            for (int index = 0; index < size; index++) {
                total += insert_function_being_tested_here(values[index]);
            }
        }
        long diff = System.nanoTime() - start;
        System.out.println(total);
        System.out.println(diff / 1_000_000L);

    }

}
```
```

## leaf-server/minecraft-patches/features/0008-Faster-floating-point-positive-modulo.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Aug 2023 22:29:08 +0200
Subject: [PATCH] Faster floating-point positive modulo

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

The JMH benchmark of this patch can be found in SunBox's `FasterFloatingPointPositiveModulo`
```

## leaf-server/minecraft-patches/features/0009-Simpler-ShapelessRecipe-comparison-for-vanilla.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 16:22:47 +0100
Subject: [PATCH] Simpler ShapelessRecipe comparison for vanilla

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Simpler ShapelessRecipes comparison for Vanilla"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane description *

Paper added a fancy sorting comparison due to Bukkit recipes breaking
the vanilla one, however this is far more advanced than what you need
for all the vanilla recipes.

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0010-Reduce-projectile-chunk-loading.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 12:00:55 +0100
Subject: [PATCH] Reduce projectile chunk loading

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Reduce projectile chunk loading"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0011-Predict-Halloween.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 16:29:01 +0100
Subject: [PATCH] Predict Halloween

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

The Halloween date is 10/31
Cache the date result to prevent calculate in every call

The JMH benchmark of this patch can be found in SunBox's `PredictHalloween`
```

## leaf-server/minecraft-patches/features/0012-Move-random-tick-random.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 16:53:40 +0100
Subject: [PATCH] Move random tick random

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Move ThreadUnsafeRandom Initialization"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

ThreadUnsafeRandom is initialized too late and some of our patches
require it to be initialized earlier. By moving it to the superclass, we
initialize it earlier, ensuring that it is available sooner.
```

## leaf-server/minecraft-patches/features/0013-Optimize-random-calls-in-chunk-ticking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 16:45:45 +0100
Subject: [PATCH] Optimize random calls in chunk ticking

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Optimize random calls in chunk ticking"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

The patch also received the following subsequent modification:
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Description *

Throttling of ice and snow tick has been moved to another patch as
configurable ice and snow tick chance.

* Airplane description *

Especially at over 30,000 chunks these random calls are fairly heavy. We
use a different method here for checking lightning, and for checking
ice.

Lightning: Each chunk now keeps an int of how many ticks until the
lightning should strike. This int is a random number from 0 to 100000 * 2,
the multiplication is required to keep the probability the same.

Ice and snow: We just generate a single random number 0-16 and increment
it, while checking if it's 0 for the current chunk.

Depending on configuration for things that tick in a chunk, this is a
5-10% improvement.

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0014-Reduce-enderman-teleport-chunk-lookups.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 19:53:35 +0100
Subject: [PATCH] Reduce enderman teleport chunk lookups

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Reduce chunk loading & lookups"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0015-Check-targeting-range-before-getting-visibility.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 20:21:06 +0100
Subject: [PATCH] Check targeting range before getting visibility

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Early return optimization for target finding"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0016-Cache-on-climbable-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 20:40:40 +0100
Subject: [PATCH] Cache on climbable check

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Cache climbing check for activation"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0017-Make-EntityCollisionContext-a-live-representation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 22:03:33 +0100
Subject: [PATCH] Make EntityCollisionContext a live representation

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Make EntityCollisionContext a live representation"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane description *

While Context is in the name, it is not used as a context. Instead it is
always created, use temporarily, then thrown away. This means having a
lot of fields to initialize and make space for is useless. I cannot find
anywhere in the codebase where this is used as a context which may be
saved for later, so this should be safe assuming plugins don't use it
for some strange reason.

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0018-Better-checking-for-useless-move-packets.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 22:26:37 +0100
Subject: [PATCH] Better checking for useless move packets

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Better checking for useless move packets"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0019-Remove-lambda-from-ticking-guard.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 23 Nov 2022 23:13:56 +0100
Subject: [PATCH] Remove lambda from ticking guard

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

Remove lambda for clear stacktrace and better for JIT

This patch is based on the following patch:
"Remove lambda from ticking guard"
By: Paul Sauve <paul@technove.co>
As part of: Airplane (https://github.com/TECHNOVE/Airplane)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Airplane copyright *

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0020-SIMD-support.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 01:19:12 +0100
Subject: [PATCH] SIMD support

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add SIMD utilities"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0021-Make-book-writing-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 02:11:29 +0100
Subject: [PATCH] Make book writing configurable

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add option to disable books"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

Books are commonly the target of duping-related exploits. If you
anticipate that your server will be an early target of duping attacks in
the event that new exploits are found, you may want to consider removing
the ability for non-privileged players to edit books. This patch allows
you to easily disable books, should you want to preemptively remove this
functionality before additional exploits are found.
```

## leaf-server/minecraft-patches/features/0022-Reduce-in-wall-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 10:31:38 +0100
Subject: [PATCH] Reduce in wall checks

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Optimize suffocation"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

The isInWall check to determine suffocation is quite expensive, and
often is completely unnecessary to check. We do two things here to
improve this:

1. We only check for suffocation once per 20 ticks. The maximum
no-damage ticks value means that this change should be extremely
difficult, if not impossible, for players to notice.

2. We additionally execute a check to see if the player can even take
damage in the first place. This check doesn't improve performance much
but is so much cheaper than the suffocation check that it's worth
keeping it.
```

## leaf-server/minecraft-patches/features/0023-Make-chat-order-verification-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 05:22:43 +0100
Subject: [PATCH] Make chat order verification configurable

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add option to disable out-of-order chat"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0024-Make-ender-dragon-respawn-attempt-after-placing-end-.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 7 Aug 2023 21:35:57 +0200
Subject: [PATCH] Make ender dragon respawn attempt after placing end crystals
 configurable

The original Pufferfish description has been included below for reference,
but please note that since Paper 1.20.1, there is no need to disable end crystal
spawning for performance: Paper already contains performance
enhancements that make the end crystal checks very fast.

See Paper#9611 for more information. This patch now only
functions as a gameplay mechanism tweak.

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Option to disable end crystal dragon respawning"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

On servers that expect end crystal fights in the end dimension,
disabling this will prevent the server from performing an expensive
search to attempt respawning the ender dragon whenever a player places
an end crystal.
```

## leaf-server/minecraft-patches/features/0025-Make-saving-fireworks-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 07:30:30 +0100
Subject: [PATCH] Make saving fireworks configurable

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Don't save Fireworks"
By: Aikar <aikar@aikar.co>
As part of: EmpireCraft (https://github.com/starlis/empirecraft)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* EmpireCraft description *

Fireworks can bug out and not detonate, and an automated
launcher can very easily fill a chunk.

Prevent saving Fireworks so that chunk unloads will wipe a chunks fireworks in this case.
```

## leaf-server/minecraft-patches/features/0026-Reduce-villager-item-re-pickup.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 23:15:49 +0100
Subject: [PATCH] Reduce villager item re-pickup

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Slow down villager pickup of thrown items"
By: Aikar <aikar@aikar.co>
As part of: EmpireCraft (https://github.com/starlis/empirecraft)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* EmpireCraft description *

Helps 1.8 Farms let hoppers pick it up before Villager
due to our hopper changes
```

## leaf-server/minecraft-patches/features/0027-Variable-entity-wake-up-duration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 22 Mar 2023 00:18:15 +0100
Subject: [PATCH] Variable entity wake-up duration

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

TODO: Replace random of wakeUpDurationRandom if needed.
```

## leaf-server/minecraft-patches/features/0028-Do-not-process-chat-commands-before-player-has-joine.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 15:33:32 +0100
Subject: [PATCH] Do not process chat/commands before player has joined

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Do not process chat/commands before player has joined"
By: chickeneer <emcchickeneer@gmail.com>
As part of: EmpireCraft (https://github.com/starlis/empirecraft)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0029-Do-not-log-invalid-statistics.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 23:39:32 +0100
Subject: [PATCH] Do not log invalid statistics

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Ignore statistics warnings"
By: Aikar <aikar@aikar.co>
As part of: EmpireCraft (https://github.com/starlis/empirecraft)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0030-Do-not-log-empty-message-warnings.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 24 Nov 2022 23:45:57 +0100
Subject: [PATCH] Do not log empty message warnings

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0031-Do-not-log-ignored-advancements.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 13:18:54 +0100
Subject: [PATCH] Do not log ignored advancements

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Logger settings (suppressing pointless logs)"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0032-Do-not-log-setBlock-in-far-chunks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 13:28:38 +0100
Subject: [PATCH] Do not log setBlock in far chunks

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Logger settings (suppressing pointless logs)"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0033-Do-not-log-unrecognized-recipes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 15:01:30 +0100
Subject: [PATCH] Do not log unrecognized recipes

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Logger settings (suppressing pointless logs)"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0034-Do-not-log-expired-message-warnings.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 15:07:55 +0100
Subject: [PATCH] Do not log expired message warnings

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0035-Do-not-log-Not-Secure-marker.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 25 Nov 2022 15:13:38 +0100
Subject: [PATCH] Do not log Not Secure marker

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0036-Do-not-log-disconnections-with-null-id.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 29 Jul 2023 11:20:35 +0200
Subject: [PATCH] Do not log disconnections with null id

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add option to suppress null ID disconnections"
By: Kevin Raneri <kevin.raneri@gmail.com>
As part of: Pufferfish (https://github.com/pufferfish-gg/Pufferfish)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Pufferfish description *

This can help to hide annoying scanning bots from showing up in console.
```

## leaf-server/minecraft-patches/features/0037-Do-not-log-run-as-root-warning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 1 Dec 2022 16:43:50 +0100
Subject: [PATCH] Do not log run as root warning

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Options of warnings"
By: nostalgic853 <yuu8583@proton.me>
As part of: KeYi (https://github.com/MC-Multithreading-Lab/KeYi-MT)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* KeYi description *

Let users decide if we should warn while running in a root user

* KeYi copyright *

MIT License

Copyright (c) 2022 KeYiMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0038-Do-not-log-offline-mode-warning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 1 Dec 2022 16:52:27 +0100
Subject: [PATCH] Do not log offline mode warning

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Options of warnings"
By: nostalgic853 <yuu8583@proton.me>
As part of: KeYi (https://github.com/MC-Multithreading-Lab/KeYi-MT)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* KeYi description *

Let users decide if we should warn while running in offline mode

* KeYi copyright *

MIT License

Copyright (c) 2022 KeYiMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0039-Fix-outdated-server-showing-in-ping-before-server-fu.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 15:50:22 +0100
Subject: [PATCH] Fix 'outdated server' showing in ping before server fully
 boots

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Fix 'outdated server' showing in ping before server fully boots"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0040-Fix-MC-238526.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 16:16:35 +0100
Subject: [PATCH] Fix MC-238526

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"MC-238526 - Fix spawner not spawning water animals correctly"
By: BillyGalbreath <Blake.Galbreath@Gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0041-Fix-MC-121706.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 16:42:48 +0100
Subject: [PATCH] Fix MC-121706

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"MC-121706 - Fix mobs not looking up and down when strafing"
By: Krakenied <Krakenied1@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0042-Make-logging-login-locations-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 00:17:40 +0100
Subject: [PATCH] Make logging login locations configurable

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"configurable logging of player login location"
By: Simon Gardling <titaniumtown@gmail.com>
As part of: JettPack (https://gitlab.com/Titaniumtown/JettPack)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0043-Reduce-array-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 11:25:45 +0100
Subject: [PATCH] Reduce array allocations

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

Enum's values returns anew array copy of the enums, this behavior is defined in
`src/jdk.compiler/share/classes/com/sun/tools/javac/comp/Lower.java#visitEnumDef`

This is a defensive programming strategy to prevent enums from being modified. However,
copying is unnecessary if we only have read calls.
So we can cache the values result to avoid useless allocations.

Cached as the array since it does not create iterator on the enhanced for loop,
But the list does, and may spend more time than iterating using the array.

One-time calls are excluded from this patch, since no need.

The JMH benchmark of this patch can be found in SunBox's `CachedEnumValuesForLoop`

This patch is based on the following patch:
"reduce allocs"
By: Simon Gardling <titaniumtown@gmail.com>
As part of: JettPack (https://gitlab.com/Titaniumtown/JettPack)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0044-Optimize-sun-burn-tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 23:04:59 +0100
Subject: [PATCH] Optimize sun burn tick

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"some entity micro opts"
By: Simon Gardling <titaniumtown@gmail.com>
As part of: JettPack (https://gitlab.com/Titaniumtown/JettPack)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0045-Reduce-lambda-and-Optional-allocation-in-EntityBased.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 16:28:23 +0100
Subject: [PATCH] Reduce lambda and Optional allocation in
 EntityBasedExplosionDamageCalculator

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/alloc/explosion_behavior/EntityBasedExplosionDamageCalculatorMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0046-Replace-throttle-tracker-map-with-optimized-collecti.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 23 Dec 2022 22:22:47 +0100
Subject: [PATCH] Replace throttle tracker map with optimized collection

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Use more fastutil data structures"
By: nopjmp <kthompson@hey.com>
As part of: Dionysus (https://github.com/nopjmp/Dionysus)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Dionysus description *

Use them in more places.

* Dionysus copyright *

Copyright 2021 Kayla Thompson

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-server/minecraft-patches/features/0047-Cache-FluidOcclusionCacheKey-hash.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 15:51:59 +0100
Subject: [PATCH] Cache FluidOcclusionCacheKey hash

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

The JMH benchmark of this patch can be found in SunBox's `RecordHashCode`
```

## leaf-server/minecraft-patches/features/0048-Cache-ShapePairKey-hash.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 12 Dec 2024 02:58:34 -0500
Subject: [PATCH] Cache ShapePairKey hash

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

The JMH benchmark of this patch can be found in SunBox's `RecordHashCode`
```

## leaf-server/minecraft-patches/features/0049-Replace-division-by-multiplication-in-CubePointRange.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 17:24:24 +0100
Subject: [PATCH] Replace division by multiplication in CubePointRange

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/shapes/precompute_shape_arrays/CubePointRangeMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0050-Replace-parts-by-size-in-CubePointRange.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 17:28:37 +0100
Subject: [PATCH] Replace parts by size in CubePointRange

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0051-Check-frozen-ticks-before-landing-block.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 20:17:41 +0100
Subject: [PATCH] Check frozen ticks before landing block

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/entity/fast_powder_snow_check/LivingEntityMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0052-Update-boss-bar-within-tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 1 Dec 2022 00:12:14 +0100
Subject: [PATCH] Update boss bar within tick

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/ai/raid/RaidMixin.java"
By: Angeline <jellysquid3@users.noreply.github.com>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0053-Cache-world-generator-sea-level.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 1 Dec 2022 14:18:46 +0100
Subject: [PATCH] Cache world generator sea level

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

* Lithium description *

Use cached sea level instead of retrieving from the registry every time.
This method is called for every block in the chunk so this will save a lot of registry lookups.
Lazy initialization added to avoid unbound registry value crash

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/gen/cached_generator_settings/NoiseBasedChunkGeneratorMixin.java"
By: SuperCoder7979 <k.pranav@gmail.com>, 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0054-Skip-secondary-POI-sensor-if-absent.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 10:47:29 +0100
Subject: [PATCH] Skip secondary POI sensor if absent

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following mixin:
"net/caffeinemc/mods/lithium/mixin/ai/sensor/secondary_poi/SecondaryPoiSensorMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)

* Lithium description *

villagers that have no secondary POI (farmland) do not search for it
```

## leaf-server/minecraft-patches/features/0055-Skip-entity-move-if-movement-is-zero.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 14:25:58 +0100
Subject: [PATCH] Skip entity move if movement is zero

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following mixin:
"com/ishland/vmp/mixins/entity/move_zero_velocity/MixinEntity.java"
By: ishland <ishlandmc@yeah.net>
As part of: VMP (https://github.com/RelativityMC/VMP-fabric)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0056-Store-mob-counts-in-an-array.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 17:46:23 +0100
Subject: [PATCH] Store mob counts in an array

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following mixin:
"com/ishland/vmp/mixins/general/spawn_density_cap/MixinSpawnDensityCapperDensityCap.java"
By: ishland <ishlandmc@yeah.net>
As part of: VMP (https://github.com/RelativityMC/VMP-fabric)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0057-Use-linked-map-for-entity-trackers.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Thu, 1 Dec 2022 14:40:03 +0100
Subject: [PATCH] Use linked map for entity trackers

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following mixin:
"com/ishland/vmp/mixins/entitytracker/MixinThreadedAnvilChunkStorage.java"
By: ishland <ishlandmc@yeah.net>
As part of: VMP (https://github.com/RelativityMC/VMP-fabric)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0058-Optimize-noise-generation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 19:49:14 +0100
Subject: [PATCH] Optimize noise generation

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following mixins:
* "com/ishland/c2me/opts/math/mixin/MixinOctavePerlinNoiseSampler.java"
* "com/ishland/c2me/opts/math/mixin/MixinPerlinNoiseSampler.java"
By: ishland <ishlandmc@yeah.net>
As part of: C2ME (https://github.com/RelativityMC/C2ME-fabric)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0059-Optimize-sheep-offspring-color.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 23 Dec 2022 22:47:38 +0100
Subject: [PATCH] Optimize sheep offspring color

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following mixins and classes:
* "carpetfixes/helpers/Utils.java"
* "carpetfixes/mixins/optimizations/SheepEntity_childColorMixin.java"
By: fxmorin <28154542+fxmorin@users.noreply.github.com>
As part of: carpet-fixes (https://github.com/fxmorin/carpet-fixes)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* carpet-fixes description *

The game determines the child sheep's color by getting a wool block from the parents, putting them in a crafting
recipe, getting the output wool and getting the color from that.
I don't know in what world we would consider a data-driven method with that much overhead as a smart idea. Instead,
we used a prebaked list of all the possible colors and combinations, however this means that you can't use a
datapack to change it.

* carpet-fixes copyright *

MIT License

Copyright (c) 2020 Fx Morin

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0060-Hide-flames-on-entities-with-fire-resistance.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 14:39:27 +0100
Subject: [PATCH] Hide flames on entities with fire resistance

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Don't send fire packets if player has Fire Resistance"
By: Cryptite <cryptite@gmail.com>
As part of: Slice (https://github.com/Cryptite/Slice)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0061-Skip-cloning-advancement-criteria.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 16:54:28 +0100
Subject: [PATCH] Skip cloning advancement criteria

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Skip cloning loot parameters" [sic]
By: etil2jz <81570777+etil2jz@users.noreply.github.com>
As part of: Mirai (https://github.com/etil2jz/Mirai)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0062-Reduce-block-destruction-packet-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Fri, 23 Dec 2022 20:42:50 +0100
Subject: [PATCH] Reduce block destruction packet allocations

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Cache block break animation packet"
By: VytskaLT <VytskaLT@protonmail.com>
As part of: SportPaper (https://github.com/Electroid/SportPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0063-Spread-out-sending-all-player-info.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 23:18:57 +0100
Subject: [PATCH] Spread out sending all player info

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Spread out and optimise player list ticksSpread out and optimise player list ticks"
By: James Lyne <jim+github@not-null.co.uk>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0064-Skip-PlayerCommandSendEvent-if-there-are-no-listener.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 00:07:46 +0100
Subject: [PATCH] Skip PlayerCommandSendEvent if there are no listeners

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Skip events if there's no listeners"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0065-Send-multiple-keep-alive-packets.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 00:43:42 +0100
Subject: [PATCH] Send multiple keep-alive packets

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Alternative Keepalive Handling"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0066-Make-slow-login-timeout-configurable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 24 Dec 2022 22:50:13 +0100
Subject: [PATCH] Make slow login timeout configurable

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0067-Don-t-load-chunks-to-spawn-phantoms.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 19:31:09 +0100
Subject: [PATCH] Don't load chunks to spawn phantoms

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Don't load chunks when spawning phantoms"
By: PureGero <puregero@gmail.com>
As part of: MultiPaper (https://github.com/MultiPaper/MultiPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0068-Don-t-load-chunks-to-activate-climbing-entities.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 19:39:38 +0100
Subject: [PATCH] Don't load chunks to activate climbing entities

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0069-Broadcast-crit-animations-as-the-entity-being-critte.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 23:33:28 +0100
Subject: [PATCH] Broadcast crit animations as the entity being critted

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Send crit animations as the entity being critted"
By: PureGero <puregero@gmail.com>
As part of: MultiPaper (https://github.com/MultiPaper/MultiPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0070-Ignore-null-legacy-structure-data.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 23:39:05 +0100
Subject: [PATCH] Ignore null legacy structure data

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Add null check in LegacyStructureDataHandler"
By: PureGero <puregero@gmail.com>
As part of: MultiPaper (https://github.com/MultiPaper/MultiPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0071-Prevent-entities-random-strolling-into-non-ticking-c.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 20:38:15 +0100
Subject: [PATCH] Prevent entities random strolling into non-ticking chunks

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Don't wander into non-ticking chunks"
By: PureGero <puregero@gmail.com>
As part of: MultiPaper (https://github.com/MultiPaper/MultiPaper)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0072-Global-EULA-file.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 26 Dec 2022 00:34:43 +0100
Subject: [PATCH] Global EULA file

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Global Eula file"
By: tr7zw <tr7zw@live.de>
As part of: YAPFA (https://github.com/tr7zw/YAPFA)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* YAPFA copyright *

Copyright 2020 tr7zw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-server/minecraft-patches/features/0073-5-second-TPS-average.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 12:42:32 +0100
Subject: [PATCH] 5 second TPS average

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add 5 second tps average in /tps"
By: William Blake Galbreath <blake.galbreath@gmail.com>
As part of: Purpur (https://github.com/PurpurMC/Purpur)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Purpur copyright *

MIT License

Copyright (c) 2019-2022 PurpurMC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0074-Measure-last-tick-time.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sat, 26 Nov 2022 16:54:05 +0100
Subject: [PATCH] Measure last tick time

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Add getLastTickMs api"
By: tr7zw <tr7zw@live.de>
As part of: YAPFA (https://github.com/tr7zw/YAPFA)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* YAPFA copyright *

Copyright 2020 tr7zw

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

## leaf-server/minecraft-patches/features/0075-Increase-time-statistics-in-intervals.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 22:50:57 +0100
Subject: [PATCH] Increase time statistics in intervals

License: MIT (https://opensource.org/licenses/MIT)
Gale - https://galemc.org

This patch is based on the following patch:
"Smarter statistics ticking"
By: Mykyta Komarnytskyy <nkomarn@hotmail.com>
As part of: Hydrinity (https://github.com/duplexsystem/Hydrinity)
Licensed under: MIT (https://opensource.org/licenses/MIT)

* Hydrinity description *

In vanilla, statistics that count time spent for an action (i.e. time played or sneak time) are incremented every tick. This is retarded. With this patch and a configured interval of 20, the statistics are only ticked every 20th tick and are incremented by 20 ticks at a time. This means a lot less ticking with the same accurate counting.

With an interval of 20, this patch saves roughly 3ms per tick on a server w/ 80 players online.

* Hydrinity copyright *

This patch was created for the Hydrinity project <https://github.com/Hydrinity/Hydrinity> by Mykyta Komarnytskyy <nkomarn@hotmail.com> under the MIT license.

MIT License

Copyright (c) 2020 Mykyta Komarnytskyy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## leaf-server/minecraft-patches/features/0076-For-collision-check-has-physics-before-same-vehicle.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 00:30:17 +0100
Subject: [PATCH] For collision check has physics before same vehicle
MIME-Version: 1.0
Content-Type: text/plain; charset=UTF-8
Content-Transfer-Encoding: 8bit

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"Swaps the predicate order of collision"
By: ㄗㄠˋ ㄑㄧˊ <tsao-chi@the-lingo.org>
As part of: Akarin (https://github.com/Akarin-project/Akarin)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0077-Skip-negligible-planar-movement-multiplication.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Nov 2022 23:44:41 +0100
Subject: [PATCH] Skip negligible planar movement multiplication

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0078-Optimize-matching-item-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Sun, 25 Dec 2022 20:51:32 +0100
Subject: [PATCH] Optimize matching item checks

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0079-Pre-compute-VarLong-sizes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Mon, 21 Aug 2023 21:46:10 +0200
Subject: [PATCH] Pre-compute VarLong sizes

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following commit:
"Reapply "Optimize varint writing""
By: Andrew Steinborn <git@steinborn.me>
As part of: Velocity (https://github.com/PaperMC/Velocity)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Velocity description *

Inspired by the approach described at the bottom of https://richardstartin.github.io/posts/dont-use-protobuf-for-telemetry

Given that we do a lot of varint writing as well, this should provide a small performance boost for larger/complex packets whilst not regressing hard on smaller packets.

This includes a test to ensure that the behavior is as expected and fixes the initialization loop so that the correct results will be given. Much thanks to @octylFractal for acting as my duck while trying to figure this out.
```

## leaf-server/minecraft-patches/features/0080-Optimize-VarInt-write-and-VarLong-write.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 22 Aug 2023 21:38:37 +0200
Subject: [PATCH] Optimize VarInt#write and VarLong#write

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following commit:
"Reapply "Optimize varint writing""
By: Andrew Steinborn <git@steinborn.me>
As part of: Velocity (https://github.com/PaperMC/Velocity)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Velocity description *

Inspired by the approach described at the bottom of https://richardstartin.github.io/posts/dont-use-protobuf-for-telemetry

Given that we do a lot of varint writing as well, this should provide a small performance boost for larger/complex packets whilst not regressing hard on smaller packets.

This includes a test to ensure that the behavior is as expected and fixes the initialization loop so that the correct results will be given. Much thanks to @octylFractal for acting as my duck while trying to figure this out.
```

## leaf-server/minecraft-patches/features/0081-Reduce-RandomSource-instances.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Tue, 29 Nov 2022 00:45:45 +0100
Subject: [PATCH] Reduce RandomSource instances

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"don't create new random instance"
By: foss-mc <69294560+foss-mc@users.noreply.github.com>
As part of: Patina (https://github.com/PatinaMC/Patina)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0082-Reduce-skull-ItemStack-lookups-for-reduced-visibilit.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Aug 2023 20:17:20 +0200
Subject: [PATCH] Reduce skull ItemStack lookups for reduced visibility

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"feat: reduce sensor work"
By: peaches94 <peachescu94@gmail.com>
As part of: Petal (https://github.com/Bloom-host/Petal)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Petal description *

this patch is focused around the sensors used for ai
delete the line of sight cache less often and use a faster nearby comparison
```

## leaf-server/minecraft-patches/features/0083-Initialize-line-of-sight-cache-with-low-capacity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Aug 2023 19:01:50 +0200
Subject: [PATCH] Initialize line of sight cache with low capacity

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org
```

## leaf-server/minecraft-patches/features/0084-Reduce-line-of-sight-updates-and-cache-lookups.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Martijn Muijsers <martijnmuijsers@live.nl>
Date: Wed, 30 Aug 2023 20:01:31 +0200
Subject: [PATCH] Reduce line of sight updates and cache lookups

License: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)
Gale - https://galemc.org

This patch is based on the following patch:
"feat: reduce sensor work"
By: peaches94 <peachescu94@gmail.com>
As part of: Petal (https://github.com/Bloom-host/Petal)
Licensed under: GPL-3.0-only (https://www.gnu.org/licenses/gpl-3.0.html)

* Petal description *

this patch is focused around the sensors used for ai
delete the line of sight cache less often and use a faster nearby comparison
```

## leaf-server/minecraft-patches/features/0085-Pufferfish-Optimize-mob-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Kevin Raneri <kevin.raneri@gmail.com>
Date: Wed, 10 Nov 2021 00:37:03 -0500
Subject: [PATCH] Pufferfish: Optimize mob spawning

Original license: GPL-3.0-only
Original project: https://github.com/pufferfish-gg/Pufferfish

Co-authored-by: booky10 <boooky10@gmail.com>

This patch aims to reduce the main-thread impact of mob spawning by
offloading as much work as possible to other threads. It is possible for
inconsistencies to come up, but when they happen they never interfere
with the server's operation (they don't produce errors), and side
effects are limited to more or less mobs being spawned in any particular
tick.

It is possible to disable this optimization if it is not required or if
it interferes with any plugins. On servers with thousands of entities,
this can result in performance gains of up to 15%, which is significant
and, in my opinion, worth the low risk of minor mob-spawning-related
inconsistencies.
```

## leaf-server/minecraft-patches/features/0086-Pufferfish-Dynamic-Activation-of-Brain.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Paul Sauve <paul@technove.co>
Date: Fri, 15 Jan 2021 19:05:01 -0600
Subject: [PATCH] Pufferfish: Dynamic Activation of Brain

Original license: GPL-3.0-only
Original project: https://github.com/pufferfish-gg/Pufferfish

This replaces the current method of ticking an inactive entity's
pathfinder 1/4 times with a new method that's dynamic based off how far
away it is from a player. If an entity is within 32 blocks, it gets
ticked every tick. If it's within 45 blocks, it gets ticked every other
tick. If it's within 55 blocks, it gets ticked once every three ticks.
(these numbers have since been changed, but the idea is the same.)

Airplane
Copyright (C) 2020 Technove LLC

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## leaf-server/minecraft-patches/features/0087-Fix-Pufferfish-and-Purpur-patches.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 29 Apr 2024 14:18:58 -0400
Subject: [PATCH] Fix Pufferfish and Purpur patches

Co-authored-by: OverwriteMC <artyom030803@gmail.com>
```

## leaf-server/minecraft-patches/features/0088-Purpur-Configurable-server-mod-name.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 17 Jul 2023 08:31:51 +0800
Subject: [PATCH] Purpur: Configurable server mod name

Original license: MIT
Original project: https://github.com/PurpurMC/Purpur
```

## leaf-server/minecraft-patches/features/0089-Configurable-server-GUI-name.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 14 Jan 2024 05:14:09 -0500
Subject: [PATCH] Configurable server GUI name
```

## leaf-server/minecraft-patches/features/0090-Configurable-vanilla-username-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 12 Oct 2022 14:36:58 -0400
Subject: [PATCH] Configurable vanilla username check
```

## leaf-server/minecraft-patches/features/0091-Remove-Spigot-check-for-broken-BungeeCord-configurat.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 12 Oct 2022 14:48:45 -0400
Subject: [PATCH] Remove Spigot check for broken BungeeCord configurations
```

## leaf-server/minecraft-patches/features/0092-Remove-UseItemOnPacket-Too-Far-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 18 Nov 2022 23:26:16 -0500
Subject: [PATCH] Remove UseItemOnPacket Too Far check

This Check is added in 1.17.x -> 1.18.x that updated by Mojang.
By removing this check, it gives ability for hackers to use some modules of hack clients.
```

## leaf-server/minecraft-patches/features/0093-Remove-change-non-editable-sign-warning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 28 Mar 2024 14:04:35 -0400
Subject: [PATCH] Remove change non-editable sign warning
```

## leaf-server/minecraft-patches/features/0094-KeYi-Add-an-option-for-spigot-item-merging-mechanism.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalgic853 <yuu8583@proton.me>
Date: Sun, 23 Oct 2022 23:21:45 +0800
Subject: [PATCH] KeYi: Add an option for spigot item merging mechanism

Original license: MIT
Original project: https://github.com/KeYiMC/KeYi
```

## leaf-server/minecraft-patches/features/0095-Carpet-Fixes-Optimized-getBiome-method.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalgic853 <yuu8583@proton.me>
Date: Tue, 25 Oct 2022 00:28:35 +0800
Subject: [PATCH] Carpet-Fixes: Optimized getBiome method

Original license: MIT
Original project: https://github.com/fxmorin/carpet-fixes

Optimized the getBiome call to be 25% - 75% faster
This is a fully vanilla optimization.

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0096-Carpet-Fixes-Use-optimized-RecipeManager.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalgic853 <yuu8583@proton.me>
Date: Tue, 25 Oct 2022 00:57:45 +0800
Subject: [PATCH] Carpet-Fixes: Use optimized RecipeManager

This patch is based on the following mixin:
"carpetfixes/mixins/optimizations/RecipeManager_fasterMixin.java"
By: fxmorin <28154542+fxmorin@users.noreply.github.com>

Original license: MIT
Original project: https://github.com/fxmorin/carpet-fixes

Dreeam: Remove stream, better than original version

Optimized the RecipeManager getFirstMatch call to be up to 3x faster
This is a fully vanilla optimization. Improves: [Blast]Furnace/Campfire/Smoker/Stonecutter/Crafting/Sheep Color Choosing
This was mostly made for the auto crafting table, since the performance boost is much more visible while using that mod
```

## leaf-server/minecraft-patches/features/0097-Akarin-Save-Json-list-asynchronously.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: =?UTF-8?q?=E3=84=97=E3=84=A0=CB=8B=20=E3=84=91=E3=84=A7=CB=8A?=
 <tsao-chi@the-lingo.org>
Date: Thu, 5 Jan 2023 09:08:17 +0800
Subject: [PATCH] Akarin: Save Json list asynchronously

Original license: GPL-3.0-only
Original project: https://github.com/Akarin-project/Akarin
```

## leaf-server/minecraft-patches/features/0098-Slice-Smooth-Teleports.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Cryptite <cryptite@gmail.com>
Date: Sat, 13 Aug 2022 08:58:14 -0500
Subject: [PATCH] Slice: Smooth Teleports

Original license: MIT
Original project: https://github.com/Cryptite/Slice

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0099-Parchment-Make-FixLight-use-action-bar.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Blast-MC <cjblanton2@gmail.com>
Date: Thu, 25 Aug 2022 20:32:01 -0400
Subject: [PATCH] Parchment: Make FixLight use action bar

Original license: GPL-3.0-only
Original project: https://github.com/ProjectEdenGG/Parchment
```

## leaf-server/minecraft-patches/features/0100-Leaves-Protocol-Core.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Sun, 2 Feb 2025 13:08:32 +0800
Subject: [PATCH] Leaves: Protocol Core

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

Commit: a04cb9530e6eb291390bc32aa650f64825bd4d73
```

## leaf-server/minecraft-patches/features/0101-Leaves-Jade-Protocol.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Mon, 3 Feb 2025 13:33:19 +0800
Subject: [PATCH] Leaves: Jade Protocol

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by Jade (https://github.com/Snownee/Jade)
```

## leaf-server/minecraft-patches/features/0102-Leaves-Xaero-Map-Protocol.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Fri, 7 Feb 2025 14:23:43 +0800
Subject: [PATCH] Leaves: Xaero Map Protocol

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by Xaero Map
```

## leaf-server/minecraft-patches/features/0103-Leaves-Syncmatica-Protocol.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Mon, 3 Feb 2025 16:51:01 +0800
Subject: [PATCH] Leaves: Syncmatica Protocol

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by Syncmatica (https://github.com/End-Tech/syncmatica)
```

## leaf-server/minecraft-patches/features/0104-Leaves-Replay-Mod-API.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Tue, 4 Feb 2025 19:45:19 +0800
Subject: [PATCH] Leaves: Replay Mod API

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by ReplayMod (https://github.com/ReplayMod)
```

## leaf-server/minecraft-patches/features/0105-Petal-Async-Pathfinding.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: peaches94 <peachescu94@gmail.com>
Date: Sun, 26 Jun 2022 16:51:37 -0500
Subject: [PATCH] Petal: Async Pathfinding

Fixed & Updated by KaiijuMC
Original license: GPL-3.0-only
Original project:  https://github.com/KaiijuMC/Kaiiju

Original license: GPL-3.0-only
Original project: https://github.com/Bloom-host/Petal

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Co-authored-by: Taiyou06 <kaandindar21@gmail.com>
Co-authored-by: Altiami <yoshimo.kristin@gmail.com>

This patch was ported downstream from the Petal fork.

Makes most pathfinding-related work happen asynchronously
```

## leaf-server/minecraft-patches/features/0106-Petal-reduce-work-done-by-game-event-system.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: peaches94 <peachescu94@gmail.com>
Date: Sun, 10 Jul 2022 13:29:20 -0500
Subject: [PATCH] Petal: reduce work done by game event system

Original license: GPL-3.0-only
Original project: https://github.com/Bloom-host/Petal

1. going into game event dispatching can be expensive so run the checks before dispatching

2. EuclideanGameEventListenerRegistry is not used concurrently so we ban that usage for improved performance with allays
```

## leaf-server/minecraft-patches/features/0107-Reduce-canSee-work.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 11 Sep 2023 15:47:19 -0400
Subject: [PATCH] Reduce canSee work

Co-authored by: Martijn Muijsers <martijnmuijsers@live.nl>
Co-authored by: MachineBreaker <machinebreaker>
```

## leaf-server/minecraft-patches/features/0108-Fix-sprint-glitch.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: pixelNightly <pixelnightly@proton.me>
Date: Mon, 4 Dec 2023 16:11:36 +0200
Subject: [PATCH] Fix sprint glitch
```

## leaf-server/minecraft-patches/features/0109-Configurable-movement-speed-of-more-entities.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 17 Dec 2023 19:34:46 -0500
Subject: [PATCH] Configurable movement speed of more entities
```

## leaf-server/minecraft-patches/features/0110-Faster-sequencing-of-futures-for-chunk-structure-gen.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 2 Jan 2024 21:13:53 -0500
Subject: [PATCH] Faster sequencing of futures for chunk structure gen

Replace `thenApply` with `thenCompose`. Once one task is completed then the next task starts immediately,
to prevent blocking threads while waiting to complete all tasks. But may cause the sequence of future compose disorder.
```

## leaf-server/minecraft-patches/features/0111-Luminol-Configurable-region-format-framework.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrHua269 <mrhua269@gmail.com>
Date: Sat, 18 Apr 2026 09:03:56 +0800
Subject: [PATCH] Luminol: Configurable region format framework

This patch contains implementations that are based on the following projects:

Region format framework & Buffered Linear
Original license: GPL-3.0-only
Original project: https://github.com/LuminolMC/Luminol

Linear V2
Original license: GPL-3.0-only
Original project: https://github.com/xymb-endcrystalme/Abomination
```

## leaf-server/minecraft-patches/features/0112-Plazma-Add-some-missing-Pufferfish-configurations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: AlphaKR93 <dev@alpha93.kr>
Date: Wed, 27 Sep 2023 18:29:51 +0900
Subject: [PATCH] Plazma: Add some missing Pufferfish configurations

Original license: MIT
Original project: https://github.com/PlazmaMC/PlazmaBukkit

Add Pufferfish DAB support for Camel, Sniffer
https://github.com/pufferfish-gg/Pufferfish/issues/83
```

## leaf-server/minecraft-patches/features/0113-Plazma-Add-missing-purpur-configuration-options.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: AlphaKR93 <dev@alpha93.kr>
Date: Sat, 25 Mar 2023 00:52:11 +0900
Subject: [PATCH] Plazma: Add missing purpur configuration options

Original license: MIT
Original project: https://github.com/PlazmaMC/PlazmaBukkit

Add more Purpur configurable options for entities

Dreeam note:
Some new mobs added in newer versions of Minecraft didn't support Purpur's isControllable features.
I have no interest in adding this kind of new feature in Leaf. If you want, ask Purpur's support.
```

## leaf-server/minecraft-patches/features/0114-SparklyPaper-Skip-distanceToSqr-call-in-ServerEntity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Wed, 15 Nov 2023 23:39:36 -0300
Subject: [PATCH] SparklyPaper: Skip "distanceToSqr" call in
 "ServerEntity#sendChanges" if the delta movement hasn't changed

Original project: https://github.com/SparklyPower/SparklyPaper

The "distanceToSqr" call is a bit expensive, so avoiding it is pretty nice, around ~15% calls are skipped with this check

We could also check if the x,y,z coordinates are equal, but for now, let's just keep the identity check, which also helps us since Minecraft's code does reuse the original delta movement Vec3 object
```

## leaf-server/minecraft-patches/features/0115-SparklyPaper-Skip-MapItem-update-if-the-map-does-not.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Fri, 17 Nov 2023 14:22:41 -0300
Subject: [PATCH] SparklyPaper: Skip "MapItem#update()" if the map does not
 have the CraftMapRenderer present

Original project: https://github.com/SparklyPower/SparklyPaper

Optimizes "image in map" maps, without requiring the map to be locked, which some old map plugins may not do

This has the disadvantage that the vanilla map data will never be updated while the CraftMapRenderer is not present, but that's not a huuuge problem for u
```

## leaf-server/minecraft-patches/features/0116-SparklyPaper-Optimize-canSee-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Thu, 23 Nov 2023 14:36:47 -0300
Subject: [PATCH] SparklyPaper: Optimize "canSee" checks

Original project: https://github.com/SparklyPower/SparklyPaper

The "canSee" checks is in a hot path, invoked by each entity for each player on the server if they are in tracking range, so optimizing it is pretty nice

First, we change the original "HashMap" to fastutil's "Object2ObjectOpenHashMap", because the containsKey throughput is better

Then, we add a "isEmpty()" check before attempting to check if the map contains something

This seems stupid, but it does seem that it improves the performance a bit, and it makes sense, "containsKey(...)" does not attempt to check the map size before attempting to check if the map contains the key

We also create a "canSee" method tailored for "ChunkMap#updatePlayer()", a method without the equals check (the "updatePlayer()" already checks if the entity is the same entity) because the CraftPlayer's `equals()` check is a *bit* expensive compared to only checking the object's identity, and because the identity has already been check, we don't need to check it twice.
```

## leaf-server/minecraft-patches/features/0117-Polpot-Make-egg-and-snowball-can-knockback-player.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: lilingfengdev <145678359+lilingfengdev@users.noreply.github.com>
Date: Thu, 18 Jan 2024 13:30:02 +0800
Subject: [PATCH] Polpot: Make egg and snowball can knockback player

Original project: https://github.com/HaHaWTH/Polpot

Added knockback for snowball and egg.
Set hurtMarked to true to be able to send velocity changes
under Entity#sendChanges via ClientboundSetEntityMotionPacket
```

## leaf-server/minecraft-patches/features/0118-Remove-useless-creating-stats-json-bases-on-player-n.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 27 Feb 2024 03:17:10 -0500
Subject: [PATCH] Remove useless creating stats json bases on player name logic
```

## leaf-server/minecraft-patches/features/0119-Improve-Purpur-AFK-system.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 2 Mar 2024 18:22:15 -0500
Subject: [PATCH] Improve Purpur AFK system

AFK command & command cooldown
AFK title message
```

## leaf-server/minecraft-patches/features/0120-Virtual-thread-support-for-chat-executor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Fri, 25 Oct 2024 02:27:21 +0800
Subject: [PATCH] Virtual thread support for chat executor
```

## leaf-server/minecraft-patches/features/0121-Virtual-thread-support-for-user-authenticator.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 31 Dec 2024 00:00:00 -0800
Subject: [PATCH] Virtual thread support for user authenticator
```

## leaf-server/minecraft-patches/features/0122-Virtual-thread-support-for-download-pool.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 6 Apr 2025 11:22:35 +0200
Subject: [PATCH] Virtual thread support for download pool
```

## leaf-server/minecraft-patches/features/0123-Mirai-Configurable-chat-message-signatures.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: etil2jz <81570777+etil2jz@users.noreply.github.com>
Date: Tue, 2 Aug 2022 14:48:12 +0200
Subject: [PATCH] Mirai: Configurable chat message signatures

Fixed & Updated by Dreeam-qwq
Original license: GPL-3.0-only
Original project: https://github.com/Dreeam-qwq/Mirai

Original license: GPL-3.0-only
Original project: https://github.com/etil2jz/Mirai
```

## leaf-server/minecraft-patches/features/0124-Matter-Secure-Seed.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Apehum <apehumchik@gmail.com>
Date: Thu, 9 Dec 2021 02:18:17 +0800
Subject: [PATCH] Matter: Secure Seed

TODO - Dreeam:
Update to BLAKE3

Original license: GPL-3.0-only
Original project: https://github.com/plasmoapp/matter

Co-authored-by: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0125-Matter-Secure-Seed-command.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Apehum <apehumchik@gmail.com>
Date: Thu, 16 Dec 2021 04:23:40 +0800
Subject: [PATCH] Matter: Secure Seed command

Original license: GPL-3.0-only
Original project: https://github.com/plasmoapp/matter
```

## leaf-server/minecraft-patches/features/0126-Faster-random-generator.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Faster random generator

This patch replaces LegacyRandomSource with FasterRandomSource by default,
which is faster in general.

Benchmark results (10,000,000 iterations) (Azul Zulu 23.0.1)

FasterRandomSource (Leaf) (Backed by Xoroshiro128PlusPlus): 51,633,700 ns
LegacyRandomSource (Vanilla): 254,857,500 ns
ThreadUnsafeRandom (Moonrise): 102,265,100 ns
SimpleThreadUnsafeRandom (Moonrise): 97,054,600 ns
```

## leaf-server/minecraft-patches/features/0127-Don-t-save-primed-tnt-entity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalfinals <yuu8583@proton.me>
Date: Mon, 29 Apr 2024 23:30:21 +0800
Subject: [PATCH] Don't save primed tnt entity
```

## leaf-server/minecraft-patches/features/0128-Don-t-save-falling-block-entity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalfinals <yuu8583@proton.me>
Date: Mon, 29 Apr 2024 23:31:25 +0800
Subject: [PATCH] Don't save falling block entity
```

## leaf-server/minecraft-patches/features/0129-Configurable-connection-message.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 2 Jun 2024 01:21:36 +0800
Subject: [PATCH] Configurable connection message
```

## leaf-server/minecraft-patches/features/0130-Configurable-unknown-command-message.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 7 Aug 2024 18:54:01 +0800
Subject: [PATCH] Configurable unknown command message
```

## leaf-server/minecraft-patches/features/0131-Remove-stream-in-BlockBehaviour-cache-blockstate.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 7 Jun 2024 17:43:43 +0800
Subject: [PATCH] Remove stream in BlockBehaviour cache blockstate
```

## leaf-server/minecraft-patches/features/0132-Remove-stream-in-entity-visible-effects-filter.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 17 Sep 2024 02:26:44 -0400
Subject: [PATCH] Remove stream in entity visible effects filter
```

## leaf-server/minecraft-patches/features/0133-Remove-stream-and-double-iteration-in-enough-deep-sl.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 17 Oct 2024 01:51:38 -0400
Subject: [PATCH] Remove stream and double iteration in enough deep sleeping
 player check
```

## leaf-server/minecraft-patches/features/0134-Remove-stream-in-trial-spawner-ticking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 5 Oct 2024 15:39:15 -0400
Subject: [PATCH] Remove stream in trial spawner ticking
```

## leaf-server/minecraft-patches/features/0135-Remove-stream-in-Brain.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sat, 26 Oct 2024 00:06:04 +0800
Subject: [PATCH] Remove stream in Brain
```

## leaf-server/minecraft-patches/features/0136-Remove-stream-in-BehaviorUtils.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sat, 26 Oct 2024 00:56:24 +0800
Subject: [PATCH] Remove stream in BehaviorUtils
```

## leaf-server/minecraft-patches/features/0137-Remove-stream-in-YieldJobSite.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sat, 26 Oct 2024 14:04:54 +0800
Subject: [PATCH] Remove stream in YieldJobSite
```

## leaf-server/minecraft-patches/features/0138-Remove-stream-in-PlayerSensor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Remove stream in PlayerSensor

Stream operations in PlayerSensor take too much time
while ticking Villager farms, so just replace it with for loop =-=
Before: 164ms
After: 18ms
```

## leaf-server/minecraft-patches/features/0139-Remove-stream-in-GolemSensor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Remove stream in GolemSensor

Stream operations in GolemSensor is really expensive and takes
up 80% time per method call.
Before: 192ms
After: 17ms
```

## leaf-server/minecraft-patches/features/0140-Remove-stream-in-GateBehavior.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Remove stream in GateBehavior
```

## leaf-server/minecraft-patches/features/0141-Remove-stream-in-matchingSlot.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Tue, 26 Nov 2024 19:58:29 -0500
Subject: [PATCH] Remove stream in matchingSlot
```

## leaf-server/minecraft-patches/features/0142-Remove-stream-in-villagers.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 8 Feb 2025 22:11:16 +0100
Subject: [PATCH] Remove stream in villagers

TradeWithVillager#figureOutWhatIAmWillingToTrade
In the test, this can give ~40% improvement (~20ms -> ~12ms),
under 2048 villagers situation.
And ~100% improvement (~36ms -> ~0ms), under 512 villagers situation.

Villager#countFoodPointsInInventory
In the test, this can give ~82.14% improvement (~1456ms -> ~260ms),
under 2048 villagers situation.
And ~93.92% improvement (~1382ms -> ~84ms), under 512 villagers situation.
```

## leaf-server/minecraft-patches/features/0143-Remove-stream-on-updateConnectedPlayersWithinRange.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 22 Mar 2025 11:22:23 +0100
Subject: [PATCH] Remove stream on updateConnectedPlayersWithinRange
```

## leaf-server/minecraft-patches/features/0144-Remove-stream-on-PlayerDetector.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 22 Mar 2025 12:51:28 +0100
Subject: [PATCH] Remove stream on PlayerDetector

Dreeam TODO: Merge to single loop
```

## leaf-server/minecraft-patches/features/0145-Remove-stream-in-MobSensor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 12 May 2025 19:11:16 +0200
Subject: [PATCH] Remove stream in MobSensor
```

## leaf-server/minecraft-patches/features/0146-Remove-stream-in-TemptingSensor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 12 May 2025 19:39:03 +0200
Subject: [PATCH] Remove stream in TemptingSensor
```

## leaf-server/minecraft-patches/features/0147-Remove-stream-in-CraftWorld-spawnParticle.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 29 Oct 2025 02:13:08 -0400
Subject: [PATCH] Remove stream in CraftWorld#spawnParticle
```

## leaf-server/minecraft-patches/features/0148-Replace-Entity-active-effects-map-with-optimized-col.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 17 Sep 2024 02:35:13 -0400
Subject: [PATCH] Replace Entity active effects map with optimized collection

Dreeam TODO: check this
```

## leaf-server/minecraft-patches/features/0149-Replace-criterion-map-with-optimized-collection.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 7 Sep 2024 02:12:55 -0400
Subject: [PATCH] Replace criterion map with optimized collection
```

## leaf-server/minecraft-patches/features/0150-Replace-brain-with-optimized-collection.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sat, 26 Oct 2024 00:06:04 +0800
Subject: [PATCH] Replace brain with optimized collection

Co-authored-by: Taiyou06 <kaandindar21@gmail.com>
Co-authored-by: hayanesuru <hayanesuru@outlook.jp>
```

## leaf-server/minecraft-patches/features/0151-Reduce-worldgen-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Fri, 14 Jun 2024 23:19:55 +0800
Subject: [PATCH] Reduce worldgen allocations

This change optimizes the way SurfaceRules update their biome supplier,avoiding unnecessary object creations and thus reducing memory allocations
during world generation. The update method now reuses the existing PositionalBiomeGetter object if it's already present, otherwise it
initializes a new one.
Additionally, the tryApply method in SurfaceRules now avoids iterator
allocation by directly accessing the rules list, which further contributes
to reducing garbage collection pressure during world generation.
```

## leaf-server/minecraft-patches/features/0152-Use-caffeine-cache-for-kickPermission-instead-of-usi.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 23 Jun 2024 11:26:20 +0800
Subject: [PATCH] Use caffeine cache for kickPermission instead of using
 google.common.cache

This patch includes code that references the Caffeine caching library,
which is licensed under the Apache License, Version 2.0.

Caffeine (https://github.com/ben-manes/caffeine)
Copyright (c) Ben Manes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## leaf-server/minecraft-patches/features/0153-Fix-MC-17876.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-17876

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-17876

Temporarily bypass the max health check during entity creation, as equipment attributes are not applied yet at this stage.

Co-authored-by: Bjarne Koll <git@lynxplay.dev>
```

## leaf-server/minecraft-patches/features/0154-Fix-MC-65198.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <fsjk947@gmail.com>
Date: Wed, 13 Mar 2024 03:33:08 +0800
Subject: [PATCH] Fix MC-65198

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-65198
```

## leaf-server/minecraft-patches/features/0155-Fix-MC-200418.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sun, 7 Jul 2024 01:29:57 +0800
Subject: [PATCH] Fix MC-200418

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-200418
```

## leaf-server/minecraft-patches/features/0156-Fix-MC-223153.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sun, 7 Jul 2024 01:59:11 +0800
Subject: [PATCH] Fix MC-223153

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-223153
```

## leaf-server/minecraft-patches/features/0157-Fix-MC-270656.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-270656

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-270656
```

## leaf-server/minecraft-patches/features/0158-Fix-MC-298464.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sun, 13 Jul 2025 19:40:10 +0900
Subject: [PATCH] Fix MC-298464

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-298464

Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0159-Fix-MC-299642.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-299642

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-299642

Minecraft uses 3D movement control for turtles to move in water, which makes them fall even NoGravity tag is set to true.
This direct manipulation of deltaMovement bypasses the standard move logic where the NoGravity tag is checked and applied.
```

## leaf-server/minecraft-patches/features/0160-Fix-MC-152094.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-152094

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-152094
```

## leaf-server/minecraft-patches/features/0161-Configurable-player-knockback-zombie.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 4 Aug 2024 19:34:29 +0800
Subject: [PATCH] Configurable player knockback zombie
```

## leaf-server/minecraft-patches/features/0162-Paper-PR-Skip-AI-during-inactive-ticks-for-non-aware.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: froobynooby <froobynooby@froobworld.com>
Date: Mon, 5 Aug 2024 20:33:57 +0800
Subject: [PATCH] Paper PR: Skip AI during inactive ticks for non-aware mobs

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/10990
```

## leaf-server/minecraft-patches/features/0163-Paper-PR-Prevent-zombie-reinforcements-loading-chunk.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Newwind <support@newwindserver.com>
Date: Sat, 27 Jul 2024 11:09:37 +0200
Subject: [PATCH] Paper PR: Prevent zombie reinforcements loading chunks

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/11175

When a zombie calls reinforcements it tries to spawn them in a random location within a 40 block radius of the zombie,
before spawning, it checks isSpawnPositionOk() for the position which loads the block to check if a mob can spawn on said block.
This patch ensures the chunk at the random location is loaded before trying to spawn the reinforcement zombie in it.
```

## leaf-server/minecraft-patches/features/0164-Paper-PR-Fix-some-beacon-event-issues.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Warrior <50800980+Warriorrrr@users.noreply.github.com>
Date: Thu, 31 Aug 2023 10:23:06 +0200
Subject: [PATCH] Paper PR: Fix some beacon event issues

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/9674

Closes Paper#8947

Moves the deactivate event call into the onRemove method for the beacon block itself to prevent it from running when the block entity is unloaded. Also fixes an issue where the events were not being called when the beacon beam gets blocked.

The field I added feels a bit wrong but it works, it's to prevent the activation event being called immediately after loading, can't see any better way to differentiate between a newly placed beacon and a newly loaded one.
```

## leaf-server/minecraft-patches/features/0165-Dont-send-useless-entity-packets.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: William Blake Galbreath <blake.galbreath@gmail.com>
Date: Sat, 6 Jul 2019 17:00:04 -0500
Subject: [PATCH] Dont send useless entity packets

TODO: Add more reducers

Original license: MIT
Original project: https://github.com/PurpurMC/Purpur
```

## leaf-server/minecraft-patches/features/0166-optimize-attribute.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 14 Aug 2025 14:12:38 +0900
Subject: [PATCH] optimize attribute
```

## leaf-server/minecraft-patches/features/0167-Nitori-Async-playerdata-saving.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 23 Aug 2024 22:04:20 -0400
Subject: [PATCH] Nitori: Async playerdata saving

Original license: GPL-3.0-only
Original project: https://github.com/Gensokyo-Reimagined/Nitori
```

## leaf-server/minecraft-patches/features/0168-Optimize-nearby-alive-players-for-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 19 Oct 2024 03:28:33 -0400
Subject: [PATCH] Optimize nearby alive players for spawning

Use SpottedLeaf's nearby players system to avoid iterating over all online players
and reduce the cost on predicate test
```

## leaf-server/minecraft-patches/features/0169-Cache-blockstate-cache-array.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 22 Oct 2024 17:07:36 +0800
Subject: [PATCH] Cache blockstate cache array

Cache values to reduce allocations.
(See `Reduce array allocations` patch for full explanation)

The JMH benchmark of this patch can be found in SunBox's `CachedEnumValuesForLoop`
```

## leaf-server/minecraft-patches/features/0170-Optimize-Entity-distanceToSqr.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Fri, 29 Oct 2077 00:00:00 +0800
Subject: [PATCH] Optimize Entity distanceToSqr

This patch optimizes Entity#distanceToSqr call by using Math#fma which is around 1.2x to 4x faster than original method,
avoids multiple casting in Entity#distanceTo, using Math#sqrt directly instead of Mojang's Mth#sqrt. Additionally, this patch makes
these methods more able to be inlined by the JIT compiler.
```

## leaf-server/minecraft-patches/features/0171-EMC-Don-t-use-snapshots-for-TileEntity-getOwner.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Aikar <aikar@aikar.co>
Date: Tue, 7 Nov 2017 00:01:04 -0500
Subject: [PATCH] EMC: Don't use snapshots for TileEntity::getOwner

Original license: MIT
Original project: https://github.com/starlis/empirecraft

Also see Leaf's EMC-Don-t-use-snapshots-for-acquiring-blockstate
```

## leaf-server/minecraft-patches/features/0172-Cache-tile-entity-position.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 25 Oct 2024 22:27:07 -0400
Subject: [PATCH] Cache tile entity position

Dreeam TODO: Check if there is a way to cache isRemoved without problem
```

## leaf-server/minecraft-patches/features/0173-TT20-Lag-compensation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Mon, 1 Nov 2077 00:00:00 +0800
Subject: [PATCH] TT20: Lag compensation

Original license: AGPL-3.0-only
Original project: https://github.com/snackbag/TT20

Co-authored-by: MrlingXD <90316914+wling-art@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0174-C2ME-Reduce-Allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 7 Nov 2024 19:45:31 +0100
Subject: [PATCH] C2ME: Reduce Allocations

This patch is based on the following mixin:
"com/ishland/c2me/opts/allocs/mixin/object_pooling_caching/MixinOreFeature.java"
By: ishland <ishlandmc@yeah.net>
As part of: C2ME (https://github.com/RelativityMC/C2ME-fabric)
Licensed under: MIT (https://opensource.org/licenses/MIT)
```

## leaf-server/minecraft-patches/features/0175-Lithium-Skip-unnecessary-calculations-if-player-is-n.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: kidofcubes <kidofcubes@gmail.com>
Date: Fri, 8 Nov 2024 00:22:44 +0800
Subject: [PATCH] Lithium: Skip unnecessary calculations if player is not
 flying or swing

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/entity/fast_elytra_check/LivingEntityMixin.java"
* "net/caffeinemc/mods/lithium/mixin/entity/fast_hand_swing/LivingEntityMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0176-Lithium-fast-util.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 7 Nov 2024 21:50:47 +0100
Subject: [PATCH] Lithium: fast util

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/math/fast_util/AABBMixin.java"
* "net/caffeinemc/mods/lithium/mixin/math/fast_util/AxisCycleDirectionMixin.java"
* "net/caffeinemc/mods/lithium/mixin/math/fast_util/DirectionMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0177-Lithium-cached-iterate-outwards.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Fri, 8 Nov 2024 00:06:34 +0100
Subject: [PATCH] Lithium: cached iterate outwards

By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0178-Use-faster-and-thread-safe-ban-list-date-format-pars.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 11 Nov 2024 02:46:39 -0500
Subject: [PATCH] Use faster and thread-safe ban list date format parsing

Dreeam TODO: check is there need to use more accurate benchmark using jmh?

Use DateTimeFormatter since the original java SimpleDateFormat is not thread-safe
If calls DateTimeFormatter asynchronously, one data format may pollute another

This can fix the server crash on loading according to the user report.
The server crashed during initing the IP ban list, probably caused by calls in off-main.

Some performance test **only for reference**
Single thread, 10,000,000 times loop, java 21 (graalvm / zulu)

SimpleDateFormat:                       ~29,446ms
DateTimeFormatter:                      ~13,128ms
apache commons-lang's FastDateFormat:   ~23,514ms

In the end, DateTimeFormatter is also fastest in three implementations in any ways,
Wether there is a high frequnently calls or not. And also thread-safe. So there is
a better solution, why not using it?
```

## leaf-server/minecraft-patches/features/0179-C2ME-Optimize-world-gen-math.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] C2ME: Optimize world gen math

This patch is based on following mixins:
* "com/ishland/c2me/opts/math/mixin/MixinChunkPos.java"
* "com/ishland/c2me/opts/worldgen/vanilla/mixin/structure_weight_sampler/MixinStructureWeightSampler.java"
By: ishland <ishlandmc@yeah.net>
As part of: C2ME-fabric (https://github.com/RelativityMC/C2ME-fabric)
Licensed under: MIT
```

## leaf-server/minecraft-patches/features/0180-Reduce-fluid-status-allocation-in-NoiseBasedChunkGen.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 14 Nov 2024 19:54:22 +0800
Subject: [PATCH] Reduce fluid status allocation in NoiseBasedChunkGenerator
```

## leaf-server/minecraft-patches/features/0181-Cache-chunk-key.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 2 Nov 2024 04:15:20 -0400
Subject: [PATCH] Cache chunk key

Cache convert process in ChunkPos to the chunkKey, to avoid unnecessary casting and shift operations.
This patch didn't cache SectionPos or BlockPos to chunkKey, since they are mutable after creation.

The JMH benchmark of this patch can be found in SunBox's `CacheChunkKey`
```

## leaf-server/minecraft-patches/features/0182-Configurable-tripwire-dupe.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 24 Dec 2024 13:28:56 -0500
Subject: [PATCH] Configurable tripwire dupe

Bring back MC-59471, MC-129055 on 1.21.2+, which fixed in 1.21.2 snapshots 24w33a and 24w36a

Note: this is different from Paper's skip-tripwire-hook-placement-validation, that setting
handles tripwire hook dupe
```

## leaf-server/minecraft-patches/features/0183-Paper-PR-Fix-MC-117075-Block-Entities-Unload-Lag-Spi.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Sun, 26 Nov 2023 13:02:16 -0300
Subject: [PATCH] Paper PR: Fix MC-117075: Block Entities Unload Lag Spike

Original license: GPL-3.0-only
Original project: https://github.com/SparklyPower/SparklyPaper
Paper pull request: https://github.com/PaperMC/Paper/pull/9970

We replaced the `blockEntityTickers` list with a custom list based on fastutil's `ObjectArrayList` with a small yet huge change for us: A method that allows us to remove a list of indexes from the list.

This is WAY FASTER than using `removeAll` with a list of entries to be removed, because we don't need to calculate the identity of each block entity to be removed, and we can jump directly to where the search should begin, giving a performance boost for small removals (because we don't need to loop thru the entire list to find what element should be removed) and a performance boost for big removals (no need to calculate the identity of each block entity).
```

## leaf-server/minecraft-patches/features/0184-Sepals-Rearrange-the-attackable-conditions.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: cao-awa <cao.awa.www@gmail.com>
Date: Fri, 23 Aug 2024 00:23:32 +0800
Subject: [PATCH] Sepals: Rearrange the attackable conditions

Original license: GPL-3.0-only
Original project: https://github.com/cao-awa/Sepals

Rearrange the attackable conditions
let less costs predicate running first
reduce the probability of high-costs calculating

-- The complaints --
Mojang's attackable predicate is:

!entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN)
 && Sensor.testAttackableTargetPredicate(entity, target)
 && FrogEntity.isValidFrogFood(target)
 && !this.isTargetUnreachable(entity, target)
 && target.isInRange(entity, 10.0)

in this case, 'Sensor#testAttackableTargetPredicate' has calls 'TargetPredicate#test'
that cause a very lots raycast calculate when entities too much in the area
but... minecraft's raycast is absolutely bad, very slow

the 'TargetPredicate#test' in this case (800 frogs) has make 9.8ms costs in once game tick
among them, 'BlockView.raycast' contributed 7.3ms

then i make it be:

FrogEntity.isValidFrogFood(target) &&
 entity.getBrain().hasMemoryModule(MemoryModuleType.HAS_HUNTING_COOLDOWN) &&
 target.isInRange(entity, 10.0) &&
 Sensor.testAttackableTargetPredicate(entity, target) &&
 isTargetUnreachable(entity, target);

the 'isValidFrogFood' is simple conditions, check the entity's tag has in 'frog_food'
and a extra check when entity is slime then skip it when it size not 1

the 'isInRange' and 'hasMemoryModule' also simple, it only a few math calculates

Test Result:
800 frogs cramming in a 7x7 space:

|                                      Environment |  time  | Percent(Avg.) |
|-------------------------------------------------:|:------:|:-------------:|
|          Vanilla (FrogAttackablesSensor#matches) | 10 ms  |     100 %     |
|     With Lithium (FrogAttackablesSensor#matches) | 5.7 ms |     57 %      |
|         With Sepals (SepalsFrogBrain#attackable) | 0.1 ms |      1 %      |
| With Sepals+Lithium (SepalsFrogBrain#attackable) | 0.1 ms |      1 %      |
```

## leaf-server/minecraft-patches/features/0185-SparklyPaper-Skip-dirty-stats-copy-when-requesting-p.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Mon, 13 Jan 2025 14:32:08 -0300
Subject: [PATCH] SparklyPaper: Skip dirty stats copy when requesting player
 stats

Original project: https://github.com/SparklyPower/SparklyPaper
```

## leaf-server/minecraft-patches/features/0186-SparklyPaper-Reset-dirty-flag-when-loading-maps-from.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Wed, 5 Jun 2024 15:20:00 -0300
Subject: [PATCH] SparklyPaper: Reset dirty flag when loading maps from the
 disk

Original project: https://github.com/SparklyPower/SparklyPaper

By default, the server will start rewriting all map datas to the disk after loading it, even if the map didn't have any changes

This also slows down world saving a lot if you have a lot of maps
```

## leaf-server/minecraft-patches/features/0187-Optimize-checking-nearby-players-for-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 8 Feb 2025 05:32:30 +0100
Subject: [PATCH] Optimize checking nearby players for spawning
```

## leaf-server/minecraft-patches/features/0188-Avoid-useless-deque-clear-on-LevelTicks-cleanupAfter.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MachineBreaker <saltspigotpp@gmail.com>
Date: Wed, 16 Oct 2024 03:39:24 -0400
Subject: [PATCH] Avoid useless deque clear on LevelTicks#cleanupAfterTick
```

## leaf-server/minecraft-patches/features/0189-Optimize-baby-villager-sensor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 8 Feb 2025 22:36:31 +0100
Subject: [PATCH] Optimize baby villager sensor

In the test, this can give ~16.58% improvement (~2316ms -> ~1932ms),
under 2048 villagers situation.
And ~42.93% improvement (~764ms -> ~436ms), under 512 villagers situation.
```

## leaf-server/minecraft-patches/features/0190-Only-player-pushable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 27 Nov 2024 23:13:12 -0500
Subject: [PATCH] Only player pushable

Useful for extreme cases like massive entities collide together in a small area
```

## leaf-server/minecraft-patches/features/0191-Remove-iterators-from-Inventory.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 13 Feb 2025 01:25:40 +0100
Subject: [PATCH] Remove iterators from Inventory
```

## leaf-server/minecraft-patches/features/0192-optimize-mob-despawn.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 4 Jul 2025 03:22:38 +0900
Subject: [PATCH] optimize mob despawn
```

## leaf-server/minecraft-patches/features/0193-Slightly-optimise-getNearestPlayer.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 16 Feb 2025 09:21:50 +0100
Subject: [PATCH] Slightly optimise getNearestPlayer
MIME-Version: 1.0
Content-Type: text/plain; charset=UTF-8
Content-Transfer-Encoding: 8bit

Original:

Condition checks per iteration: ~3-4 (predicate check, distance check, and d comparison)
Calculations per iteration: Distance calculation + potentially a multiplication
Total operations: ~(5-6) × n

Patch:

Condition checks per iteration: 1 (just the distance comparison)
Calculations per iteration: Distance calculation
One-time operation: Convert distance to squared distance
Total operations: ~3 × n (in the no-predicate case) or ~4 × n (with predicate)
```

## leaf-server/minecraft-patches/features/0194-Bulk-writes-to-writeLongArray-during-chunk-loading.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Fri, 21 Feb 2025 15:06:55 +0100
Subject: [PATCH] Bulk writes to writeLongArray during chunk loading
```

## leaf-server/minecraft-patches/features/0195-Improve-sorting-in-SortedArraySet.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 20 Feb 2025 15:37:39 +0100
Subject: [PATCH] Improve sorting in SortedArraySet

Changes between 5% to 10% depending on the size of the array
For size 16: ~5% improvement (735.589 ns vs 697.095 ns)
For size 4096: ~10% improvement (1563.115 ns vs 1396.803 ns)
```

## leaf-server/minecraft-patches/features/0196-Make-removeIf-slightly-faster.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 23 Feb 2025 00:37:39 +0100
Subject: [PATCH] Make removeIf slightly faster
```

## leaf-server/minecraft-patches/features/0197-Rewrite-ClientboundLightUpdatePacketData.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 24 Feb 2025 21:33:24 +0100
Subject: [PATCH] Rewrite ClientboundLightUpdatePacketData
MIME-Version: 1.0
Content-Type: text/plain; charset=UTF-8
Content-Transfer-Encoding: 8bit

On writing operations:
At 256 sections with 0.75 fill ratio: 18x faster (333.489 μs → 18.606 μs)
At 256 sections with 0.5 fill ratio: 13.5x faster (147.804 μs → 10.962 μs)
```

## leaf-server/minecraft-patches/features/0198-async-chunk-sender.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 2 Mar 2025 21:23:20 +0100
Subject: [PATCH] async chunk sender

Co-authored-by: hayanesuru <hayanesuru@outlook.jp>
```

## leaf-server/minecraft-patches/features/0199-Spawner-Configurations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 9 Mar 2025 00:36:26 +0100
Subject: [PATCH] Spawner Configurations
```

## leaf-server/minecraft-patches/features/0200-SparklyPaper-Parallel-world-ticking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrPowerGamerBR <git@mrpowergamerbr.com>
Date: Sun, 25 May 2025 21:39:32 -0300
Subject: [PATCH] SparklyPaper: Parallel world ticking

Original project: https://github.com/SparklyPower/SparklyPaper

Co-authored-by: Altiami <yoshimo.kristin@gmail.com>
Co-authored-by: Taiyou06 <kaandindar21@gmail.com>
Co-authored-by: MrlingXD <90316914+wling-art@users.noreply.github.com>

Commit: 491a14f432bd2ab42ba1586d4a6786b740713662
```

## leaf-server/minecraft-patches/features/0201-SparklyPaper-Track-each-world-MSPT.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 6 Mar 2025 00:26:19 +0100
Subject: [PATCH] SparklyPaper: Track each world MSPT

Original project: https://github.com/SparklyPower/SparklyPaper
```

## leaf-server/minecraft-patches/features/0202-Paper-PR-Fix-cancelled-Projectile-Events-still-consu.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Tamion <70228790+notTamion@users.noreply.github.com>
Date: Sun, 16 Feb 2025 12:22:53 +0100
Subject: [PATCH] Paper PR: Fix cancelled Projectile Events still consuming
 arrows

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/12124

fixes https://github.com/PaperMC/Paper/issues/12123
```

## leaf-server/minecraft-patches/features/0203-Optimize-SetLookAndInteract-and-NearestVisibleLiving.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Wed, 19 Mar 2025 13:41:29 +0100
Subject: [PATCH] Optimize SetLookAndInteract and NearestVisibleLivingEntities

Changes predicate order to be faster
These numbers are gotten with 2000 villagers searching for a Job
Paper: 3859ms
Leaf : 1363ms (-64% reduction)
```

## leaf-server/minecraft-patches/features/0204-rewrite-InsideBrownianWalk.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Wed, 19 Mar 2025 15:18:58 +0100
Subject: [PATCH] rewrite InsideBrownianWalk

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)

This method is mainly visible when ton of villagers suddenly wants to sleep.
```

## leaf-server/minecraft-patches/features/0205-Use-BFS-on-getSlopeDistance.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Wed, 19 Mar 2025 18:41:56 +0100
Subject: [PATCH] Use BFS on getSlopeDistance

Uses Breadth First Search (BFS) to optimize getSlopeDistance
Paper: ~75ms
Leaf: ~48ms (-36%)
This should help drastically on the farms that use actively changing fluids.
```

## leaf-server/minecraft-patches/features/0206-Paper-PR-Throttle-failed-spawn-attempts.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: froobynooby <froobynooby@froobworld.com>
Date: Wed, 17 Jul 2024 18:46:11 +0930
Subject: [PATCH] Paper PR: Throttle failed spawn attempts

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/11099

For example config in paper-world-defaults.yml
```
    spawning-throttle:
      failed-attempts-threshold: 1200
      throttled-ticks-per-spawn:
        ambient: 10 # default value in bukkit.yml tickers-per * 10
        axolotls: 10
        creature: 4000
        monster: 10
        underground_water_creature: 10
        water_ambient: 10
        water_creature: 10
```

This patch adds the option to use longer ticks-per-spawn for a given
mob type in chunks where spawn attempts are consecutively failing.

This behaviour is particularly useful on servers where players build
mob farms. Mob farm designs often require making surrounding chunks
spawnproof, which causes the server to waste CPU cycles trying to spawn mobs in
vain. Throttling spawn attempts in suspected spawnproof chunks improves
performance without noticeably advantaging or disadvantaging the mob farm.
```

## leaf-server/minecraft-patches/features/0207-Improve-BlockEntity-ticking-isRemoved-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: kidofcubes <kidofcubes@gmail.com>
Date: Sun, 16 Feb 2025 00:51:24 +0800
Subject: [PATCH] Improve BlockEntity ticking isRemoved check

Uses direct reference for isRemoved check in BlockEntity ticking,
reducing cost caused by nested wrapper calls
Leaf (Before): ~15104ms
Leaf (After): ~628ms (-96%)
This should help for massive hopper chains or hopper matrix.
```

## leaf-server/minecraft-patches/features/0208-Raytrace-AntiXray-SDK-integration.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: LeeGodSRC <lee20040919@gmail.com>
Date: Sat, 8 Mar 2025 11:40:46 +0800
Subject: [PATCH] Raytrace AntiXray SDK integration

Integrated with Imanity Software's Raytrace AntiXray for better performance

Original project: https://github.com/Imanity-Software/raytrace-antixray-spigot-sdk
```

## leaf-server/minecraft-patches/features/0209-Optimize-ContextMap.create.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 22 Mar 2025 00:08:11 +0100
Subject: [PATCH] Optimize ContextMap.create
```

## leaf-server/minecraft-patches/features/0210-Micro-optimizations-for-random-tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 22 Mar 2025 03:02:52 +0100
Subject: [PATCH] Micro optimizations for random tick
```

## leaf-server/minecraft-patches/features/0211-Use-direct-iteration-on-Sensing.tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 23 Mar 2025 20:48:16 +0100
Subject: [PATCH] Use direct iteration on Sensing.tick
```

## leaf-server/minecraft-patches/features/0212-Optimise-non-flush-packet-sending.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Spottedleaf <Spottedleaf@users.noreply.github.com>
Date: Tue, 22 Sep 2020 01:49:19 -0700
Subject: [PATCH] Optimise non-flush packet sending

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/10172

Places like entity tracking make heavy use of packet sending,
and internally netty will use some very expensive thread wakeup
calls when scheduling.

Thanks to various hacks in ProtocolLib as well as other
plugins, we cannot simply use a queue of packets to group
send on execute. We have to call execute for each packet.

Tux's suggestion here is exactly what was needed - tag
the Runnable indicating it should not make a wakeup call.

Big thanks to Tux for making this possible as I had given
up on this optimisation before he came along.

Locally this patch drops the entity tracker tick by a full 1.5x.

Co-authored-by: Quang Tran <3d7777456@gmail.com>
```

## leaf-server/minecraft-patches/features/0213-Null-handling-on-MultifaceSpreader.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Wed, 2 Apr 2025 23:03:22 +0200
Subject: [PATCH] Null handling on MultifaceSpreader

WHYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
```

## leaf-server/minecraft-patches/features/0214-Optimize-ThreadedTicketLevelPropagator.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 14 Apr 2025 14:36:57 +0200
Subject: [PATCH] Optimize ThreadedTicketLevelPropagator
```

## leaf-server/minecraft-patches/features/0215-Optimise-MobEffectUtil-getDigSpeedAmplification.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 14 Apr 2025 18:07:21 +0200
Subject: [PATCH] Optimise MobEffectUtil#getDigSpeedAmplification
```

## leaf-server/minecraft-patches/features/0216-Optimise-chunkUnloads.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Mon, 14 Apr 2025 20:07:52 +0200
Subject: [PATCH] Optimise chunkUnloads
```

## leaf-server/minecraft-patches/features/0217-Optimize-BlockEntityType-isValid.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <mc@jvavav.com>
Date: Sat, 19 Apr 2025 22:12:19 +0800
Subject: [PATCH] Optimize BlockEntityType#isValid
```

## leaf-server/minecraft-patches/features/0218-Paper-PR-Add-ticket-on-player-join-to-avoid-chunk-lo.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Jason Penilla <11360596+jpenilla@users.noreply.github.com>
Date: Fri, 13 Sep 2024 14:32:32 -0700
Subject: [PATCH] Paper PR: Add ticket on player join to avoid chunk
 load-unload-load cycle

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/11398

Adding the entity will add and then immediately remove an entity load ticket, which would result in the chunk loading and then unloading before being loaded again once the player chunk loader reacts (delay can vary based on rate limit configs)
By adding a ticket with a short removal delay we attempt to keep the chunk loaded until the player chunk loader reacts, but this is not a guarantee due to the aforementioned rate limit configs. Plugins should still handle load/unload events as normal, however this will reduce redundant calls.
The delay is currently set to 2 seconds, however, we may want to adjust this before merging (for example the player chunk unload delay is 5 seconds)

fixes Paper#9581
```

## leaf-server/minecraft-patches/features/0219-Sakura-Optimise-check-inside-blocks-and-traverse-blo.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Samsuik <kfian294ma4@gmail.com>
Date: Fri, 8 Nov 2024 19:35:49 +0000
Subject: [PATCH] Sakura: Optimise check inside blocks and traverse blocks

Original license: MIT
Original project: https://github.com/Samsuik/Sakura

Chunk cache is already removed from this patch, since we already have it in Leaf

The JMH benchmark of this patch can be found in SunBox's `BetterBlocksTraverse`
```

## leaf-server/minecraft-patches/features/0220-Sakura-copy-EntityList-implementation-to-BasicEntity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Samsuik <kfian294ma4@gmail.com>
Date: Sun, 2 Feb 2025 17:05:05 +0000
Subject: [PATCH] Sakura: copy EntityList implementation to BasicEntityList

Original license: MIT
Original project: https://github.com/Samsuik/Sakura
```

## leaf-server/minecraft-patches/features/0221-Protocol-Core.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 6 May 2025 17:44:16 +0900
Subject: [PATCH] Protocol Core
```

## leaf-server/minecraft-patches/features/0222-Async-switch-connection-state.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 9 May 2025 16:55:34 +0900
Subject: [PATCH] Async switch connection state
```

## leaf-server/minecraft-patches/features/0223-Optimize-BlockEntities-tickersInLevel.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Thu, 8 May 2025 13:30:07 +0200
Subject: [PATCH] Optimize BlockEntities tickersInLevel
```

## leaf-server/minecraft-patches/features/0224-Pluto-Check-if-the-cactus-can-even-survive-being-pla.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Yive <6853318+Yive@users.noreply.github.com>
Date: Tue, 11 Jul 2023 13:27:01 -0700
Subject: [PATCH] Pluto: Check if the cactus can even survive being placed

Original license: GPL-3.0-only
Original project: https://github.com/Yive/Pluto

Results at 3,000 randomTickSpeed and 24,448 cacti:

check-survival-before-growth - false & doTileDrop - true:  48mspt
check-survival-before-growth - true  & doTileDrop - true:  25mspt
check-survival-before-growth - false & doTileDrop - false: 18mspt
check-survival-before-growth - true  & doTileDrop - false:  6mspt

Setting the gamerule "doTileDrop" to false was to simulate a server that has chunk collectors.

Note: This might increase the item output of a cacti farm, though in theory it should act the same as vanilla.
```

## leaf-server/minecraft-patches/features/0225-Only-tick-items-at-hand.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Fri, 9 May 2025 23:50:55 +0200
Subject: [PATCH] Only tick items at hand
```

## leaf-server/minecraft-patches/features/0226-Optimise-player-movement-checks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 10 May 2025 23:18:17 +0200
Subject: [PATCH] Optimise player movement checks
```

## leaf-server/minecraft-patches/features/0227-Add-configurable-death-item-drop-knockback-settings.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: adabugra <57899270+adabugra@users.noreply.github.com>
Date: Thu, 15 May 2025 19:09:04 +0300
Subject: [PATCH] Add configurable death item drop knockback settings
```

## leaf-server/minecraft-patches/features/0228-Optimize-getScaledTrackingDistance.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 17 May 2025 19:01:50 +0900
Subject: [PATCH] Optimize getScaledTrackingDistance
```

## leaf-server/minecraft-patches/features/0229-Optimize-SynchedEntityData-packDirty.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 17 May 2025 19:03:31 +0900
Subject: [PATCH] Optimize SynchedEntityData#packDirty
```

## leaf-server/minecraft-patches/features/0230-Optimize-isEyeInFluid.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrlingXD <90316914+wling-art@users.noreply.github.com>
Date: Sat, 17 May 2025 08:25:33 +0800
Subject: [PATCH] Optimize isEyeInFluid
```

## leaf-server/minecraft-patches/features/0231-Cache-block-state-tags.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 23 May 2025 12:01:42 +0900
Subject: [PATCH] Cache block state tags
```

## leaf-server/minecraft-patches/features/0232-optimize-getEntityStatus.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 23 May 2025 15:57:42 +0900
Subject: [PATCH] optimize getEntityStatus
```

## leaf-server/minecraft-patches/features/0233-Rail-Optimization-optimized-PoweredRailBlock-logic.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 17 Feb 2024 17:57:08 -0500
Subject: [PATCH] Rail Optimization: optimized PoweredRailBlock logic

Original project: https://github.com/FxMorin/RailOptimization

Full Rewrite of the powered rail iteration logic
that makes powered/activator rails turning on/off up to 4x faster.
This rewrite brings a massive performance boost while keeping the vanilla order. This is achieved by running all the
powered rail logic from a single rail instead of each block iterating separately. Which was not only very
expensive but also completely unnecessary and with a lot of massive overhead
```

## leaf-server/minecraft-patches/features/0234-optimise-ChunkGenerator-getMobsAt.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 3 Jun 2025 15:16:32 +0900
Subject: [PATCH] optimise ChunkGenerator#getMobsAt

inline fillStartsForStructure
```

## leaf-server/minecraft-patches/features/0235-cache-biome-for-mob-spawning-and-advancements.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 3 Jun 2025 15:20:40 +0900
Subject: [PATCH] cache biome for mob spawning and advancements
```

## leaf-server/minecraft-patches/features/0236-optimize-mob-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 3 Jun 2025 15:20:59 +0900
Subject: [PATCH] optimize mob spawning

Changes:
- Use K-D Tree for the nearst player lookup
- Use more efficient way for collecting spawning chunks
- Avoid getChunk calls if its position is same as the chunk used for mob spawning
- Don't sync load chunks for mob spawning checks
- Fix data race in async mob spawning by adding chunk position to the mob count map then apply result on server thread.
- Reduce random calls
- Use kd-tree for player search

Generally faster than the non-async approach
iterate over all entities, get their chunk, and increment the count
```

## leaf-server/minecraft-patches/features/0237-optimize-structure-map.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 3 Jun 2025 15:41:12 +0900
Subject: [PATCH] optimize structure map
```

## leaf-server/minecraft-patches/features/0238-throttle-mob-spawning.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Wed, 4 Jun 2025 20:54:03 +0900
Subject: [PATCH] throttle mob spawning
```

## leaf-server/minecraft-patches/features/0239-Add-BlockExplosionHitEvent.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Pascalpex <pascalpex@gmail.com>
Date: Wed, 4 Jun 2025 17:03:32 +0200
Subject: [PATCH] Add BlockExplosionHitEvent
```

## leaf-server/minecraft-patches/features/0240-Old-Blast-Protection-explosion-knockback.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 2 Jun 2025 03:29:20 +0800
Subject: [PATCH] Old Blast Protection explosion knockback
```

## leaf-server/minecraft-patches/features/0241-Use-UUID-for-cure-reputation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Use UUID for cure reputation

Related MC issue: https://bugs.mojang.com/browse/MC/issues/MC-247647
```

## leaf-server/minecraft-patches/features/0242-Fix-crash-during-parsing-unknown-command-message.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 15 Jun 2025 02:20:26 +0800
Subject: [PATCH] Fix crash during parsing unknown command message

Use direct impl for unknown commands message parse to prevent crash
Filter control chars to prevent crash
```

## leaf-server/minecraft-patches/features/0243-optimize-random-tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 6 Jun 2025 20:46:10 +0900
Subject: [PATCH] optimize random tick
```

## leaf-server/minecraft-patches/features/0244-do-not-log-invalid-flatten-text-component-parse.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 6 Jun 2025 05:44:21 +0800
Subject: [PATCH] do not log invalid flatten text component parse
```

## leaf-server/minecraft-patches/features/0245-Fast-BiomeManager-seed-obfuscation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fast BiomeManager seed obfuscation

Pretty tiny optimization, but better than nothing
```

## leaf-server/minecraft-patches/features/0246-Replace-EntitySelectorOptions-map-with-optimized-col.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Replace EntitySelectorOptions map with optimized collection
```

## leaf-server/minecraft-patches/features/0247-optimize-no-action-time.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 4 Jul 2025 03:06:55 +0900
Subject: [PATCH] optimize no action time
```

## leaf-server/minecraft-patches/features/0248-optimize-waypoint.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 4 Jul 2025 03:13:33 +0900
Subject: [PATCH] optimize waypoint
```

## leaf-server/minecraft-patches/features/0249-Paw-optimization.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 24 Apr 2025 16:36:16 -0400
Subject: [PATCH] Paw optimization

Some random optimizations

- Remove Paper's dead code
- Only set shuffle random seed if is really used
- Cache direction values to skip copy
- Secret patches (WIP)
```

## leaf-server/minecraft-patches/features/0250-Skip-inactive-entity-for-execute.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Skip inactive entity for execute

Co-authored-by: <90316914+wling-art@users.noreply.github.com>
```

## leaf-server/minecraft-patches/features/0251-Optimise-getEntities.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 12 Jul 2025 02:00:43 +0200
Subject: [PATCH] Optimise getEntities

Co-authored by: Martijn Muijsers <martijnmuijsers@live.nl>
```

## leaf-server/minecraft-patches/features/0252-Re-route-SetClosestHomeAsWalkTarget-s-poi-finding-to.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 16 Jul 2025 11:13:38 +0800
Subject: [PATCH] Re-route SetClosestHomeAsWalkTarget's poi finding to paper's
 faster logic

Also see findAnyPoiPositions in NearestBedSensor#doTick
```

## leaf-server/minecraft-patches/features/0253-optimize-checkInsideBlocks-calls.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sat, 19 Jul 2025 13:31:49 +0200
Subject: [PATCH] optimize checkInsideBlocks calls

1. Chunk caching - Reduce chunk lookups via local caching
2. Array-based storage - Replace EnumMaps with ordinal-indexed arrays
3. Memory reuse - Cache positions/Vec3/level references
4. Iterator removal - Use indexed loops instead

License: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0254-Optimise-TextColor.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Overwrite987 <artem.korolev_2003@mail.ru>
Date: Fri, 25 Jul 2025 01:04:43 +0300
Subject: [PATCH] Optimise TextColor

The JMH benchmark of this patch can be found in SunBox's JMH `OptimizeTextColorMisc`, `OptimizeTextColorMapGet`
and nanoTime bench `OptimizeTextColor`.
```

## leaf-server/minecraft-patches/features/0255-Do-not-create-fire-if-explosion-was-cancelled.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Overwrite987 <artem.korolev_2003@mail.ru>
Date: Mon, 28 Jul 2025 03:35:42 +0300
Subject: [PATCH] Do not create fire if explosion was cancelled

Related to https://github.com/PaperMC/Paper/issues/12441
This patch is a similar solution as the part of https://github.com/PaperMC/Paper/pull/11840
```

## leaf-server/minecraft-patches/features/0256-Skip-BlockPhysicsEvent-if-no-listeners.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 3 Aug 2025 22:29:34 +0800
Subject: [PATCH] Skip BlockPhysicsEvent if no listeners

BlockPhysicsEvent creates new CraftBlock instance and clones CraftBlockData when constructed it.
It's a defensive programming stratgy and expensive if there are many neighbor updates.
However, there is no plugin listening to this event sometimes, so that just add a check to not
call it if no listeners listening to it.
```

## leaf-server/minecraft-patches/features/0257-Lithium-equipment-tracking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Lithium: equipment tracking

Should have special treatment to ArmorStand, since Paper introduced the configurable
ArmorStand no-tick, and still gives it ability to update equipment changes.
Thus added a bypass condition in LivingEntity#collectEquipmentChanges, always send
ArmorStand equipment changes even if the ArmorStand is no-tick

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/util/in_world_tracking/entity/LivingEntityMixin.java"
* "net/caffeinemc/mods/lithium/mixin/util/in_world_tracking/entity/EntityMixin.java"
* "net/caffeinemc/mods/lithium/mixin/util/item_component_and_count_tracking/PatchedDataComponentMapMixin.java"
* "net/caffeinemc/mods/lithium/mixin/util/item_component_and_count_tracking/ItemStackMixin.java"
* "net/caffeinemc/mods/lithium/mixin/entity/equipment_tracking/enchantment_ticking/LivingEntityMixin.java"
* "net/caffeinemc/mods/lithium/mixin/entity/equipment_tracking/equipment_changes/LivingEntityMixin.java"
* "net/caffeinemc/mods/lithium/mixin/entity/equipment_tracking/EntityEquipmentMixin.java"
* "net/caffeinemc/mods/lithium/common/util/change_tracking/ChangePublisher.java"
* "net/caffeinemc/mods/lithium/common/util/change_tracking/ChangeSubscriber.java"
* "net/caffeinemc/mods/lithium/common/world/in_world_tracking/MaybeInLevelObject.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0258-Add-io_uring-support.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Thu, 7 Aug 2025 00:22:50 +0800
Subject: [PATCH] Add io_uring support
```

## leaf-server/minecraft-patches/features/0259-Paper-PR-Optimise-temptation-lookups.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: okx-code <okx@okx.sh>
Date: Thu, 6 Feb 2025 19:05:00 +0100
Subject: [PATCH] Paper PR: Optimise temptation lookups

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/11942

Both TemptGoal and TemptingSensor recheck their validity each tick.
For this, they iterate all online players, checking their main and
offhand items against item tags.
This logic quickly explodes as each brain/goal selector queries all
players.

This patch attempts to optimise this behaviour by lazily caching the
results of the non-entity specific checks in a single BitSet.
Such cache is valid for a single tick but can be re-used by each tempt
goal or sensor sharing the same non-entity specific selection
predicates.
```

## leaf-server/minecraft-patches/features/0260-Paper-PR-Optimise-temptation-lookups-changes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 15 Aug 2025 16:16:08 +0900
Subject: [PATCH] Paper PR: Optimise temptation lookups changes

Some changes to the original temptation lookups optimization patch
- Replaced some storages from ArrayList to Array to be faster
- Cached mob location access in iteration
- Added tempt goal lookup optimizations for:
    - Purpur's cow mushrooms feed
    - Purpur's villager / wandering trader follow emerald
    - Happy ghast
```

## leaf-server/minecraft-patches/features/0261-Paper-PR-fix-ConcurrentModificationException-in-asyn.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Gero <gecam59@gmail.com>
Date: Wed, 11 Feb 2026 12:54:45 +0100
Subject: [PATCH] Paper PR: fix ConcurrentModificationException in async
 command sending

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/13626

fixes ConcurrentModificationException in async command sending (issue https://github.com/PaperMC/Paper/issues/11101)
the CME was caused by map.put(this.dispatcher.getRoot(), rootCommandNode); iterating the roots children asynchronously for hash code calculation. this is fixed by copying the whole root node.

different approach to https://github.com/PaperMC/Paper/pull/13233 without locking/synchronization
```

## leaf-server/minecraft-patches/features/0262-Paper-PR-Fix-cancelled-rooted-dirt-drops-being-duped.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Sun, 10 Jan 2077 00:00:00 +0800
Subject: [PATCH] Paper PR: Fix cancelled rooted dirt drops being duped

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/13537

This PR fixes Paper#13536, cancelled rooted dirt drops being duped

Analysis

```
protected static final Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap(
        ImmutableMap.of(
            Blocks.GRASS_BLOCK,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT_PATH,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.DIRT,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.FARMLAND.defaultBlockState())),
            Blocks.COARSE_DIRT,
            Pair.of(HoeItem::onlyIfAirAbove, changeIntoState(Blocks.DIRT.defaultBlockState())),
            Blocks.ROOTED_DIRT,
            Pair.of(useOnContext -> true, changeIntoStateAndDropItem(Blocks.DIRT.defaultBlockState(), Items.HANGING_ROOTS))
        )
    );
```

The rooted dirt item drop logic is being processed in HoeItem#useOn, the drop capturing isn't enabled in this path, causing hanging roots to still be dropped even BlockPlaceEvent is cancelled.
This bug has existed since 1.19, after Mojang added the rooted dirt.
```

## leaf-server/minecraft-patches/features/0263-Lithium-combined-heightmap-update.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Wed, 30 Jul 2025 23:18:41 +0200
Subject: [PATCH] Lithium: combined heightmap update

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/world/combined_heightmap_update/LevelChunkMixin.java"
* "net/caffeinemc/mods/lithium/common/world/chunk/heightmap/CombinedHeightmapUpdate.java"

By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0264-Skip-PreCreatureSpawnEvent-if-no-listeners.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Taiyou06 <kaandindar21@gmail.com>
Date: Sun, 17 Aug 2025 14:46:30 +0200
Subject: [PATCH] Skip PreCreatureSpawnEvent if no listeners
```

## leaf-server/minecraft-patches/features/0265-optimize-goal-selector.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 15 Aug 2025 15:02:27 +0900
Subject: [PATCH] optimize goal selector
```

## leaf-server/minecraft-patches/features/0266-thread-unsafe-chunk-map.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 14:52:44 +0900
Subject: [PATCH] thread unsafe chunk map
```

## leaf-server/minecraft-patches/features/0267-optimize-SimpleBitStorage-object-layout.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 14:55:00 +0900
Subject: [PATCH] optimize SimpleBitStorage object layout
```

## leaf-server/minecraft-patches/features/0268-optimize-get-chunk.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 14:55:37 +0900
Subject: [PATCH] optimize get chunk
```

## leaf-server/minecraft-patches/features/0269-remove-shouldTickBlocksAt-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 14:56:22 +0900
Subject: [PATCH] remove shouldTickBlocksAt check
```

## leaf-server/minecraft-patches/features/0270-optimize-PalettedContainer-get.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 14:59:34 +0900
Subject: [PATCH] optimize PalettedContainer#get
```

## leaf-server/minecraft-patches/features/0271-optimize-LevelChunk-getBlockStateFinal.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:00:20 +0900
Subject: [PATCH] optimize LevelChunk#getBlockStateFinal
```

## leaf-server/minecraft-patches/features/0272-optimize-FluidState-is-TagKey.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:00:57 +0900
Subject: [PATCH] optimize FluidState#is(TagKey)
```

## leaf-server/minecraft-patches/features/0273-Pluto-don-t-load-chunks-to-spread-grass.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:39:09 +0900
Subject: [PATCH] Pluto: don't load chunks to spread grass

Original license: GPL-3.0-only
Original project: https://github.com/Yive/Pluto
```

## leaf-server/minecraft-patches/features/0274-counting-chunk-section-fluid.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:12:14 +0900
Subject: [PATCH] counting chunk section fluid

Port `fluidCount` optimization from vanilla 26.1. Uses `fluidCount` name instead of other custom fields,
since plugins like CraftEngine may inject LevelChunkSection.
And it's safe to use `fluidCount` if we assume plugins support multiple versions, and they can easily
support Leaf by just adding a condition.
```

## leaf-server/minecraft-patches/features/0275-optimize-onClimbable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:42:48 +0900
Subject: [PATCH] optimize onClimbable
```

## leaf-server/minecraft-patches/features/0276-optimize-applyMovementEmissionAndPlaySound.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:43:00 +0900
Subject: [PATCH] optimize applyMovementEmissionAndPlaySound
```

## leaf-server/minecraft-patches/features/0277-optimize-isStateClimbable.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:43:06 +0900
Subject: [PATCH] optimize isStateClimbable
```

## leaf-server/minecraft-patches/features/0278-optimize-getOnPos.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:43:17 +0900
Subject: [PATCH] optimize getOnPos
```

## leaf-server/minecraft-patches/features/0279-optimize-eye-block-position.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:43:24 +0900
Subject: [PATCH] optimize eye block position
```

## leaf-server/minecraft-patches/features/0280-optimize-entity-in-fluid.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 9 Aug 2025 15:44:06 +0900
Subject: [PATCH] optimize entity in fluid
```

## leaf-server/minecraft-patches/features/0281-Replace-entity-fluid-height-map.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sun, 10 Aug 2025 16:14:07 +0900
Subject: [PATCH] Replace entity fluid height map
```

## leaf-server/minecraft-patches/features/0282-optimize-collision-shape.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Mon, 11 Aug 2025 02:19:41 +0900
Subject: [PATCH] optimize collision shape
```

## leaf-server/minecraft-patches/features/0283-cache-collision-list.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Mon, 11 Aug 2025 02:19:59 +0900
Subject: [PATCH] cache collision list
```

## leaf-server/minecraft-patches/features/0284-fast-bit-radix-sort.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 28 Aug 2025 17:10:34 +0900
Subject: [PATCH] fast bit radix sort

Co-authored-by: Taiyou06 <kaandindar21@gmail.com>
```

## leaf-server/minecraft-patches/features/0285-optimize-tickEffects.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 30 Aug 2025 20:30:37 +0900
Subject: [PATCH] optimize tickEffects
```

## leaf-server/minecraft-patches/features/0286-Pluto-Expose-Direction-Plane-s-faces.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Yive <6853318+Yive@users.noreply.github.com>
Date: Tue, 6 May 2025 22:14:55 -0700
Subject: [PATCH] Pluto: Expose Direction$Plane's faces

The JMH benchmark of this patch can be found in SunBox's `CachedEnumValuesForLoop`

Original license: GPL-3.0-only
Original project: https://github.com/Yive/Pluto
```

## leaf-server/minecraft-patches/features/0287-Pluto-reduce-allocation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Yive <6853318+Yive@users.noreply.github.com>
Date: Tue, 6 May 2025 10:12:32 -0700
Subject: [PATCH] Pluto: reduce allocation

Original license: GPL-3.0-only
Original project: https://github.com/Yive/Pluto
```

## leaf-server/minecraft-patches/features/0288-Only-update-frozen-ticks-if-changed.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 30 Aug 2025 20:31:35 +0900
Subject: [PATCH] Only update frozen ticks if changed
```

## leaf-server/minecraft-patches/features/0289-Prevent-executing-commands-if-server-stopped.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Mon, 29 Sep 2025 03:28:55 -0400
Subject: [PATCH] Prevent executing commands if server stopped

The server is executing player's commands when server stopped, but the player didn't disconnect.
Since the server is stopped, the commands execute on current thread instead of the main thread.

It's a kind of weird race condition, not sure what causes it,
so just add a `isStopped` check, like the check under `handleChat`
```

## leaf-server/minecraft-patches/features/0290-optimize-canHoldAnyFluid.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 9 Oct 2025 18:32:43 +0900
Subject: [PATCH] optimize canHoldAnyFluid
```

## leaf-server/minecraft-patches/features/0291-Multithreaded-Tracker.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: peaches94 <peachescu94@gmail.com>
Date: Sat, 2 Jul 2022 00:35:56 -0500
Subject: [PATCH] Multithreaded Tracker

Original license: GPL-3.0-only
Original project: https://github.com/Bloom-host/Petal

Original license: GPL-3.0-only
Original project: https://github.com/TECHNOVE/Airplane-Experimental

Co-authored-by: Paul Sauve <paul@technove.co>
Co-authored-by: Kevin Raneri <kevin.raneri@gmail.com>
Co-authored-by: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Co-authored-by: hayanesuru <hayanesuru@outlook.jp>

This patch refactored from original multithreaded tracker (Petal version),
and is derived from the Airplane fork by Paul Sauve, the tree is like:
Airplane -> Pufferfish(?) -> Petal -> Leaf

The core logic has beed reworked compared to the old one, can handle larger
scale situation better now.
Current impl includes many improvements and fixes we made, such as
plugin compat issues with some NPC plugins using real entity type,
e.g. Citizens.

However we still recommend to use those packet based NPC plugins,
e.g. ZNPC Plus, Adyeshach, Fancy NPC, etc.
```

## leaf-server/minecraft-patches/features/0292-optimize-collidedAlongVector.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Tue, 21 Oct 2025 10:45:21 +0900
Subject: [PATCH] optimize collidedAlongVector
```

## leaf-server/minecraft-patches/features/0293-Rewrite-entity-despawn-time.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Rewrite entity despawn time

HOW IT WORKS:
In the default implementation, totalEntityAge only increments when an entity is actively ticked.
Consequently, if an entity resides in a weak-loaded chunk, its aging process effectively pauses, allowing it to exist far beyond the configured despawn time.

This patch addresses this discrepancy by synchronizing entity age with absolute server time:
Introduces lastTickTime to record the MinecraftServer.currentTick when an entity was last ticked.
Once the entity is ticked again or unloaded, the system calculates the elapsed time (currentTick - lastTickTime), and adds it to the totalEntityAge.
Also, it moves the despawn logic of projectiles to their own tick method (see ThrowableProjectile#tick), making it faster to despawn massive stuck projectiles.

Benchmarks (60,000 snowballs):
Before: Around 120 seconds, server thread is struggling to process projectile physics.
After: 1.5 seconds

Brings the ability to despawn weak-loaded entities once they are ticked,
a solution for https://github.com/PaperMC/Paper/issues/12986
```

## leaf-server/minecraft-patches/features/0294-Optimize-Vec3i-hashing.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Optimize Vec3i hashing

The vanilla Vec3i hash formula can produce severe collisions for some
structured BlockPos distributions, especially dense chunk coordinate
sets. Replace it with a mixed coordinate hash that avoids those common linear
collision patterns.
Benchmarks show clear improvements for pathological and dense spatial maps,
while random or small coordinate sets are mostly unchanged. This patch is aimed
at improving worst-case distribution rather than reducing raw hash instruction
count.
```

## leaf-server/minecraft-patches/features/0295-Cache-world-border.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Cache world border

The world border is only initialized once when the level is created.
Lookup from data storage map multiple time is quite unnecessary and expensive.
```

## leaf-server/minecraft-patches/features/0296-Lithium-faster-hash-palette.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sun, 18 Jan 2026 03:17:42 -0500
Subject: [PATCH] Lithium: faster hash palette

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/chunk/palette/StrategyMixin.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0297-Optimize-PatchedDataComponentMap-equals.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Optimize PatchedDataComponentMap equals

The server frequently compares item states via broadcastChanges() during player ticks,
the original equals() implementation directly calls the underlying map's equality check,
which often leads to expensive linear scans when dealing with Reference2ObjectArrayMap.

Optimizations:
1. fail fast by early comparing patch size.
2. Check for reference equality of the patch maps first.
This is highly effective due to the COW mechanism where many unmodified ItemStacks share the same map instance.
```

## leaf-server/minecraft-patches/features/0298-optimize-movement-vector-normalization.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: VeVeVeVel <147647046+VeVeVeVel@users.noreply.github.com>
Date: Mon, 26 Jan 2026 22:24:02 +0900
Subject: [PATCH] optimize movement vector normalization

The previous implementation always calculated the scalar length and normalized the vector, even if the vector's length
was already less than or equal to the maximum threshold `8.0`. Since `vec3.length()` involves a square root operation
(`\sqrt{x}`), performing it unnecessarily can impact performance in high-frequency movement logic.

I have replaced the unconditional normalization with a conditional check based on the squared length ():

- If  (`d > 64.0 (8^2)`): The vector exceeds the maximum distance. We normalize and scale it to .
- If `d <= 64.0`: The vector is already within the  limit. We can use the original `vec3` directly, skipping the
`length()`, `normalize()`, and `scale()` calls entirely.
```

## leaf-server/minecraft-patches/features/0299-Reduce-optimiseRandomTick-new-BlockPos-instance-crea.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MrlingXD <90316914+wling-art@users.noreply.github.com>
Date: Thu, 22 May 2025 02:28:50 +0800
Subject: [PATCH] Reduce optimiseRandomTick new BlockPos instance create
```

## leaf-server/minecraft-patches/features/0300-configurable-goal-update-interval.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Mon, 19 Jan 2026 14:08:30 +0900
Subject: [PATCH] configurable goal update interval

The alternative solution of Pufferfish's Throttle-goal-selector-during-inactive-ticking.patch
```

## leaf-server/minecraft-patches/features/0301-Prevent-entities-from-moving-into-weak-loaded-chunks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Prevent entities from moving into weak loaded chunks

This also should mitigate most cases of memory leaks caused by massive weak loaded projectiles
```

## leaf-server/minecraft-patches/features/0302-Don-t-load-POI-for-competitor-scan.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Don't load POI for competitor scan
```

## leaf-server/minecraft-patches/features/0303-optimize-PathNavigation-followThePath.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Sat, 21 Mar 2026 15:25:23 +0900
Subject: [PATCH] optimize PathNavigation#followThePath

To prevent unnecessary BlockPos allocation in `asBlockPos` under `getNextNodePos`
```

## leaf-server/minecraft-patches/features/0304-reduce-enchantment-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 19 Mar 2026 20:11:58 +0900
Subject: [PATCH] reduce enchantment allocations

reduce allocation of creating entity loot contexts and unmodifiable set boxing
```

## leaf-server/minecraft-patches/features/0305-Reduce-AbstractContainerMenu-allocations.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Reduce AbstractContainerMenu allocations

Use simplified and more efficient supplier.
Must be checked every MC update to make sure the supplier reference lifecycle won't get leaked and be reused in the next loop.
```

## leaf-server/minecraft-patches/features/0306-Optimize-map-lookups-with-isEmpty-check.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Mon, 9 Feb 2026 12:37:26 +0300
Subject: [PATCH] Optimize map lookups with isEmpty check
```

## leaf-server/minecraft-patches/features/0307-Filter-ClientboundSetEntityMotionPacket.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dueris <jedimastertoothless@hotmail.com>
Date: Sat, 16 Aug 2025 20:29:29 -0700
Subject: [PATCH] Filter ClientboundSetEntityMotionPacket

Original license: GPL-3.0-only
Original project: https://github.com/CraftCanvasMC/Canvas

Entities with a 360-degree rotation and movement, and item and ender eye entities
get royally screwed over with this change, as it removes the synchronization
of the entity delta movement.

This packet, when disabled, can reduce bandwidth by 60%, which is insane, but we do
need to add a rough filter that ensures there is no visual impact on the client

The only entities needing this are:
1. Squid, only mobs with 360-degree range of rotation
2. Item Entities, disabling this causes glitches and often slightly desyncs their position within 0.5 blocks on the client side. But we filter it if
   it hasn't moved, since for some reason it sends those packets anyway???
3. Ender Eyes, they depend on their velocity being set bc the packet sets the velocity on the client, but the client doesn't handle the
     ender eye velocity on its own bc the ender eye velocity is in relation to the stronghold structure which the client doesn't have access to
4. Shulker bullets use random to determine where they are going, so we need to send velocity packets to ensure it remains smooth on the client

This filter helps keep unneeded packets reduced, but also ensures that we keep smooth visual gameplay
```

## leaf-server/minecraft-patches/features/0308-Disable-Useless-Particles.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dueris <jedimastertoothless@hotmail.com>
Date: Tue, 19 Aug 2025 16:09:26 -0700
Subject: [PATCH] Disable Useless Particles

Original license: GPL-3.0-only
Original project: https://github.com/CraftCanvasMC/Canvas

It is recommended to enable these options, as the client displays most of these particles already, so the server-side particle logic is not needed
```

## leaf-server/minecraft-patches/features/0309-Optimize-pushable-selector.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Sat, 28 Feb 2026 14:17:02 +0900
Subject: [PATCH] Optimize pushable selector

Avoid duplicated Bukkit#isPushable check, already checked inside the Bukkit#canCollideWithBukkit
```

## leaf-server/minecraft-patches/features/0310-Optimize-respawn-anchor-explosion.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Tue, 31 Mar 2026 05:23:16 -0400
Subject: [PATCH] Optimize respawn anchor explosion

- Remove stream
- Use faster method to get fluid state
(Since we can assume the chunk is loaded when a respawn anchor exploded.)
```

## leaf-server/minecraft-patches/features/0311-Skip-VehicleEntityCollisionEvent-if-no-listeners.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Tue, 31 Mar 2026 09:30:12 +0300
Subject: [PATCH] Skip VehicleEntityCollisionEvent if no listeners
```

## leaf-server/minecraft-patches/features/0312-Cache-identifier-toString-and-hash.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: OverwriteMC <artyom030803@gmail.com>
Date: Thu, 2 Apr 2026 20:10:28 +0300
Subject: [PATCH] Cache identifier toString and hash

Cache toString and hash to reduce allocations.
Use lazy cache here to prevent cache inconsistency caused by
operations that don't use the constructor, e.g., Gson serialization or Reflection.
```

## leaf-server/minecraft-patches/features/0313-Fix-MC-301114-Combat-Tracker-memory-leak.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Fix MC-301114 Combat Tracker memory leak

Rewritten using EvictingRingList instead of ArrayDeque.
Superseded Paper PR: https://github.com/PaperMC/Paper/pull/13455
```

## leaf-server/minecraft-patches/features/0314-Paper-Lazy-set-Entity.projectileSource-in-AbstractPr.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Owen1212055 <23108066+Owen1212055@users.noreply.github.com>
Date: Sat, 4 Apr 2026 20:03:09 -0400
Subject: [PATCH] Paper: Lazy set Entity.projectileSource in
 AbstractProjectile#getShooter

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/5e9a9188c04668e8f6b9ebda1060f295ca9b4baa

Backported from Paper 26.1.1

Fixes Paper#13475
Supersedes Paper#13643
Supersedes Paper#13481
```

## leaf-server/minecraft-patches/features/0315-Paper-Add-EntityLungeEvent.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dueris <122416109+Dueris@users.noreply.github.com>
Date: Sat, 11 Apr 2026 06:25:42 -0700
Subject: [PATCH] Paper: Add EntityLungeEvent

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/67a54b3117aaa7e2cd81c6f777f5b1bccf4a98fc

Backported from Paper 26.1.1

Feature implementation for Paper#13634
```

## leaf-server/minecraft-patches/features/0316-Paper-reset-pending-keepalive-state-on-listener-hand.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Harold <55301634+ZECHEESELORD@users.noreply.github.com>
Date: Sat, 25 Apr 2026 12:50:10 -0400
Subject: [PATCH] Paper: reset pending keepalive state on listener handoff
 during reconfiguration

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/c39822ae08040423b7e0e0d66ca1666fee0ae0c4

Backported from Paper 26.1.2

Fixes PaperMC/Velocity#1723. (The backend side)

During mid session reconfiguration behind Velocity, Paper was carrying the same KeepAlive tracker across listener handoff. That allowed a pending PLAY keepalive expectation to survive into the CONFIG listener.

ServerCommonPacketListenerImpl#createCookie(...) now gives the next listener a fresh KeepAlive instance instead of reusing the current listener's pending tracker. This preserves latency/history state, but intentionally resets inflight keepalive challenge state across listener handoff. In other words:

- preserve ping history
- reset pending keepalive expectations

That restores the expected protocol boundary between PLAY and CONFIG.
```

## leaf-server/minecraft-patches/features/0317-Paper-Fix-clicked-items-disappearing-when-player-die.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Alejandro Nieto <contact@axyss.dev>
Date: Mon, 4 May 2026 08:23:40 +0200
Subject: [PATCH] Paper: Fix clicked items disappearing when player dies

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/b4682bfef616ac62e73cc96046dacdf4a6f53eeb

Backported from Paper 26.1.2

Resolves Paper#12928

This issue was introduced on 1.21.2 when Mojang refactored AbstractContainerMenu#removed() and moved some of the logic to AbstractContainerMenu.dropOrPlaceInInventory().
When they did that, they inverted the logic of the flag that checked wether the player was dead or not and stopped using !player.isAlive() in favor of player.isRemoved().

On vanilla this works fine because by the time AbstractContainerMenu.dropOrPlaceInInventory() is executed the player is already removed, but this is not the case in paper.
```

## leaf-server/minecraft-patches/features/0318-Paper-Add-options-to-disable-chunk-and-block-entity-.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Nassim Jahnke <nassim@njahnke.dev>
Date: Sun, 26 Apr 2026 17:09:05 +0200
Subject: [PATCH] Paper: Add options to disable chunk and block entity ticking

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/acfe105b9dd64f4b3179ce2575e3809568974a20

Backported from Paper 26.1.2
```

## leaf-server/minecraft-patches/features/0319-Paper-optimize-ServerWaypointManager-with-locator-ba.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Owen1212055 <23108066+Owen1212055@users.noreply.github.com>
Date: Sat, 4 Apr 2026 22:14:19 -0400
Subject: [PATCH] Paper: optimize ServerWaypointManager with locator bar
 disabled

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/7e34fba07666e93a4dd066d97b6373a18eb0f570

Backported from Paper 26.1.1

The waypoint system iterates over each player for running waypoint calculations. This, most notably, is done on setPosRaw
which is executed twice for each player (one for non players).
This results in every time a player moves, it iterates over each player twice.

So instead, we cache the game rule and instead use this to prevent iterating over all the players and avoid the redundant
connection logic checks.

This performance issue is most noticeable with many players on the same level.
```

## leaf-server/minecraft-patches/features/0320-Paper-Limit-projectiles-size.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Nassim Jahnke <nassim@njahnke.dev>
Date: Wed, 3 Jun 2026 19:16:54 +0200
Subject: [PATCH] Paper: Limit projectiles size

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/e2c7e5cff2d81190d15b85fb3713ededef09ec21

Backported from Paper 26.1.2
```

## leaf-server/minecraft-patches/features/0321-Improve-Purpur-JS-expression-evaluation.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Wed, 15 Apr 2026 23:58:48 -0400
Subject: [PATCH] Improve Purpur JS expression evaluation

Fallback to vanilla logic if the related Purpur config value is default, to skip unnecessary JS evaluation.
And use fastutil's map to prevent the cost of un/boxing.

Actually, we can also replace the Rhino with GraalJS to achieve better performance. However, I don't think
there are many people who will use this Purpur feature, we can do it if really needed.
```

## leaf-server/minecraft-patches/features/0322-configurable-ice-and-snow-tick-chance.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Thu, 22 Jan 2026 01:21:43 +0900
Subject: [PATCH] configurable ice and snow tick chance
```

## leaf-server/minecraft-patches/features/0323-fix-skeleton-horse-trap-NPE.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: hayanesuru <hayanesuru@outlook.jp>
Date: Fri, 23 Jan 2026 18:47:34 +0900
Subject: [PATCH] fix skeleton horse trap NPE

Prevent removing the goal during the goals iteration.
```

## leaf-server/minecraft-patches/features/0324-Skip-item-merge-checks-for-full-stacks.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: nostalfinals <yuu8583@proton.me>
Date: Thu, 16 Apr 2026 16:46:39 +0800
Subject: [PATCH] Skip item merge checks for full stacks
```

## leaf-server/minecraft-patches/features/0325-disable-world-data-saving.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Manere <133597204+Manered@users.noreply.github.com>
Date: Sat, 25 Apr 2026 22:20:28 +0200
Subject: [PATCH] disable world data saving

Skip persistence for worlds in misc.disable-world-data-saving.worlds (leaf-global.yml):
ServerLevel full and incremental saves, and Moonrise chunk/entity/POI writes (including unload).
Shutdown closes the chunk system without flushing saves.
```

## leaf-server/minecraft-patches/features/0326-Replace-entity-equipment-items-to-array.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Fri, 24 Apr 2026 02:41:08 -0400
Subject: [PATCH] Replace entity equipment items to array
```

## leaf-server/minecraft-patches/features/0327-Leaves-Lithium-Sleeping-Block-Entity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: MC_XiaoHei <xor7xiaohei@gmail.com>
Date: Thu, 31 Jul 2025 09:37:26 +0800
Subject: [PATCH] Leaves: Lithium Sleeping Block Entity

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This patch is Powered by Lithium (https://github.com/CaffeineMC/lithium)

This patch is based on the following mixins:
* "net/caffeinemc/mods/lithium/mixin/world/block_entity_ticking/sleeping/*.java"
* "net/caffeinemc/mods/lithium/mixin/block/hopper/*.java"
* "net/caffeinemc/mods/lithium/mixin/util/block_entity_retrieval/LevelMixin.java"
* "net/caffeinemc/mods/lithium/mixin/util/inventory_change_listening/*.java"
* "net/caffeinemc/mods/lithium/mixin/util/inventory_comparator_tracking/*.java"
* "net/caffeinemc/mods/lithium/mixin/util/item_component_and_count_tracking/*.java"
* "net/caffeinemc/mods/lithium/api/inventtory/*.java"
* "net/caffeinemc/mods/lithium/common/block/entity/*.java"
* "net/caffeinemc/mods/lithium/common/hopper/*.java"
* "net/caffeinemc/mods/lithium/common/tracking/entity/*.java"
* "net/caffeinemc/mods/lithium/common/util/change_tracking/ChangePublisher.java"
* "net/caffeinemc/mods/lithium/common/util/change_tracking/ChangeSubscriber.java"
* "net/caffeinemc/mods/lithium/common/util/tuples/WorldSectionBox.java"
* "net/caffeinemc/mods/lithium/common/util/DirectionConstants.java"
* "net/caffeinemc/mods/lithium/common/world/WorldHelper.java"
By: 2No2Name <2No2Name@web.de>
As part of: Lithium (https://github.com/CaffeineMC/lithium)
Licensed under: LGPL-3.0-only (https://www.gnu.org/licenses/lgpl-3.0.html)
```

## leaf-server/minecraft-patches/features/0328-fixup-Leaves-Lithium-Sleeping-Block-Entity.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Dreeam <61569423+Dreeam-qwq@users.noreply.github.com>
Date: Sat, 2 May 2026 08:31:52 -0400
Subject: [PATCH] fixup: Leaves: Lithium Sleeping Block Entity

This is a temp patch for tracking new changes added in Lithium that Leaves haven't updated yet.
This patch will merge into Leaves once they finish the 26.x update, and sync back to Leaf,
(merge into original patch).

Last checked Lithium commit: f081ee429879b7c395c22843a41d5ee5b4379ede

Commits/changes synced from Lithium:
https://github.com/CaffeineMC/lithium/commit/d2f48f22cb3890e81bcb49ba61dc625685b28c86
https://github.com/CaffeineMC/lithium/commit/9d29463e79e821ab3d80ade274312caab0a6d397
https://github.com/CaffeineMC/lithium/commit/2c2da310d5e21fe23a8886030503c68d8adfae4e
https://github.com/CaffeineMC/lithium/commit/d2f48f22cb3890e81bcb49ba61dc625685b28c86
https://github.com/CaffeineMC/lithium/commit/d2f48f22cb3890e81bcb49ba61dc625685b28c86
https://github.com/CaffeineMC/lithium/commit/26985fca69279a3bb735d8f0b21f7569906b0870
https://github.com/CaffeineMC/lithium/commit/f1a897deff1ac25884ea66923f9c8dbdbd25ee54
https://github.com/CaffeineMC/lithium/commit/66183a05f63362c40633bae3161d27c5a8af3ba6
https://github.com/CaffeineMC/lithium/commit/59b8dcb226a78c04964aec69cbb3d745ed0515a7
https://github.com/CaffeineMC/lithium/commit/990618bb6d1faebf6fc5f8c43105d299af857d13
```

## leaf-server/minecraft-patches/features/0329-Leaves-Vanilla-hopper.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: violetc <58360096+s-yh-china@users.noreply.github.com>
Date: Fri, 26 Jan 2024 01:36:34 +0800
Subject: [PATCH] Leaves: Vanilla hopper

Original license: GPL-3.0-only
Original project: https://github.com/LeavesMC/Leaves

This is a temporary solution designed to attempt to restore the vanilla behavior of the funnel while preserving optimizations as much as possible. It should ultimately be replaced by the optimization solution provided by lithium.
```

## leaf-server/minecraft-patches/features/0330-Add-read-only-mode-for-Linear-v2.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Add read-only mode for Linear v2
```

## leaf-server/minecraft-patches/features/0331-Reduce-debug-subscribers-overhead.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Reduce debug subscribers overhead
```

## leaf-server/minecraft-patches/features/0332-Limit-max-size-of-subscription-codec.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: HaHaWTH <102713261+HaHaWTH@users.noreply.github.com>
Date: Tue, 9 Nov 2077 00:00:00 +0800
Subject: [PATCH] Limit max size of subscription codec
```

## leaf-server/minecraft-patches/features/0333-Pre-authenticate-event.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Lumine1909 <133463833+Lumine1909@users.noreply.github.com>
Date: Thu, 21 May 2026 14:05:19 +0800
Subject: [PATCH] Pre authenticate event
```

## leaf-server/minecraft-patches/features/0334-Move-ClientboundCommandsPacket-creation-off-the-main.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: purpurcof <purpurcof@users.noreply.github.com>
Date: Mon, 27 Jul 2026 12:00:00 +0300
Subject: [PATCH] Move ClientboundCommandsPacket creation off the main thread

Build the packet on the async command sending pool to avoid
enumerateNodes()/createEntries() lag on login with many plugin commands.
```

## leaf-server/minecraft-patches/features/0335-Paper-Disconnect-State-Fixes.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Owen <23108066+Owen1212055@users.noreply.github.com>
Date: Sat, 25 Apr 2026 18:06:21 -0400
Subject: [PATCH] Paper: Disconnect State Fixes

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper commit: https://github.com/PaperMC/Paper/commit/8021488ef48a4aae5246cd3d2f97b8573bffdad9

Backported from Paper 26.1.2
```

## leaf-server/minecraft-patches/features/0336-Use-legacy-tracker-ticking.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: Alexandra-Myers <bia416cat@gmail.com>
Date: Tue, 23 Sep 2025 21:10:20 -0400
Subject: [PATCH] Use legacy tracker ticking

Original license: GPL-3.0-only
Original project: https://github.com/PaperMC/Paper
Paper pull request: https://github.com/PaperMC/Paper/pull/13094

Related MC issue: https://mojira.dev/MC-297196
```

## leaf-server/minecraft-patches/features/0337-Limit-explosion-packets-per-player-per-tick.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: s0nd3r-py <285719686+s0nd3r-py@users.noreply.github.com>
Date: Sun, 8 Nov 2026 14:04:54 +0200
Subject: [PATCH] Limit explosion packets per player per tick

Every ClientboundExplodePacket makes the client play a sound and spawn an
explosion emitter plus debris particles. TNT chain reactions send hundreds
of these per tick to every nearby player.

When network.explosion-packet-limit.enabled is true (default false), only
the first N explosion packets per player per tick are sent. For the rest
the knockback is summed and delivered in one packet at the end of the
level tick. The client applies explosion knockback with addDeltaMovement,
so the sum gives the same player motion. Damage, block destruction and
entity knockback are server side and unaffected.
```

## leaf-server/minecraft-patches/features/0338-Avoid-software-Math.fma-fallback-in-explosion-exposu.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: s0nd3r-py <285719686+s0nd3r-py@users.noreply.github.com>
Date: Sun, 8 Nov 2026 14:08:53 +0200
Subject: [PATCH] Avoid software Math.fma fallback in explosion exposure rays

Paper's getSeenFraction interpolates ray origins with Math.fma. On CPUs
without hardware FMA (pre-Haswell/Piledriver, and VPS whose virtual CPU
model hides FMA, e.g. qemu64/kvm64) the JDK computes Math.fma exactly with
BigDecimal. A profile of a TNT chain reaction on such a CPU spent 26% of
main-thread time there.

WaterMath.fma uses Math.fma when HotSpot reports UseFMA (hardware
instruction) and a * b + c otherwise. The result differs by at most one
ulp; vanilla computes these interpolations without fma anyway.
```

## leaf-server/minecraft-patches/features/0339-Reduce-TNT-fuse-metadata-packets.patch

```text
From 0000000000000000000000000000000000000000 Mon Sep 17 00:00:00 2001
From: s0nd3r-py <285719686+s0nd3r-py@users.noreply.github.com>
Date: Sun, 8 Nov 2026 14:50:09 +0200
Subject: [PATCH] Reduce TNT fuse metadata packets

PrimedTnt sets its fuse every tick, marking entity data dirty, so the
server sends one ClientboundSetEntityDataPacket per TNT per tracking player
per tick. The client ticks primed TNT with the same code and counts the
fuse down locally, so these packets repeat values the client already has.

With performance.reduce-packets.tnt-fuse.enabled (default false) the fuse
is updated without marking it dirty, except every resync-interval ticks
and whenever something else (a plugin) changes it. The server-side value,
explosion timing and API results are unchanged.
```

## DivineMC (BX-Team) — перенесённые изменения

Источник: https://github.com/BX-Team/DivineMC, лицензия GPL-3.0. Код адаптирован под Minecraft 1.21.11 и структуру WaterMc, а не скопирован как патч.

| Изменение | Оригинал | Где в WaterMc | По умолчанию |
|---|---|---|---|
| Async portal pre-search | ветка `ver/26.3`, `org.bxteam.divinemc.async.world.PortalSearchPrefetcher` и вызов в `Entity.setAsInsidePortal` (BX-Team / DivineMC) | `me.s0nd3r.watermc.world.PortalSearchPrefetcher`, `async.portal-search-prefetch.enabled` | выключено |
| Command block parse results caching | `ver/1.21.11`, `0038-Command-block-parse-results-caching.patch` | `BaseCommandBlock.waterPerformCached`, `performance.command-block-parse-cache.enabled`. Переписано: в кэше хранится разобранное дерево, источник команды подставляется заново при каждом запуске (в оригинале повторно использовался уже закрытый источник), кэш сбрасывается при смене диспетчера или узла команды | выключено |
| Async locate command | `ver/1.21.11`, `0010-Async-locate-command.patch` | `LocateCommand.waterRunLocate`, `async.async-locate.enabled`. Только структуры и биомы, только игрок или консоль без `execute store`; `/locate poi` остаётся синхронным, ответ отправляется из основного потока | выключено |
| Disable leaf decay | `ver/1.21.11`, `0015-Disable-leaf-decay.patch` | `LeavesBlock.decaying`, `gameplay-mechanisms.disable-leaf-decay` (расстояния листвы обновляются как в ванилле) | выключено |
| Crafter 1gt delay | `ver/1.21.11`, `0055-Crafter-1gt-delay.patch` | `CrafterBlock`, `gameplay-mechanisms.crafter-1gt-delay` (свой переключатель; в оригинале проверялся флаг медной лампы) | выключено |

## Leaf (Winds-Studio) — изменения из веток новее нашей базы

Источник: https://github.com/Winds-Studio/Leaf, GPL-3.0-only. Для патчей с дополнительным условием GPLv3 §7(b) указаны авторы, как оно требует.

| Изменение | Ветка | Автор оригинала |
|---|---|---|
| Paper: Fix tree capture state leak | `ver/1.21.11` 0337 | Dreeam (Paper PR 14001) |
| Optimize PoiAccess visited section tracking | `ver/1.21.11` 0339 | Original patch by HaHaWTH, co-authored by MrlingXD (WaterMc: откат на хэш-множество для огромных областей) |
| Short-circuit isOnFire checks, Skip firing GenericGameEvent if no listeners, reduce computeSpeed Vec3 allocations, optimize PathNavigation#shouldRecomputePath | `ver/26.3` | Leaf |
| Cache XP orb merge parameters | `ver/26.3` | Original patch by HaHaWTH |
| Leaves Fork cache item max stack size | `ver/26.3` | Lumine1909/Leaves_Fork (WaterMc: пустой стак сохраняет ванильный ответ 1) |
| Lithium: Cache retracting piston collision shapes | `ver/26.3` | CaffeineMC Lithium (через Leaf) |
| reduce PalettedContainer lookup in collisions | `ver/26.3` | Original patch by HaHaWTH |
| Optimize chunk packet construction, optimize ClientboundCommandsPacket#enumerateNodes | `ver/26.3` | Leaf |
| propagate block destruction cancellation | `ver/1.21.11` 0338 | Original patch by HaHaWTH (Paper issue 13586; всегда включено: учитывает отмену `BlockDestroyEvent` плагинами) |
| Lithium: Reduce Brain memories lookup | `ver/26.3` 0308 | Original patch by HaHaWTH; Lithium (2No2Name, LGPL-3.0). WaterMc: адаптировано под карту памяти 1.21.11, кэш привязан к конкретному Brain |
| optimize entity activation | `ver/26.3` 0297 | hayanesuru. WaterMc: `performance.optimize-entity-activation.enabled`, выключено (зона активации — круг вместо квадрата) |
| Lithium: optimize non-POI block searches | `ver/26.3` 0304 | Original patch by HaHaWTH; Lithium (jcw780, 2No2Name, LGPL-3.0). WaterMc: исправлен сдвиг найденной позиции в ветке с незагруженными чанками |

## Встроенные сторонние компоненты

| Компонент | Лицензия | Где в WaterMc | По умолчанию |
|---|---|---|---|
| Raytrace Entity Culling (DivineMC `ver/26.3`, BX-Team; серверный порт EntityCulling от tr7zw) | GPL-3.0 | `me.s0nd3r.watermc.culling`, `network.raytrace-entity-culling.enabled` | выключено |
| OcclusionCulling (LogisticsCraft) | MIT | `com.logisticscraft.occlusionculling` (исходники без изменений) | — |
| Parallel sensor phase (идея DivineMC, BX-Team) | GPL-3.0 | `me.s0nd3r.watermc.ai.ParallelSensors`, `async.parallel-sensor-phase.enabled`; таймеры сенсоров переписаны (ванильный счёт, совместимо с DAB/EAR) | выключено |
| ViaVersion 5.12.2, ViaBackwards 5.12.2 (ViaVersion team) | GPL-3.0 | jar-файлы внутри ядра `watermc/bundled-plugins/`, загрузка через механизм `--add-plugin`, `network.multi-version.enabled` | выключено |

## Идеи из модов (переписано заново)

| Изменение | Источник | Лицензия | Где в WaterMc | По умолчанию |
|---|---|---|---|---|
| Ранний отсев чанков для крепостей | ModernFix `cache_strongholds` (embeddedt) | LGPL-3.0 | `me.s0nd3r.watermc.world.StrongholdRingBounds`, `performance.stronghold-early-reject.enabled` | включено (результат тот же) |
| Приоритеты потоков чанков и I/O | threadtweak (getchoo) | MIT | `MoonriseCommon`, `async.thread-priorities.enabled` | выключено |
| Лимит размножения | ServerCore `breeding_cap` (Wesley1808) | MIT | `gameplay-mechanisms.breeding-cap.enabled` | выключено |
| Запись UTF-8 строк в пакет без временного буфера | Krypton `Utf8StringMixin` (Andrew Steinborn) | LGPL-3.0 | `net.minecraft.network.Utf8String.write` (байты те же) | всегда |
| Общая пустая карта для опустевшего патча компонентов | FerriteCore `dataComponentPatch` (malte0811) | MIT | `PatchedDataComponentMap.waterReleaseIfEmpty` | всегда |
| Динамическая дальность прорисовки и симуляции по MSPT | ServerCore `dynamic` (Wesley1808) | MIT | `me.s0nd3r.watermc.world.DynamicDistance`, `performance.dynamic-distance.enabled` | выключено |
