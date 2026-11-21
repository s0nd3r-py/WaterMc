# Модернизация ядра: точка входа, раскладка каталогов, зачистка легаси (12 ноября 2026)

## Точка входа `org.watermc.WaterMain`

- `Main-Class` серверного JAR и `META-INF/main-class` бандлера — `org.watermc.WaterMain`.
- `org.bukkit.craftbukkit.Main` — тонкий делегат: `main(String[])` → `WaterMain.main`, публичные поля `useJline`, `useConsole`, `BOOT_TIME` сохранены.
- Всегда headless: `java.awt.headless=true`, удалены `MinecraftServerGui`, `com.destroystokyo.paper.gui.*`, `org.purpurmc.purpur.gui.*`, проверка `GraphicsEnvironment` и AWT-проверка JRE при старте. AWT по-прежнему загружается лениво только API (`loadServerIcon(BufferedImage)`, карты).
- Белый список CLI: `--port`, `--host`, `--world-dir`, `--plugins`, `--config`, `--bukkit-settings`, `--spigot-settings`, `--paper-settings` (каталог конфигов Paper), `--purpur-settings`, `--watermc-settings`, `--commands-settings`, `--help-settings`, `--permissions-settings`, `--nojline`, `--noconsole`, `--version`, `--forceUpgrade`, `--help`.
- Удалённые флаги (`--demo`, `--bonusChest`, `--eraseCache`, `--jfrProfile`, `--singleplayer`, `--safeMode`) завершают запуск с кодом 2 и понятным сообщением. Прочие неизвестные флаги (`--world`, `--add-plugin`, `--initSettings`, `--serverId`, `--pidFile` …) тоже отклоняются.
- `nogui` / `--nogui` от хостинг-панелей принимается как no-op: сервер и так headless, а отказ ломал бы стандартные egg-шаблоны Pterodactyl.

## Раскладка каталогов (`org.watermc.bootstrap.ServerLayout`)

| Где | Что |
| --- | --- |
| корень | `watermc-1.21.11.jar`, `server.properties`, `eula.txt`, `config/`, `data/`, `worlds/`, `logs/`, `plugins/`, `libraries/` |
| `config/` | `bukkit.yml`, `spigot.yml`, `purpur.yml`, `commands.yml`, `help.yml`, `permissions.yml`, `paper-global.yml`, `paper-world-defaults.yml`, `watermc.yml`, `watermc-world.yml` |
| `data/` | `ops.json`, `whitelist.json`, `banned-players.json`, `banned-ips.json`, `usercache.json`, `version_history.json` |
| `worlds/` | миры по умолчанию |

- Автомиграция при старте: файлы из корня переносятся в `config/`/`data/`, `config/leaf-global.yml` → `config/watermc.yml`, `config/gale-world-defaults.yml` → `config/watermc-world.yml`, `<мир>/gale-world.yml` → `<мир>/watermc-world.yml`. Существующий файл в новом месте никогда не перезаписывается — конфликт пишется в лог, старый файл остаётся.
- Миры: если задан `--world-dir` или `settings.world-container` в `bukkit.yml` — используется он; если в корне уже есть папка мира (`world` или `level-name`) — корень, чтобы существующие миры не «пропали»; иначе `worlds/`.
- `settings.permissions-file` с относительным путём: существующий файл по старому пути уважается, иначе путь разрешается в `config/`.

## Консолидация конфигов форков

- `config/watermc.yml` — бывший `leaf-global.yml` плюс секция `server-tuning` (все 25 настроек бывшего `gale-global.yml`). При первом старте значения берутся из `gale-global.yml`, затем он перемещается в `config/legacy/` (не удаляется).
- `config/watermc-world.yml` и `<мир>/watermc-world.yml` — бывшие мировые конфиги Gale (тот же формат Configurate).
- Отдельного `leaves.yml` в этом форке не было: настройки Leaves уже жили в конфиге Leaf.
- Префикс логгера конфигурации `[LeafConfig]` → `[WaterMc]`; имена потоков и сообщения Leaf/Gale → WaterMc. `isBrandCompatible` по-прежнему принимает ключи Paper/Gale/Leaf для плагинов.

## Зачистка легаси

- Удалены неиспользуемые классы: `PWTEventScheduler`, `KDTree2D`, `SpscIntQueue`, `org.spigotmc.TickLimiter`, мёртвые `allMatch`/`anyMatch` в `HopperBlockEntity`. Протоколы Leaves (`AppleSkinProtocol`, `AsteorBarProtocol`) и модули конфигов **не** мёртвые: их находит сканирование пакетов по аннотациям.
- `SimpleDateFormat` → потокобезопасные `DateTimeFormatter` (`org.watermc.util.WaterDateFormats`): `usercache.json`, сообщения о бане (общий статический `SimpleDateFormat` в `PlayerList` был гонкой данных), метка резервной копии, формат совместим с прежними файлами.
- `StringBuffer` → `StringBuilder` (`RconConsoleSource`, `MultipleTestTracker`).
- `NodeEvaluatorCache` (async pathfinding): глобальный `synchronized` → `ConcurrentHashMap` + `ConcurrentLinkedQueue`, создание оценщика вне блокировки.
- `SyncLongOpenHashSet` (sentChunks при multithreaded tracker): монитор → реентерабельный `ReentrantReadWriteLock`, параллельные `contains` из потоков трекера.
- Оставлено осознанно: `Collections.synchronizedList` в `ServerConnectionListener` (ванильный код делает `synchronized (connections)` при обходе и удалении в тике — замена на COW или lock-free изменила бы семантику); `java.io.File` в публичном Bukkit API и в местах, которые передают `File` в API.

## Горячие пути

- `ChunkMap.TrackedEntity.updatePlayers`: дальность с учётом пассажиров, Y-дальность из конфига, позиция и чанк сущности вычисляются один раз на проход, а не для каждого игрока; цикл по индексу без итератора. Если плагин телепортирует сущность внутри `PlayerTrackEntityEvent`, оставшиеся игроки этого прохода используют позицию на начало прохода — следующий тик пересчитывает.
- Рефлексии в горячих путях CraftBukkit нет: остаток — старт, heap dump и уже мемоизированный `CraftBlockType.isInteractable`. `CraftMagicNumbers` уже на `IdentityHashMap`/`EnumMap`.
- Воронки уже содержат быстрый выход Paper по полной/пустой воронке и Lithium sleeping block entities. Новых изменений логики воронок не вносилось.

## Не проверено

Ускорение трекинга и блокировок не измерено бенчмарком; живой запуск с реальными плагинами и Pterodactyl не выполнялся.
