# Автономный пайплайн: актуально на 12 ноября 2026

Рабочий репозиторий D:/water-mc/WaterMc; C:/Users/<user>/Desktop/leafmc fork - water-mc/WaterMc — ссылка на него. Прежний D:/water-mc/Water не менялся. Существовавшие Core-Math/документы и пользовательское dirty tree сохранены, новых коммитов/публикаций нет.

Текущий пайплайн пользователя завершён и проверен: Crash Recovery -> Lightning Boot -> Hyper Chunk/World IO -> Memory/Final Packaging. Последний полный прогон compileJava/test/runWaterTests/runWaterCompatibilityTests BUILD SUCCESSFUL: Water 97/97, compatibility 26/26, upstream test без ошибок. Затем createWaterMcJar и scanJarForBadCalls BUILD SUCCESSFUL, runnable --version и packaged legacy DFU/native codec probe прошли. Активных сборок/тестовых серверов нет.

[Полный итог/артефакт/SHA](verification-2026-11-12.md), [аудит памяти](memory-footprint-audit.md), [машинный отчёт](final-distribution-verification-2026-11-12.json). Логи/XML/история попыток: benchmarks/results/2026-11-12-autonomous; предыдущие модули: benchmarks/results/2026-11-11-autonomous.

Ранее завершены Chunk Storage, Dependency Purge, scheduler/threading и AI: docs/chunk-storage.md, dependency-purge.md, scheduler-threading.md, entity-ai.md. Новые изменения описаны в docs/lightning-boot.md и docs/world-io-compression.md.

Границы: FoliaGlobalRegionScheduler оптимизирован, но полного Owned Regions здесь нет (FallbackRegionScheduler). Async A* имеет исправления publication/termination и реальный A* differential на 20 immutable grids, но не новый immutable world/Mob snapshot; opt-in/off. Zero-copy относится к общему compressed payload/gathering prefix, не ко всей сериализации NBT и encrypted output. Нулевые аллокации доказаны только на перечисленных прогретых путях. ZLIB переносимый default; native ZSTD custom codec opt-in требует WaterMc-compatible reader. Частные плагины/100 игроков/контролируемый MSPT/RAM benchmark не проверены.

Recovery .lock удаление отклонялось автоматической политикой; штатный Gradle восстановился и все сборки прошли без него. Служебные binary файлы скопированного первого test report оставлены после отказа очистки. На результат это не влияет.

Для дальнейших Gradle запусков обязательны GRADLE_USER_HOME=D:/water-mc/gradle-home, TMP/TEMP=D:/water-mc/tmp, GRADLE_OPTS=-Djava.io.tmpdir=D:/water-mc/tmp и BelowNormal. Не менять D:/water-mc/Water и игровые процессы. Сначала смотреть актуальный отчёт выше, не выполнять устаревшие session IDs из исторического HANDOFF.
