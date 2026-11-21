import type { L } from "@/lib/i18n"

export const REPO = "https://github.com/s0nd3r-py/WaterMc"
export const DOWNLOAD = `${REPO}/releases/latest/download/watermc-paperclip-1.21.11.jar`
export const RUN_COMMAND = "java -Xms4G -Xmx4G -jar watermc-paperclip-1.21.11.jar --nogui"

export const hero = {
  lead: {
    ru: "Быстрое серверное ядро на базе Leaf для Minecraft 1.21.11. Быстрее генерация мира, быстрее запуск, плагины Paper работают без правок.",
    en: "A fast Leaf-based server core for Minecraft 1.21.11. Faster world generation, faster startup, Paper plugins work unchanged.",
  },
  download: { ru: "Скачать 1.21.11", en: "Download 1.21.11" },
}

export type Feature = { icon: string; title: L; text: L }

export const features: Feature[] = [
  {
    icon: "map",
    title: { ru: "Быстрая генерация мира", en: "Fast world generation" },
    text: {
      ru: "Больше потоков генерации чанков, плоский поиск биома и быстрый выбор точки спавна на новом мире.",
      en: "More chunk worker threads, flat biome search and fast spawn point selection on new worlds.",
    },
  },
  {
    icon: "zap",
    title: { ru: "Быстрый запуск", en: "Fast startup" },
    text: {
      ru: "Свой загрузчик, ленивый DataFixerUpper и маппинги, которые грузятся только когда они нужны плагину.",
      en: "Own launcher, lazy DataFixerUpper and mappings that load only when a plugin needs them.",
    },
  },
  {
    icon: "users",
    title: { ru: "Толпы и фермы", en: "Crowds and farms" },
    text: {
      ru: "Дешёвое толкание мобов, кэш памяти мозга и поиск блоков из Lithium, лимит размножения.",
      en: "Cheap mob pushing, Lithium brain-memory cache and block searches, breeding cap.",
    },
  },
  {
    icon: "globe",
    title: { ru: "Встроенный ViaVersion", en: "Built-in ViaVersion" },
    text: {
      ru: "ViaVersion и ViaBackwards внутри ядра одной настройкой. Свой экземпляр в plugins/ всегда главнее.",
      en: "ViaVersion and ViaBackwards inside the core behind one switch. Your own copy in plugins/ always wins.",
    },
  },
  {
    icon: "eye-off",
    title: { ru: "Скрытие за стенами", en: "Entity culling" },
    text: {
      ru: "Сервер не отправляет сущности, которых игрок точно не видит. Меньше трафика, ESP-читы слепнут.",
      en: "The server skips entities a player provably cannot see. Less traffic, ESP cheats go blind.",
    },
  },
  {
    icon: "folder",
    title: { ru: "Порядок в папке", en: "Clean server folder" },
    text: {
      ru: "config/, data/, worlds/, logs/ вместо тридцати файлов в корне. Перенос при первом запуске сам.",
      en: "config/, data/, worlds/, logs/ instead of thirty files in the root, migrated on first start.",
    },
  },
  {
    icon: "shield",
    title: { ru: "Аудит и watchdog", en: "Audit and watchdogs" },
    text: {
      ru: "Лог называет плагин за каждой выдачей OP и командой от консоли. Watchdog плагинов и Netty.",
      en: "The log names the plugin behind every OP grant and console command. Plugin and Netty watchdogs.",
    },
  },
  {
    icon: "scroll",
    title: { ru: "WaterScript", en: "WaterScript" },
    text: {
      ru: "Встроенные скрипты: события, команды с правами, таймеры и переменные без отдельного плагина.",
      en: "Built-in scripting: events, permission-checked commands, timers and variables without a plugin.",
    },
  },
]

export type Item = { name: string; text: L }

