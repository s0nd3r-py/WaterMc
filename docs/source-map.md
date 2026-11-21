# Карта исходников и границы аудита

Полные пути с именем модуля отсчитываются от корня репозитория. Сокращённые `net/minecraft` и `ca/spottedleaf` — от `watermc-server/src/minecraft/java`. Это реальные Java-файлы, не места хранения патчей. Карта не означает завершённый аудит каждого алгоритма.

| Подсистема | Точки входа | Что сохранять при изменениях |
| --- | --- | --- |
| Запросы сущностей и коллизии | `ca/spottedleaf/moonrise/patches/chunk_system/level/entity/ChunkEntitySlices.java`; `net/minecraft/world/level/Level.java` | Границы AABB, порядок выдачи, составные сущности, изменения коллекции из callback, владение потоком мира. Существующий цикл уже использует массивы и кэш границ. |
| Tracking и пакеты | `net/minecraft/server/level/ServerEntity.java`; `net/minecraft/server/network/ServerGamePacketListenerImpl.java` | Порядок пакетов, видимость, dirty-флаги metadata, скорость и синхронизацию игрока. Ограничения визуальных эффектов остаются опциональными. |
| Игроки и здоровье | `net/minecraft/server/players/PlayerList.java`; `net/minecraft/world/entity/LivingEntity.java`; `watermc-server/src/main/java/org/bukkit/craftbukkit/entity/CraftPlayer.java` | Реальное здоровье хранит CraftPlayer; tracked health может быть масштабированным. Respawn и teleport передают Bukkit-обёртки. |
| Очереди чанков | `ca/spottedleaf/moonrise/patches/chunk_system/scheduling/ChunkTaskScheduler.java`; `NewChunkHolder.java` в том же пакете | Зависимости, tickets, отмену, завершения и потоки callback. Не добавлять обращения Bukkit к миру из worker. |
| Запланированные тики | `net/minecraft/world/ticks/LevelTicks.java`; `LevelChunkTicks.java` | Время, приоритет, subtick-порядок, unload/reload и постановку новых задач во время тика. |
| Редстоун и block entities | `net/minecraft/server/level/ServerLevel.java`; `watermc-server/src/main/java/org/dreeam/leaf/util/list/BlockEntityTickersList.java`; Lithium в `src/main/java/net/caffeinemc/mods/lithium` | Обновления соседей, события инвентарей, пробуждения, компараторы и момент вызова плагинов. |
| Pathfinding и AI | `net/minecraft/world/level/pathfinder/WalkNodeEvaluator.java`; существующий асинхронный pathfinding Leaf в основных исходниках | Типы пути/коллизий и доступ к меняющимся данным мира. Буферы поиска должны иметь конкретного владельца. |
| Освещение | `ca/spottedleaf/moonrise/patches/starlight/light/StarLightEngine.java`, `SkyStarLightEngine.java`, `BlockStarLightEngine.java` | Зависимости соседних чанков, границы света, результат распространения и владение данными. |
| Block/light updates | `net/minecraft/server/level/ChunkHolder.java` | Существующую группировку по секциям, dirty-флаги, итоговое состояние клиента, порядок и pending unload. |
| Region I/O | `ca/spottedleaf/moonrise/patches/chunk_system/io/MoonriseRegionFileIO.java`; `net/minecraft/world/level/chunk/storage/RegionFileStorage.java`, `RegionFile.java`; основные исходники `abomination/LinearRegionFile.java`, `me/earthme/luminol/data/BufferedLinearRegionFile.java` | Форматы, порядок записи, flush/shutdown, восстановление после сбоя. Перед заменой нужны save/reload и прерванные записи. |
| Планировщик плагинов | `watermc-server/src/main/java/org/bukkit/craftbukkit/scheduler/CraftScheduler.java` | Главный поток, порядок, отмену задач и постановку задачи на текущий тик. |
| События | `watermc-api/src/main/java/org/bukkit/event/HandlerList.java` и фабрики событий CraftBukkit | Порядок регистрации, приоритеты, отмену, видимость массива обработчиков и синхронные вызовы. Байткод API сохранён. |

## Уже обоснованные решения

- Сохранить публичные классы API и пакеты; перенос подтверждён побайтовым сравнением.
- Сохранить мост здоровья CraftPlayer. Живой тест проверил Bukkit/NMS при нестандартном масштабе; чтение tracked health было бы ошибкой.
- Не ограничивать вибрации: `VibrationSystem.tryReloadVibrationParticle` использует результат доставки для сброса повторной попытки. Адресные эффекты и Bukkit API тоже проходят отдельно.
- Не считать неудачные chunk futures успешной генерацией; стенд требует новый мир.
- Отделять кандидаты на ускорение от измеренного выигрыша. Старые профили направляют поиск; окончательное решение требует целевой нагрузки.

Анализ владения данными и планировщика Folia — в `folia-analysis.md`. Региональная многопоточность в классическую модель Bukkit не внедрена.
