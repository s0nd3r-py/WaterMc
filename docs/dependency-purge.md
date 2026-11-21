# Dependency-Purge: кэши и примитивные коллекции

Все обращения `com.google.common.cache.*` в производственных исходниках сервера заменены на Caffeine. Версии зависимостей уже удовлетворяли задаче: Caffeine 3.2.3 и fastutil 8.5.15; менять их ради смены версии не требуется. Guava остаётся для остальных API коллекций и транзитивных библиотек Minecraft.

Мигрированы ProfileResolver (профили/скины), EncoderCache (кодирование палитр/NBT), Block (формы), BlockPattern, SpikeFeature, FeatureCountTracker, ServerPlayer (компоненты слотов), Level (breeding cooldown), а также Jade, ChatImage и ProtocolUtils. Месячный персистентный CachedUserNameToIdResolver в этой версии не использовал Guava Cache: он сохраняет существующие карты и согласованные индексы имени/UUID.

WaterCaches задаёт ForkJoinPool.commonPool для обслуживания и системный scheduler для истечения сроков. Загрузчики выполняются синхронно на вызывающем потоке; обращения BlockPattern к миру не выносятся в фон. Bounded-кэши используют политику Caffeine; у исходно безлимитных временных кэшей границы не выдумываются. Сроки жизни, weak-key identity и soft values сохранены. Состояния промахов профилей представлены Optional.empty и кэшируются.

Caffeine передаёт исключения загрузчиков без checked ExecutionException. В трёх обработчиках Jade обработка переведена на RuntimeException с сохранением предусмотренного журналирования и fallback. В EncoderCache сохраняется копирование изменяемого NBT при возврате значения.

Сбор уникальных ID в PalettedContainer.getAll использует IntOpenHashSet вместо линейного IntArraySet: обработка секции с большим числом глобальных ID больше не требует квадратичного поиска. Trove в исходниках сервера/API и их Gradle-зависимостях не обнаружен. Неизменяемые пустые коллекции и публичные unmodifiable-view оставлены с прежним контрактом; это не боксы примитивов. Отдельный shuffle списка углов LongJumpToRandomPos остаётся кандидатом AI-модуля.

CacheMigrationTest проверяет expiry с искусственным временем, ограничение размера после cleanUp, identity weak keys, единственную загрузку при конкурентных промахах, выполнение загрузчика на владельце, независимость NBT-копий и положительные/отрицательные кэши профилей без сетевых запросов. Нулевая аллокация промаха/записи Caffeine не заявляется: новые записи и ключи выделяют память.

Проверка 11 ноября 2026: compileJava, test, runWaterTests (49/49), runWaterCompatibilityTests (26/26) — BUILD SUCCESSFUL. XML и лог: benchmarks/results/2026-11-11-autonomous/dependency-purge/verified.