export const inside: { id: string; label: L; items: Item[] }[] = [
  {
    id: "startup",
    label: { ru: "Запуск и плагины", en: "Startup and plugins" },
    items: [
      {
        name: "Launcher",
        text: {
          ru: "Свой загрузчик помнит размер, время изменения и SHA-256 каждой библиотеки в libraries/.watermc-launcher и не пересчитывает их на каждом старте.",
          en: "The own launcher remembers size, modification time and SHA-256 of every library in libraries/.watermc-launcher and skips unchanged files.",
        },
      },
      {
        name: ".water-remapped",
        text: {
          ru: "Кэш ремаппера плагинов: хэши jar в hashes.tsv, индекс пишется только при изменениях, плагины только на API не переотображаются.",
          en: "Plugin remapper cache: jar hashes in hashes.tsv, the index is rewritten only on changes, API-only plugins are not remapped.",
        },
      },
      {
        name: "Lazy mappings",
        text: {
          ru: "Таблицы Spigot↔Mojang (десятки МБ) читаются только когда они нужны первому плагину.",
          en: "Spigot↔Mojang tables (tens of MB) are read only when the first plugin needs them.",
        },
      },
      {
        name: "Lazy DataFixerUpper",
        text: {
          ru: "Схемы конвертации строятся только когда сервер встречает данные старой версии Minecraft.",
          en: "Conversion schemas are built only when the server meets data from an older Minecraft version.",
        },
      },
    ],
  },
  {
    id: "classes",
    label: { ru: "Переписанные классы", en: "Rewritten classes" },
    items: [
      {
        name: "Climate.RTree",
        text: {
          ru: "Границы узлов в одном плоском long[14] с развёрнутым циклом, результат совпадает с ванилью до бита.",
          en: "Node bounds in one flat long[14] with an unrolled loop; results match vanilla bit for bit.",
        },
      },
      {
        name: "MoonriseCommon",
        text: {
          ru: "Новая формула потоков чанков (4 ядра → 2, 8 → 5, 16 → 9), на старте генерируют все ядра.",
          en: "New chunk worker formula (4 cores → 2, 8 → 5, 16 → 9); all cores generate during startup.",
        },
      },
      {
        name: "ChunkEntitySlices",
        text: {
          ru: "Плоская копия габаритов double[], поиск соседей не трогает объекты сущностей.",
          en: "A flat double[] copy of bounding boxes; neighbour searches skip entity objects.",
        },
      },
      {
        name: "LevelTicks",
        text: {
          ru: "Запланированные тики помнят ближайший срок и не обходят контейнеры до него.",
          en: "Scheduled ticks remember the nearest due time and skip container scans until then.",
        },
      },
      {
        name: "LivingEntity / Entity",
        text: {
          ru: "Здоровье из обычного поля, getTeam() у мобов без команды отвечает сразу, толкание останавливается, когда соседей достаточно.",
          en: "Health from a plain field, getTeam() answers at once for mobs without a team, pushing stops once enough neighbours are found.",
        },
      },
      {
        name: "data/*.wdb",
        text: {
          ru: "OP, белый список, баны и кэш игроков в журнале с дозаписью и CRC32C вместо JSON.",
          en: "OPs, whitelist, bans and user cache in an append-only journal with CRC32C instead of JSON.",
        },
      },
    ],
  },
  {
    id: "libraries",
    label: { ru: "Библиотеки", en: "Libraries" },
    items: [
      {
        name: "Caffeine 3.2.3",
        text: {
          ru: "Все кэши Guava заменены: профили и скины, кэш кодирования NBT, формы блоков, шаблоны блоков.",
          en: "Every Guava cache replaced: profiles and skins, NBT encoding cache, block shapes, block patterns.",
        },
      },
      {
        name: "DateTimeFormatter",
        text: {
          ru: "Вместо SimpleDateFormat: общий форматтер в PlayerList дёргали из разных потоков.",
          en: "Replaces SimpleDateFormat: one shared formatter in PlayerList was used from several threads.",
        },
      },
      {
        name: "Netty 4.2.15 · fastutil 8.5.15",
        text: {
          ru: "Свежие версии: Log4j 2.25.3, ASM 9.9.1, LMAX Disruptor 4.0.0, zstd-jni.",
          en: "Up to date: Log4j 2.25.3, ASM 9.9.1, LMAX Disruptor 4.0.0, zstd-jni.",
        },
      },
      {
        name: "libdeflate",
        text: {
          ru: "Регионы сжимает нативный libdeflate (натив Velocity), по желанию регионы в ZSTD.",
          en: "Regions compressed by native libdeflate (Velocity natives), optional ZSTD regions.",
        },
      },
    ],
  },
]

