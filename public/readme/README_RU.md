<img src="../image/watermc_banner.png" alt="WaterMc">
<div align="center">

[![Download](https://img.shields.io/github/v/release/s0nd3r-py/WaterMc?label=скачать&style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://github.com/s0nd3r-py/WaterMc/releases/latest)⠀
[![Minecraft](https://img.shields.io/badge/minecraft-1.21.11-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://github.com/s0nd3r-py/WaterMc/releases)⠀
[![Java](https://img.shields.io/badge/java-21-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](https://adoptium.net/)⠀
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=for-the-badge&colorA=0b1e2d&colorB=1f8fd6)](../../LICENSE.md)

**WaterMc** это форк [Leaf](https://github.com/Winds-Studio/Leaf) для Minecraft 1.21.11, настроенный на быструю генерацию мира, быстрый запуск и серверы с толпами мобов и игроков.
</div>

> [!WARNING]
> При первом запуске WaterMc переносит файлы сервера по новым папкам (`config/`, `data/`, `worlds/`). Сделайте бэкап **до** перехода.

[English](../../README.md) | **Русский**

## 💧 Возможности
- **Основан на [Leaf](https://github.com/Winds-Studio/Leaf)** (Paper → Purpur → Gale → Leaf): плагины Bukkit, Spigot и Paper работают без правок
- **Быстрее генерация мира**: больше чанков в секунду, чем у Leaf, и намного быстрее выбор точки спавна на новом мире
- **Быстрый запуск**: свой загрузчик, ленивый DataFixerUpper, свой кэш ремаппера плагинов
- **Толпы и фермы**: толкание дешевле, ИИ мобов умнее, меньше пустых поисков блоков
- **Встроенные ViaVersion и ViaBackwards** (опция), свой экземпляр в `plugins/` всегда главнее
- **Скрытие сущностей за стенами** (опция): невидимые игроку сущности не отправляются, ESP-читы их не видят
- **Порядок в папке сервера**: `config/`, `data/`, `worlds/`, `logs/` вместо тридцати файлов в корне
- **Инструменты админа**: `/watermc inspect`, `/watermc drain`, watchdog плагинов и Netty, аудит безопасности
- **WaterScript**: встроенные скрипты с событиями, командами, таймерами и переменными
- **Исправления**: дюп через товар жителя (1.21.6+), портал и платформа Края игнорировали отмену `BlockDestroyEvent`

### 🚀 Запуск и загрузка плагинов
- **Свой загрузчик** (`me.s0nd3r.watermc.launcher`) вместо стандартного загрузчика бандлера. Он помнит размер, время изменения и SHA-256 каждой библиотеки в `libraries/.watermc-launcher` и не перечитывает файлы, которые не менялись, поэтому тёплый старт не пересчитывает хэши всей папки `libraries/`. На Java ниже 21 пишет понятное «нужна Java 21» вместо стека ошибок.
- **Свой кэш ремаппера плагинов** (`plugins/.water-remapped`). Плагины под Spigot переотображаются на имена Mojang, как в Paper, но хэши jar кэшируются в `hashes.tsv`, индекс перезаписывается только при изменениях, а плагины, которые используют только API Bukkit/Paper, не переотображаются вообще.
- **Ленивые маппинги**: таблицы Spigot↔Mojang (десятки МБ) читаются только когда они действительно нужны первому плагину. С современными плагинами они не грузятся никогда.
- **Ленивый DataFixerUpper**: схемы конвертации строятся только когда сервер встречает данные старой версии Minecraft.
- **Без GUI**: окно консоли на Swing/AWT и всё его дерево классов удалены.

### 🧩 Что взято из модов и других форков
| Источник | Что используется в WaterMc |
|---|---|
| [Lithium](https://github.com/CaffeineMC/lithium) | Счётчик изменений памяти мозга (`Behavior.hasRequiredMemories` кэшируется, пока память не появилась или не пропала), поиск блоков без POI для `MoveToBlockGoal`, `RemoveBlockGoal`, сенсоров хоглинов и пиглинов, формы втягивающегося поршня |
| [DivineMC](https://github.com/BX-Team/DivineMC) | Скрытие сущностей за стенами, параллельные сенсоры, предзагрузка портальных чанков, асинхронный `/locate`, кэш разбора команд в командных блоках |
| [ModernFix](https://github.com/embeddedt/ModernFix) | Ранний отсев крепостей: чанки, где крепость встать не может, не ждут расчёта колец |
| [ServerCore](https://github.com/Wesley1808/ServerCore) | Лимит размножения и динамическая дальность прорисовки и симуляции по MSPT |
| [Krypton](https://github.com/astei/krypton) | Строки пишутся прямо в буфер пакета (`Utf8String.write`), без временного буфера |
| [FerriteCore](https://github.com/malte0811/FerriteCore) | Опустевший патч компонентов предмета (`PatchedDataComponentMap`) делит одну общую неизменяемую карту |
| [threadtweak](https://github.com/getchoo/threadtweak) | Потоки чанков и I/O можно запустить с приоритетом ниже основного потока |
| Ветки Leaf 26.x | Активация сущностей через KD-дерево, кэш максимального размера стака, битовая маска посещённых секций в `PoiAccess`, карты высот в `EnumMap` в пакете чанка, быстрее `ClientboundCommandsPacket`, исправление отмены разрушения блоков и другое |

Весь перенесённый код переписан под базу 1.21.11, авторы и лицензии указаны в [`docs/inherited-attribution.md`](../../docs/inherited-attribution.md).

### 🔧 Что переписано в самом WaterMc
- `Climate.RTree`: поиск биома хранит границы узлов в одном плоском `long[14]` с развёрнутым циклом, результат совпадает с ванилью до бита
- `MoonriseCommon`: новая формула потоков чанков (4 ядра → 2 потока, 8 → 5, 16 → 9), а пока готовятся миры на старте, чанки генерируют все ядра
- `ConcentricRingsStructurePlacement`: радиальные границы колец крепостей считаются один раз
- `ChunkEntitySlices`: списки сущностей секции хранят плоскую копию габаритов `double[]`, поиск соседей не трогает объекты сущностей
- `LevelTicks`: запланированные тики помнят ближайший срок и не обходят контейнеры до него
- `LivingEntity` / `Entity`: здоровье читается из обычного поля, `getTeam()` у мобов без команды отвечает сразу, поиск соседей для толкания останавливается, когда их уже достаточно
- `ExperienceOrb`: слияние сфер останавливается на первом совпадении и ищет по типу сущности
- `ActivationRange`, `Brain`, `Behavior`, `MoveToBlockGoal`, `PatchedDataComponentMap`, `Utf8String`, `PalettedContainer.getAll`
- Списки игроков (`ops`, `whitelist`, баны, кэш игроков) переехали из JSON в журнал с дозаписью `data/*.wdb` с CRC32C на каждую запись

### 📦 Библиотеки
- **Guava Cache → [Caffeine](https://github.com/ben-manes/caffeine) 3.2.3** по всему серверу: профили и скины, кэш кодирования NBT, формы блоков, шаблоны блоков и другие
- `SimpleDateFormat` → `DateTimeFormatter` (один общий форматтер в `PlayerList` дёргали из разных потоков), `StringBuffer` → `StringBuilder`, `IntArraySet` → `IntOpenHashSet` в `PalettedContainer.getAll`
- Свежие версии: Netty 4.2.15, fastutil 8.5.15, Log4j 2.25.3, ASM 9.9.1, LMAX Disruptor 4.0.0, zstd-jni
- Файлы регионов сжимает нативный libdeflate (натив Velocity), по желанию регионы в ZSTD

## 📥 Скачать
Свежая сборка в [GitHub Releases](https://github.com/s0nd3r-py/WaterMc/releases/latest). При первом запуске jar скачивает ванильный сервер с серверов Mojang и накладывает WaterMc поверх, как это делают Paper и Leaf.

```bash
java -Xms4G -Xmx4G -jar watermc-paperclip-1.21.11.jar --nogui
```

## 📄 Документация
- [Чем WaterMc отличается от Leaf](../../docs/about-watermc.md)
- [Новые ключи конфига в этом релизе](../../docs/release-description.md)
- [WaterScript](../../docs/scripting.md)

## 📦 Сборка
Изменения Minecraft хранятся патчами против Leaf, кода Mojang в репозитории нет. Нужны JDK 21 и Git.

```bash
./scripts/setup-source.sh      # Windows: .\scripts\setup-source.ps1
./gradlew :watermc-server:createMojmapPaperclipJar
```

## ⚖️ Лицензия
[GPL-3.0](../../LICENSE.md). Код из других проектов сохраняет свою лицензию и авторство: [`licenses/`](../../licenses), [`docs/inherited-attribution.md`](../../docs/inherited-attribution.md).
