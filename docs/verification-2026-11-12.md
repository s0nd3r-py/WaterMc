# Проверка WaterMc: 12 ноября 2026

Готовый запускаемый JAR: `watermc-server/build/libs/watermc-1.21.11.jar` (124,900,346 bytes).
SHA-256: `0152dea982bbb0b8a5d41143d1d00540372b700256ad18a417936f6e2fa0f296`.

Crash Recovery/AI: BUILD SUCCESSFUL, Water 66/66, compatibility 26/26. Lightning Boot: BUILD SUCCESSFUL, Water 77/77, compatibility 26/26. Hyper Chunk/World IO: BUILD SUCCESSFUL (14m45s), Water 97/97, compatibility 26/26, полный `test` прошёл. Final Packaging: BUILD SUCCESSFUL (1m39s), compile/test задачи UP-TO-DATE от полного прогона, новый JAR собран, scanJarForBadCalls прошёл. Upstream XML 8951 содержит suite containers, 22 skipped; собственные 97 и compatibility 26 без skipped.

`java -jar ... --version` на Java 21 завершился с кодом 0: `1.21.11-DEV-17f2dbd`. Проверена внутренняя SHA versions.list, бренд WaterMc и отсутствие обязательных бинарных дельт. 16 ключевых классов JAR сопоставлены с текущими compiled classes через ASM canonical serialization. Сохраняются все атрибуты, инструкции/поля/методы; нормализуются constant pool и JVM declaring-owner для унаследованных полей, которые штатно переписывает Paperweight. Первоначальные строгие byte-comparison ошибки сохранены в результатах: это различие представления, а не stale classes.

Probe исполнен на библиотеках/классах именно финального JAR, не на Gradle loose classpath. Настоящий Bootstrap и modern update/optimize не строят DFU. Затем старые options (DataVersion 500) преобразуют EN_US в en_us, числовой legacy item 1 (DataVersion 99) — в minecraft:stone/count=1; schemas строятся по требованию. ZSTD round-trip использует native zstd-jni; ZLIB provider на этой Windows — Java fallback. JCTools присутствует, legacy remapper выключен по умолчанию. EntityCollisionCache отсутствует в JAR.

[Lightning Boot](lightning-boot.md), [World IO/сеть и настройки](world-io-compression.md), [аудит памяти](memory-footprint-audit.md), [машинный отчёт с хешами незакоммиченных runtime inputs](final-distribution-verification-2026-11-12.json), [логи/XML](../benchmarks/results/2026-11-12-autonomous/).

ZLIB остаётся обычным переносимым Anvil по умолчанию. ZSTD включается через compression-format: ZSTD и использует marker 127 + watermc:zstd, поэтому новый codec требует совместимого reader. Старые ZLIB/GZIP читаются автоматически. Сжатый direct payload разделяется между каналами, перед per-client encryption копируется. Упакованные sections кэшируются с no-lock logical invalidation. Часть сериализации отдельных packet objects/NBT остаётся; требуемые независимые CompoundTag copies сохранены.

Проверенные прогретые FastAABB, flat storage, TPS/cache hits, block reads/packet size и cached mutations: 0 байт по ThreadMXBean. Это не нулевая аллокация всего tick/AI/IO. Полного Folia Owned Regions и immutable world/Mob snapshot async A* нет, async остаётся opt-in. Закрытые плагины/100 игроков/длительный uptime и контролируемые показатели MSPT/RAM на 7950X3D не проверены. Старые функциональные прогоны 8–10 ноября относятся к другим SHA JAR.

Все изменения и предшествующие пользовательские правки сохранены в рабочем дереве D:/water-mc/WaterMc. Новых коммитов/публикаций нет; Git HEAD 17f2dbd — базовая метка, а не утверждение о чистом дереве. В docs/final-distribution-verification-2026-11-12.json записаны SHA изменённых runtime inputs. D:/water-mc/Water не изменялся. Активных серверов этой проверки не оставлено.
