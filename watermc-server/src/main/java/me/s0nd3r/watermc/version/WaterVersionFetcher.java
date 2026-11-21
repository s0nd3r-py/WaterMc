package me.s0nd3r.watermc.version;

import com.destroystokyo.paper.util.VersionFetcher;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Локальная версия WaterMc без сравнения её коммитов с сервисом обновлений Leaf. */
public final class WaterVersionFetcher implements VersionFetcher {
    @Override
    public long getCacheTime() {
        return 720_000;
    }

    @Override
    public Component getVersionMessage() {
        return Component.text("WaterMc " + ServerBuildInfo.buildInfo().asString(
            ServerBuildInfo.StringRepresentation.VERSION_FULL)
            + " (локальная сборка; сервис обновлений не настроен)", NamedTextColor.YELLOW);
    }
}
