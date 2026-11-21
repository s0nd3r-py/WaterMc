package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class ExplosionPacketCoalescing extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.NETWORK.getBaseKeyName() + ".explosion-packet-limit";
    }

    public static boolean enabled = false;
    public static int maxPacketsPerTick = 8;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, """
            [WaterMc] Лимит визуальных пакетов взрывов на игрока за тик.
            Пакеты с отдачей всегда передаются сразу и без изменений, даже сверх лимита.
            Для остальных пакетов сверх лимита пропускаются звук, частицы и обломки.
            Урон и разрушение блоков выполняются сервером как прежде.""");
        maxPacketsPerTick = Math.max(1, config.getInt(getBasePath() + ".max-packets-per-player-per-tick", maxPacketsPerTick));
    }
}