export const sources: { name: string; url: string; text: L }[] = [
  {
    name: "Lithium",
    url: "https://github.com/CaffeineMC/lithium",
    text: {
      ru: "Кэш условий поведения по изменениям памяти мозга, поиск блоков без POI, формы поршней.",
      en: "Behavior conditions cached by brain memory changes, non-POI block searches, piston shapes.",
    },
  },
  {
    name: "DivineMC",
    url: "https://github.com/BX-Team/DivineMC",
    text: {
      ru: "Скрытие сущностей за стенами, параллельные сенсоры, предзагрузка порталов, асинхронный /locate.",
      en: "Entity culling, parallel sensors, portal prefetch, async /locate.",
    },
  },
  {
    name: "ModernFix",
    url: "https://github.com/embeddedt/ModernFix",
    text: {
      ru: "Ранний отсев чанков, где крепость встать не может.",
      en: "Early reject of chunks that can never hold a stronghold.",
    },
  },
  {
    name: "ServerCore",
    url: "https://github.com/Wesley1808/ServerCore",
    text: { ru: "Лимит размножения, динамическая дальность по MSPT.", en: "Breeding cap, dynamic view distance by MSPT." },
  },
  {
    name: "Krypton",
    url: "https://github.com/astei/krypton",
    text: { ru: "Строки пишутся прямо в буфер пакета.", en: "Strings encoded straight into the packet buffer." },
  },
  {
    name: "FerriteCore",
    url: "https://github.com/malte0811/FerriteCore",
    text: { ru: "Общая пустая карта для компонентов предметов.", en: "Shared empty map for item components." },
  },
  {
    name: "threadtweak",
    url: "https://github.com/getchoo/threadtweak",
    text: { ru: "Приоритеты потоков чанков и I/O.", en: "Chunk and I/O thread priorities." },
  },
  {
    name: "Leaf 26.x",
    url: "https://github.com/Winds-Studio/Leaf",
    text: {
      ru: "Активация сущностей через KD-дерево, кэш размера стака, быстрее пакеты чанков и команд.",
      en: "Entity activation via k-d tree, stack size cache, faster chunk and command packets.",
    },
  },
]

export const steps: { title: L; text: L }[] = [
  {
    title: { ru: "Скачайте jar", en: "Download the jar" },
    text: { ru: "Положите watermc-paperclip-1.21.11.jar в папку сервера.", en: "Put watermc-paperclip-1.21.11.jar into your server folder." },
  },
  {
    title: { ru: "Запустите с Java 21", en: "Run with Java 21" },
    text: {
      ru: "При первом запуске ядро скачает ванильный сервер с Mojang и наложит WaterMc поверх.",
      en: "On first start the core downloads the vanilla server from Mojang and applies WaterMc on top.",
    },
  },
  {
    title: { ru: "Настройте под себя", en: "Tune it" },
    text: {
      ru: "Конфиги ядра лежат в config/, миры в worlds/, данные игроков в data/.",
      en: "Core configs live in config/, worlds in worlds/, player data in data/.",
    },
  },
]

