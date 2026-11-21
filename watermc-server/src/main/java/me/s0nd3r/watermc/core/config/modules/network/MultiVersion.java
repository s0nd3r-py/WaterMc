package me.s0nd3r.watermc.core.config.modules.network;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/**
 * Built-in multi-version support (bundled ViaVersion + ViaBackwards). Named "multi-version" because the key
 * {@code network.protocol-support} already belongs to the mod-protocol settings (Jade, AppleSkin, Xaero ...).
 */
public class MultiVersion extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.NETWORK.getBaseKeyName() + ".multi-version";
    }

    public static boolean enabled = false;
    public static String minVersion = "";
    public static String blockedMessage = "This server supports Minecraft %s and newer.";

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        enabled = config.getBoolean(base + ".enabled", enabled, config.pickStringRegionBased("""
            Let players join with other Minecraft versions without plugins: the server loads the built-in
            ViaVersion and ViaBackwards (GPL-3.0) as ordinary plugins. If plugins/ contains your own ViaVersion,
            that copy is used and the built-in one stays off. Takes effect after a restart.""", """
            允许其他版本的玩家加入 (内置 ViaVersion 和 ViaBackwards)."""));
        String version = config.getString(base + ".min-version", minVersion, config.pickStringRegionBased("""
            Oldest client version allowed, e.g. 1.20.5. Empty = every version ViaVersion/ViaBackwards support.
            Written into plugins/ViaVersion/config.yml (block-versions) on every start.""", """
            允许的最低客户端版本, 例如 1.20.5. 留空 = 全部."""));
        if (!me.s0nd3r.watermc.bootstrap.BundledPlugins.validVersion(version)) {
            me.s0nd3r.watermc.core.config.WaterConfig.LOGGER.warn("Ignoring {}.min-version '{}': expected a version like 1.20.5", base, version);
            version = "";
        }
        minVersion = version;
        final String message = config.getString(base + ".blocked-message", blockedMessage, "Kick message for older clients; %s = min-version.");
        blockedMessage = message.contains("%s") && !minVersion.isBlank() ? message.formatted(minVersion) : message; // empty min-version blocks nothing
    }
}
