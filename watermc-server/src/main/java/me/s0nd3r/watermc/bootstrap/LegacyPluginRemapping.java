package me.s0nd3r.watermc.bootstrap;

/**
 * Переотображение плагинов (Spigot-маппинги → Mojang) и преобразование байткода старых плагинов.
 *
 * <p>Включено по умолчанию, как в Paper: плагины, собранные под Spigot, работают без настройки. Повторный запуск
 * с теми же плагинами почти бесплатен: маппинги не читаются, пока ни одному плагину не нужно переотображение,
 * а неизменённые jar не перечитываются (кэш хэшей по размеру и времени изменения).</p>
 *
 * <p>Отключение: {@code -Dwatermc.enable-legacy-remapper=false} или прежний флаг Paper
 * {@code -Dpaper.disablePluginRemapping=true}.</p>
 */
public final class LegacyPluginRemapping {
    private LegacyPluginRemapping() {}
    public static boolean enabled() {
        return Boolean.parseBoolean(System.getProperty("watermc.enable-legacy-remapper", "true")) && !Boolean.getBoolean("paper.disablePluginRemapping");
    }
}
