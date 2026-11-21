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
- **Быстрее генерация мира**: +27% чанков в секунду против Leaf, выбор точки спавна на новом мире в 2–3 раза быстрее
- **Быстрый запуск**: свой загрузчик доходит до main-класса сервера за 0,7 с вместо ~4 с, ленивый DataFixerUpper, маппинги грузятся только когда они нужны плагину
- **Толпы и фермы**: толкание в толпе из 600 мобов вдвое дешевле, кэш памяти мозга и поиск блоков из Lithium
- **Встроенные ViaVersion и ViaBackwards** (опция), свой экземпляр в `plugins/` всегда главнее
- **Скрытие сущностей за стенами** (опция): невидимые игроку сущности не отправляются, ESP-читы их не видят
- **Порядок в папке сервера**: `config/`, `data/`, `worlds/`, `logs/` вместо тридцати файлов в корне
- **Инструменты админа**: `/watermc inspect`, `/watermc drain`, watchdog плагинов и Netty, аудит безопасности, который называет плагин за каждой выдачей OP или командой от консоли
- **WaterScript**: встроенные скрипты с событиями, командами, таймерами и переменными
- **Исправления**: дюп через товар жителя (1.21.6+), портал и платформа Края игнорировали отмену `BlockDestroyEvent`

## 📈 Бенчмарки
AMD FX-4100 (4 потока), одинаковый сид, ядра запускались по очереди. Сырые логи в [`benchmarks/`](../../benchmarks).

| | Leaf / до правки | WaterMc |
|---|---|---|
| Загрузчик, `java -jar` → main-класс сервера | 3,8–4,4 с | **0,7–0,8 с** |
| Генерация новых чанков (256 чанков) | 2,09 чанка/с | **2,64 чанка/с** |
| Выбор точки спавна, новый мир | 23–65 с | **9–15 с** |
| Толкание, толпа из 600 мобов (доля основного потока) | 21–25% | **10–11%** |
| Куча после запуска и GC | 224 МиБ | **214 МиБ** |

## 📥 Скачать
Свежая сборка в [GitHub Releases](https://github.com/s0nd3r-py/WaterMc/releases/latest). При первом запуске jar скачивает ванильный сервер с серверов Mojang и накладывает WaterMc поверх, как это делают Paper и Leaf.

```bash
java -Xms4G -Xmx4G -jar watermc-paperclip-1.21.11.jar --nogui
```

## 📄 Документация
- [Чем WaterMc отличается от Leaf](../../docs/about-watermc.md)
- [Новые ключи конфига в этом релизе](../../docs/release-description.md)
- [WaterScript](../../docs/scripting.md)
- [Флаги JVM](../../docs/jvm-guide.md)

## 📦 Сборка
Изменения Minecraft хранятся патчами против Leaf, кода Mojang в репозитории нет. Нужны JDK 21 и Git.

```bash
./scripts/setup-source.sh      # Windows: .\scripts\setup-source.ps1
./gradlew :watermc-server:createMojmapPaperclipJar
```

## ⚖️ Лицензия
[GPL-3.0](../../LICENSE.md). Код из других проектов сохраняет свою лицензию и авторство: [`licenses/`](../../licenses), [`docs/inherited-attribution.md`](../../docs/inherited-attribution.md).