export const faq: { q: L; a: L }[] = [
  {
    q: { ru: "Мои плагины заработают?", en: "Will my plugins work?" },
    a: {
      ru: "Да. WaterMc основан на Leaf и Paper, API Bukkit, Spigot и Paper не менялся. Плагины под Spigot переотображаются автоматически.",
      en: "Yes. WaterMc builds on Leaf and Paper and keeps the Bukkit, Spigot and Paper API. Spigot plugins are remapped automatically.",
    },
  },
  {
    q: { ru: "Почему jar скачивает что-то при первом запуске?", en: "Why does the jar download something on first start?" },
    a: {
      ru: "Это формат paperclip, как у Paper и Leaf: в jar только изменения, а ванильный сервер берётся с официальных серверов Mojang. Дальше запуск идёт без интернета.",
      en: "It is the paperclip format used by Paper and Leaf: the jar carries only the changes and fetches the vanilla server from Mojang. Later starts run offline.",
    },
  },
  {
    q: { ru: "Как перейти с Paper, Purpur или Leaf?", en: "How do I switch from Paper, Purpur or Leaf?" },
    a: {
      ru: "Сделайте бэкап, замените jar и запустите. Конфиги переедут в config/, миры в worlds/, списки игроков в data/. Старые файлы сохраняются в data/migration-backup/.",
      en: "Take a backup, swap the jar and start. Configs move to config/, worlds to worlds/, player lists to data/. The originals are kept in data/migration-backup/.",
    },
  },
  {
    q: { ru: "Что включено по умолчанию?", en: "What is on by default?" },
    a: {
      ru: "Оптимизации, которые не меняют игру, и ранний отсев крепостей. Встроенный ViaVersion, скрытие сущностей, динамическая дальность и лимит размножения выключены, их включают в config/watermc.yml.",
      en: "Optimizations that keep gameplay identical, plus the early stronghold reject. Built-in ViaVersion, entity culling, dynamic distance and the breeding cap are off; turn them on in config/watermc.yml.",
    },
  },
  {
    q: { ru: "Где исходники?", en: "Where is the source?" },
    a: {
      ru: "На GitHub под GPL-3.0. Изменения Minecraft хранятся патчами против Leaf, кода Mojang в репозитории нет.",
      en: "On GitHub under GPL-3.0. Minecraft changes are stored as patches against Leaf, with no Mojang code in the repository.",
    },
  },
]

export type ConfigKey = { key: string; def: string; text: L }
export type ConfigGroup = { id: string; title: L; keys: ConfigKey[] }

