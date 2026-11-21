package org.bukkit.craftbukkit;

/**
 * Совместимый делегат для плагинов и скриптов, которые обращаются к {@code org.bukkit.craftbukkit.Main}.
 * Вся логика запуска находится в {@link me.s0nd3r.watermc.WaterMain}.
 */
public class Main {
    public static final java.time.Instant BOOT_TIME = java.time.Instant.now(); // Paper - track initial start time
    public static boolean useJline = true;
    public static boolean useConsole = true;

    public static void main(String[] args) {
        me.s0nd3r.watermc.WaterMain.main(args);
    }
}
