package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class PoiSearchBackoffConfig extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".poi-search-backoff";
    }

    public static boolean enabled = true;
    public static int maxDelayTicks = 100;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, """
            [WaterMc] Отступ после неудачного поиска POI жителями (кровати, рабочие места).
            Повторные поиски без изменений POI в мире откладываются до max-delay-ticks.
            Появление, исчезновение или освобождение любой POI сразу сбрасывает отступ.""");
        maxDelayTicks = Math.max(0, config.getInt(getBasePath() + ".max-delay-ticks", maxDelayTicks));
    }
}