export const configGroups: ConfigGroup[] = [
  {
    id: "performance",
    title: { ru: "Производительность", en: "Performance" },
    keys: [
      { key: "performance.stronghold-early-reject.enabled", def: "true", text: { ru: "Чанки, где крепость встать не может, не ждут расчёта колец. Мир тот же, выбор спавна быстрее.", en: "Chunks that can never hold a stronghold skip the ring calculation. Same world, faster spawn selection." } },
      { key: "performance.optimize-entity-activation.enabled", def: "false", text: { ru: "Проверка дальности активации через KD-дерево. Зона становится кругом вместо квадрата.", en: "Activation range check through a k-d tree. The zone becomes a circle instead of a square." } },
      { key: "performance.dynamic-distance.enabled", def: "false", text: { ru: "Снижает дальность симуляции, потом прорисовки, когда MSPT выше цели, и возвращает обратно.", en: "Lowers simulation, then view distance while MSPT is over target, and restores them later." } },
      { key: "performance.dynamic-distance.target-mspt", def: "40.0", text: { ru: "Целевой MSPT, от 10 до 50. Реакция при отклонении на ±5 мс.", en: "Target MSPT, 10 to 50. Reacts at ±5 ms." } },
      { key: "performance.dynamic-distance.min-view-distance", def: "4", text: { ru: "Нижняя граница дальности прорисовки.", en: "Lowest view distance." } },
      { key: "performance.dynamic-distance.min-simulation-distance", def: "4", text: { ru: "Нижняя граница дальности симуляции.", en: "Lowest simulation distance." } },
      { key: "performance.dynamic-distance.interval-seconds", def: "5", text: { ru: "Пауза между шагами.", en: "Seconds between steps." } },
      { key: "performance.command-block-parse-cache.enabled", def: "false", text: { ru: "Командные блоки не разбирают команду заново на каждом запуске.", en: "Command blocks do not re-parse their command on every run." } },
    ],
  },
  {
    id: "async",
    title: { ru: "Асинхронность", en: "Async" },
    keys: [
      { key: "async.thread-priorities.enabled", def: "false", text: { ru: "Потоки чанков и I/O ниже главного потока. Работает на Windows; на Linux нужен root.", en: "Chunk and I/O threads run below the main thread. Works on Windows; Linux needs root." } },
      { key: "async.thread-priorities.chunk-workers", def: "4", text: { ru: "Приоритет потоков чанков, от 1 до 10 (у главного 5).", en: "Chunk worker priority, 1 to 10 (main thread is 5)." } },
      { key: "async.thread-priorities.io-workers", def: "3", text: { ru: "Приоритет потоков I/O.", en: "I/O worker priority." } },
      { key: "async.portal-search-prefetch.enabled", def: "false", text: { ru: "Заранее грузит чанки у выхода из портала.", en: "Loads chunks near the portal exit in advance." } },
      { key: "async.parallel-sensor-phase.enabled", def: "false", text: { ru: "Сенсоры мобов считаются параллельно.", en: "Mob sensors are computed in parallel." } },
      { key: "async.parallel-sensor-phase.threads", def: "0", text: { ru: "Число потоков, 0 — автоматически.", en: "Thread count, 0 = automatic." } },
      { key: "async.async-locate.enabled", def: "false", text: { ru: "/locate для структур и биомов в фоне.", en: "/locate for structures and biomes in the background." } },
    ],
  },
  {
    id: "network",
    title: { ru: "Сеть", en: "Network" },
    keys: [
      { key: "network.multi-version.enabled", def: "false", text: { ru: "Встроенные ViaVersion и ViaBackwards 5.12.2. Свой jar в plugins/ главнее.", en: "Built-in ViaVersion and ViaBackwards 5.12.2. Your own jar in plugins/ wins." } },
      { key: "network.multi-version.min-version", def: "''", text: { ru: "Самая старая разрешённая версия клиента, например 1.21.5. Пусто — все.", en: "Oldest allowed client version, e.g. 1.21.5. Empty = all." } },
      { key: "network.multi-version.blocked-message", def: "This server supports Minecraft %s and newer.", text: { ru: "Сообщение кика, %s — минимальная версия.", en: "Kick message, %s = minimum version." } },
      { key: "network.raytrace-entity-culling.enabled", def: "false", text: { ru: "Не отправляет сущности, которые игрок точно не видит за стенами.", en: "Skips entities the player provably cannot see behind walls." } },
      { key: "network.raytrace-entity-culling.threads", def: "0", text: { ru: "Число потоков, 0 — процессоры / 8, от 1 до 4.", en: "Thread count, 0 = processors / 8, 1 to 4." } },
      { key: "network.raytrace-entity-culling.check-interval-ms", def: "50", text: { ru: "Как часто перепроверяется один игрок.", en: "How often one player is re-checked." } },
      { key: "network.raytrace-entity-culling.max-trace-distance", def: "64", text: { ru: "Дальше этой дистанции сущности не скрываются, от 16 до 160.", en: "Entities farther than this are never hidden, 16 to 160." } },
      { key: "network.raytrace-entity-culling.force-visible-radius", def: "4.0", text: { ru: "Ближе этого радиуса сущность видна всегда.", en: "Entities closer than this are always visible." } },
      { key: "network.raytrace-entity-culling.visibility-timeout-ms", def: "1000", text: { ru: "Сколько держится видимость, чтобы не было мерцания.", en: "How long visibility is kept to avoid flicker." } },
      { key: "network.raytrace-entity-culling.aabb-expansion", def: "0.5", text: { ru: "Запас к хитбоксу при проверке.", en: "Hitbox margin for the check." } },
      { key: "network.raytrace-entity-culling.bounding-box-limit", def: "20", text: { ru: "Сущности с большим хитбоксом не скрываются.", en: "Entities with a larger hitbox are never hidden." } },
      { key: "network.raytrace-entity-culling.cull-players", def: "true", text: { ru: "Скрывать и игроков тоже.", en: "Hide players too." } },
      { key: "network.raytrace-entity-culling.skip-marker-armor-stands", def: "true", text: { ru: "Голограммы на стойках для брони видны всегда.", en: "Hologram armor stands are always visible." } },
      { key: "network.raytrace-entity-culling.skipped-entity-types", def: "[]", text: { ru: "Типы сущностей, которые не скрываются никогда.", en: "Entity types that are never hidden." } },
    ],
  },
  {
    id: "gameplay",
    title: { ru: "Геймплей", en: "Gameplay" },
    keys: [
      { key: "gameplay-mechanisms.breeding-cap.enabled", def: "false", text: { ru: "Мобы не размножаются, если вокруг уже много особей того же вида.", en: "Mobs stop breeding when many of their kind are already nearby." } },
      { key: "gameplay-mechanisms.breeding-cap.limit", def: "32", text: { ru: "Сколько мобов того же вида блокируют размножение.", en: "How many mobs of the same type block breeding." } },
      { key: "gameplay-mechanisms.breeding-cap.range", def: "64", text: { ru: "Радиус в блоках, от 1 до 128.", en: "Radius in blocks, 1 to 128." } },
      { key: "gameplay-mechanisms.disable-leaf-decay", def: "false", text: { ru: "Листва не опадает.", en: "Leaves do not decay." } },
      { key: "gameplay-mechanisms.crafter-1gt-delay", def: "false", text: { ru: "Крафтер срабатывает с задержкой 1 тик.", en: "The crafter fires with a 1-tick delay." } },
    ],
  },
  {
    id: "diagnostics",
    title: { ru: "Диагностика", en: "Diagnostics" },
    keys: [
      { key: "misc.diagnostics.plugin-watchdog.startup-grace-seconds", def: "60", text: { ru: "Сколько секунд после старта watchdog плагинов молчит.", en: "Seconds after startup the plugin watchdog stays quiet." } },
      { key: "misc.diagnostics.netty-watchdog.enabled", def: "true", text: { ru: "Пишет в лог, если сетевой поток завис.", en: "Logs a stalled network thread." } },
      { key: "misc.diagnostics.netty-watchdog.stall-seconds", def: "10", text: { ru: "Через сколько секунд поток считается зависшим.", en: "Seconds before a thread counts as stalled." } },
      { key: "misc.diagnostics.netty-watchdog.print-stack", def: "true", text: { ru: "Печатать стек зависшего потока.", en: "Print the stalled thread's stack." } },
      { key: "misc.diagnostics.security-audit.ops", def: "true", text: { ru: "Выдача и снятие OP.", en: "OP grants and removals." } },
      { key: "misc.diagnostics.security-audit.console-commands", def: "true", text: { ru: "Команды, которые плагины выполняют от имени консоли.", en: "Commands plugins run as the console." } },
      { key: "misc.diagnostics.security-audit.game-modes", def: "true", text: { ru: "Переход в творческий режим и режим наблюдателя.", en: "Switches to creative and spectator mode." } },
      { key: "misc.diagnostics.security-audit.plugin-disable", def: "true", text: { ru: "Отключение плагина во время работы.", en: "A plugin disabled while running." } },
      { key: "misc.diagnostics.security-audit.stack-traces", def: "false", text: { ru: "Полный стек вызова для каждого события.", en: "Full call stack for every event." } },
    ],
  },
]
