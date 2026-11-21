package me.s0nd3r.watermc.bootstrap;

import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import io.papermc.paper.util.MappingEnvironment;
import io.papermc.paper.util.ObfHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Загрузка таблицы Spigot↔Mojang имён ({@link ObfHelper}) только когда она кому-то нужна.
 *
 * <p>Paper разбирал файл маппингов на каждом запуске «для расшифровки stacktrace». Но WaterMc работает
 * в именах Mojang, и stacktrace уже читаемые. Таблица нужна только Bukkit-плагинам, собранным под Spigot:
 * их reflection-вызовы с именами Spigot переводятся на лету. Поэтому:</p>
 * <ul>
 *     <li>нет таких плагинов — таблица не загружается вовсе: меньше времени запуска и памяти;</li>
 *     <li>есть — загружается в фоновом потоке с низким приоритетом, не задерживая запуск и не вызывая
 *     паузы при первом обращении плагина.</li>
 * </ul>
 * <p>Если плагин появится позже ({@code /reload}, загрузка на лету), таблица загрузится при первом обращении,
 * как в Paper.</p>
 */
public final class ObfMappingsPreload {

    private static volatile boolean scheduled;

    private ObfMappingsPreload() {
    }

    public static void scheduleIfNeeded() {
        if (scheduled || !MappingEnvironment.hasMappings() || !needsMappings(Bukkit.getPluginManager().getPlugins())) {
            return;
        }
        scheduled = true;
        final Thread thread = new Thread(() -> ObfHelper.INSTANCE.getClass(), "WaterMc Mappings Preload");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /** Нужна ли таблица: есть ли хоть один плагин без {@code paper-plugin.yml} (то есть собранный под Spigot). */
    static boolean needsMappings(final Plugin[] plugins) {
        if (!LegacyPluginRemapping.enabled()) {
            return false;
        }
        for (final Plugin plugin : plugins) {
            if (!(plugin.getPluginMeta() instanceof PaperPluginMeta)) {
                return true;
            }
        }
        return false;
    }
}
