# Аудит памяти: 12 ноября 2026

Проверено на локальном Windows/HotSpot Java 21, AMD FX-4100, с пониженным приоритетом. Это функциональная проверка аллокаций/владения, не A/B измерение скорости на 7950X3D. Полный World-IO прогон: BUILD SUCCESSFUL, Water 97/97, compatibility 26/26, upstream test task прошёл. Water/compatibility skipped=0. Upstream XML содержит 8951 записей, включая suite containers; 22 skipped согласно существующей конфигурации, поэтому это не 8951 обычный исполненный тест.

| Проверенный путь | Результат | Границы измерения |
| --- | --- | --- |
| FastAABB primitive resolve | 0 байт | Пять прогретых выборок по 100 000 вызовов; AABB и scratch уже созданы |
| WaterSectionStorage get/set/getAndSet | 0 байт | Compact same-value и materialized storage, пять выборок по 1 000 000 циклов |
| TPS primitive ring + identity cache hit | 0 байт | Пять выборок по 10 000 циклов после прогрева |
| PalettedContainer cached packet-size + block reads | 0 байт | Пять выборок по 10 000 циклов; snapshot уже закэширован |
| Мутации блока после packet cache | 0 байт | Пять выборок по 10 000 циклов; logical invalidation без Caffeine вызова/монитора |
| Shared compressed backing | Последний release даёт refCnt=0 | Cache owner + два observer references, close/eviction races, отдельное cipher ownership |

Нулевые значения измерены ThreadMXBean для текущего потока; тесты не пропущены. Они не означают нулевые аллокации всего server tick, всех AI callbacks, плагинов или чанк-загрузки. Изоляция логики теста исключает JUnit setup/logging из измеряемого участка.

Ленивое однородное хранилище не имеет long[4096] до первого изменения значения. После материализации это 32 КиБ payload плюс JVM array header. Первая мутация и copy/snapshot требуют выделений. Холодное создание identity cache копирует маленькую карту; ordinal cache конкурирующий miss может создать проигравший кандидат, hit — без монитора. Сенсоры/LoS, A* nodes/maps, immutable target snapshots, NBT и packet objects продолжают выделять память; DAB по умолчанию выключен, async A* остаётся opt-in.

Bulk region compression держит raw NBT до close, затем освобождает ссылку. Его transient память выше потокового ZLIB; предел — MAX_CHUNK_SIZE, а не 0 байт на загрузку. Только 8 КиБ transfer scratch остаются на compression worker; не удерживается последний большой raw chunk. Native compressor, input/output ByteBuf освобождаются и на ошибке. Отдельные contexts encoder/decoder закрываются владельцами; threshold update не создаёт новый compressor, Java Deflater/Inflater заканчиваются end().

Глобальные бюджеты по умолчанию: 32 МиБ section packets и 64 МиБ exact compressed cache с raw comparison bytes. Caffeine bounds приблизительные при concurrent admission и учитывают заданный вес, а не точный JVM retained size. Section keys weak, value не удерживает source Data; logical invalidation оставляет старый bounded entry до замены/eviction. Compressed frames expireAfterAccess 2 с, cache ref и channel refs считаются отдельно. Создание retainedDuplicate, ByteBuffer view, cache key и маленького prefix buffer не является zero-allocation. Для encrypted соединения writable copy необходима из-за отдельного ключа/состояния шифра.

Не удалены необходимые независимые CompoundTag.copy для нескольких читателей и pending write. Распаковка/parse выполняются существующими Moonrise compression/load executors. Zero-copy доказан для общего compressed payload и NIO gathering prefix, а не для произвольного внешнего transport/plugin или всей сериализации NBT разных packet objects.

Полного Owned Regions в ветке нет (FallbackRegionScheduler); новый immutable world/Mob snapshot для A* не реализован. Сравнение реального vanilla A* в owner/worker совпало на 20 immutable grids. Приёмка с живыми закрытыми плагинами, 100 игроками, большим миром, длительным uptime, JFR/heap-профилем и контролируемым MSPT/RAM A/B остаётся отдельной работой на целевом сервере.

Источники: [World IO](world-io-compression.md), [Lightning Boot](lightning-boot.md), [Core Math/Storage](chunk-storage.md), [AI](entity-ai.md), [результаты](../benchmarks/results/2026-11-12-autonomous/world-io/verified/summary.json).
