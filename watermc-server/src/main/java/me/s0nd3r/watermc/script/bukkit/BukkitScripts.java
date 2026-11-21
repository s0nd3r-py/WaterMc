package me.s0nd3r.watermc.script.bukkit;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import me.s0nd3r.watermc.script.GlobalStore;
import me.s0nd3r.watermc.script.ScriptEngine;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.s0nd3r.watermc.bootstrap.ServerLayout;

/** Подключение скриптового движка к серверу: файлы {@code scripts/}, события, команды, перезагрузка. */
public final class BukkitScripts {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc Scripts");
    public static final String SCRIPT_EXTENSION = ".wsc";
    public static final String COMMAND_PREFIX = "scripts";

    public static volatile boolean enabled = true;
    public static volatile long stepLimit = ScriptEngine.DEFAULT_STEP_LIMIT;

    private static @Nullable ScriptEngine engine;
    private static final Listener LISTENER = new Listener() {
    };
    private static final List<Command> REGISTERED = new ArrayList<>();
    private static ScriptEngine.@Nullable LoadResult lastResult;

    private BukkitScripts() {
    }

    private static Plugin owner() {
        return me.s0nd3r.watermc.integration.plugin.MinecraftInternalPlugin.INSTANCE;
    }

    public static Path scriptsDirectory() {
        return ServerLayout.current().root().resolve("scripts");
    }

    public static @Nullable ScriptEngine engine() {
        return engine;
    }

    public static ScriptEngine.@Nullable LoadResult lastResult() {
        return lastResult;
    }

    /** Старт сервера: вызывается после включения плагинов. */
    public static synchronized void start() {
        if (!enabled || engine != null) {
            return;
        }
        try {
            final GlobalStore globals = GlobalStore.open(ServerLayout.current().dataFile("script-variables.wdb"));
            engine = new ScriptEngine(new BukkitScriptHost(owner()), globals, stepLimit);
        } catch (final IOException ex) {
            LOGGER.error("Could not open data/script-variables.wdb; scripts are disabled", ex);
            return;
        }
        ensureDirectory();
        load(false);
    }

    /**
     * Остановка сервера: on unload, сохранение $-переменных. Никогда не бросает исключений — сбой скриптов
     * не должен прерывать сохранение игроков и миров, которое идёт следом.
     */
    public static synchronized void stop() {
        if (engine == null) {
            return;
        }
        final ScriptEngine current = engine;
        engine = null;
        try {
            unregister();
        } catch (final RuntimeException ex) {
            LOGGER.error("Could not unregister script commands and events", ex);
        }
        try {
            current.unload();
        } catch (final RuntimeException ex) {
            LOGGER.error("Error while unloading scripts", ex);
        }
        try {
            current.globals().close();
        } catch (final IOException | RuntimeException ex) {
            LOGGER.error("Could not close data/script-variables.wdb", ex);
        }
    }

    /** {@code /watermc script reload}: выгрузить и загрузить все скрипты заново. */
    public static synchronized ScriptEngine.@Nullable LoadResult reload() {
        if (engine == null) {
            start();
            return lastResult;
        }
        unregister();
        engine.unload();
        return load(true);
    }

    private static ScriptEngine.LoadResult load(final boolean syncToPlayers) {
        final Map<String, String> sources = readSources(scriptsDirectory());
        final ScriptEngine.LoadResult result = engine.load(sources);
        lastResult = result;
        registerListeners(result.events());
        registerCommands(result.commands());
        if (syncToPlayers) {
            ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).syncCommands();
        }
        for (final String error : result.errors()) {
            LOGGER.warn("[scripts] {}", error);
        }
        if (!sources.isEmpty()) {
            LOGGER.info("Scripts: {} loaded, {} with errors, {} commands, {} events", result.filesLoaded(), result.filesFailed(), result.commands().size(), result.events().size());
        }
        return result;
    }

    static Map<String, String> readSources(final Path dir) {
        final Map<String, String> sources = new TreeMap<>();
        if (!Files.isDirectory(dir)) {
            return sources;
        }
        try (Stream<Path> files = Files.walk(dir, 8)) {
            files.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(SCRIPT_EXTENSION))
                .filter(p -> !disabled(dir.relativize(p)))
                .sorted()
                .forEach(p -> {
                    try {
                        sources.put(dir.relativize(p).toString().replace('\\', '/'), Files.readString(p, StandardCharsets.UTF_8));
                    } catch (final IOException ex) {
                        LOGGER.warn("Could not read {}", p, ex);
                    }
                });
        } catch (final IOException ex) {
            LOGGER.warn("Could not read directory {}", dir, ex);
        }
        return sources;
    }

    /** Файлы и каталоги, начинающиеся с '_', отключены (например, {@code _example.wsc}). */
    static boolean disabled(final Path relative) {
        for (final Path part : relative) {
            if (part.toString().startsWith("_")) {
                return true;
            }
        }
        return false;
    }

    private static void ensureDirectory() {
        final Path dir = scriptsDirectory();
        if (Files.isDirectory(dir)) {
            return;
        }
        try {
            Files.createDirectories(dir);
            try (var in = BukkitScripts.class.getResourceAsStream("/watermc/scripts/_example.wsc")) {
                if (in != null) {
                    Files.copy(in, dir.resolve("_example.wsc"));
                }
            }
        } catch (final IOException ex) {
            LOGGER.warn("Could not create the scripts/ directory", ex);
        }
    }

    // ---------- события ----------

    private static void registerListeners(final java.util.Set<String> events) {
        HandlerList.unregisterAll(LISTENER);
        final ScriptEngine e = engine;
        if (e == null) {
            return;
        }
        if (events.contains("join")) {
            listen(PlayerJoinEvent.class, false, ev -> e.dispatch("join", new Object[]{wrapPlayer(ev.getPlayer())}, null));
        }
        if (events.contains("quit")) {
            listen(PlayerQuitEvent.class, false, ev -> e.dispatch("quit", new Object[]{wrapPlayer(ev.getPlayer())}, null));
        }
        if (events.contains("break")) {
            listen(BlockBreakEvent.class, true, ev -> e.dispatch("break",
                new Object[]{wrapPlayer(ev.getPlayer()), new ScriptObjects.BlockObject(ev.getBlock())}, () -> ev.setCancelled(true)));
        }
        if (events.contains("place")) {
            listen(BlockPlaceEvent.class, true, ev -> e.dispatch("place",
                new Object[]{wrapPlayer(ev.getPlayer()), new ScriptObjects.BlockObject(ev.getBlockPlaced())}, () -> ev.setCancelled(true)));
        }
        if (events.contains("death")) {
            listen(PlayerDeathEvent.class, false, ev -> {
                final Player killer = ev.getPlayer().getKiller();
                e.dispatch("death", new Object[]{wrapPlayer(ev.getPlayer()), killer == null ? null : wrapPlayer(killer)}, null);
            });
        }
        if (events.contains("respawn")) {
            listen(PlayerRespawnEvent.class, false, ev -> e.dispatch("respawn", new Object[]{wrapPlayer(ev.getPlayer())}, null));
        }
        if (events.contains("chat")) {
            listen(AsyncChatEvent.class, true, ev -> {
                final Player player = ev.getPlayer();
                final String message = PlainTextComponentSerializer.plainText().serialize(ev.message());
                if (dispatchOnMainThread(e, "chat", new Object[]{wrapPlayer(player), message})) {
                    ev.setCancelled(true);
                }
            });
        }
        if (events.contains("player_command")) {
            listen(PlayerCommandPreprocessEvent.class, true, ev -> {
                final String raw = ev.getMessage();
                e.dispatch("player_command", new Object[]{wrapPlayer(ev.getPlayer()), raw.startsWith("/") ? raw.substring(1) : raw}, () -> ev.setCancelled(true));
            });
        }
        if (events.contains("damage")) {
            listen(EntityDamageEvent.class, true, ev -> {
                if (ev.getEntity() instanceof Player player) {
                    e.dispatch("damage", new Object[]{wrapPlayer(player), ev.getCause().name().toLowerCase(java.util.Locale.ROOT), ev.getDamage()},
                        () -> ev.setCancelled(true));
                }
            });
        }
        if (events.contains("interact")) {
            // Клик по воздуху приходит уже отменённым, поэтому отменённые события тоже передаются.
            listen(PlayerInteractEvent.class, false, ev -> {
                if (ev.getHand() != EquipmentSlot.HAND) {
                    return;
                }
                e.dispatch("interact", new Object[]{wrapPlayer(ev.getPlayer()),
                        ev.getClickedBlock() == null ? null : new ScriptObjects.BlockObject(ev.getClickedBlock()),
                        ev.getAction().name().toLowerCase(java.util.Locale.ROOT)},
                    () -> {
                        ev.setUseInteractedBlock(Event.Result.DENY);
                        ev.setUseItemInHand(Event.Result.DENY);
                    });
            });
        }
        if (events.contains("kill")) {
            listen(EntityDeathEvent.class, false, ev -> {
                final Player killer = ev.getEntity().getKiller();
                if (!(ev.getEntity() instanceof Player) && killer != null) {
                    e.dispatch("kill", new Object[]{wrapPlayer(killer), ev.getEntity().getType().getKey().getKey()}, null);
                }
            });
        }
        if (events.contains("world_change")) {
            listen(PlayerChangedWorldEvent.class, false, ev -> e.dispatch("world_change", new Object[]{wrapPlayer(ev.getPlayer()), ev.getFrom().getName()}, null));
        }
    }

    /** Сколько асинхронный поток чата ждёт обработчик в основном потоке. */
    private static final long CHAT_WAIT_MILLIS = Long.getLong("watermc.scripts.chat-wait-millis", 1000L);

    /**
     * Выполнить обработчики события в основном потоке и вернуть, отменили ли они его.
     *
     * <p>Чат приходит из асинхронного потока, а скрипты работают с миром только в основном. Поток чата ждёт
     * выполнения обработчиков до первого {@code wait}, но не дольше {@link #CHAT_WAIT_MILLIS}. После этого отмена
     * уже не действует: сообщение уходит как обычно, в консоль пишется предупреждение.</p>
     */
    private static boolean dispatchOnMainThread(final ScriptEngine e, final String event, final Object[] values) {
        final boolean[] cancelled = {false};
        if (Bukkit.isPrimaryThread()) {
            e.dispatch(event, values, () -> cancelled[0] = true);
            return cancelled[0];
        }
        final java.util.concurrent.Future<Boolean> result = Bukkit.getScheduler().callSyncMethod(owner(), () -> {
            e.dispatch(event, values, () -> cancelled[0] = true);
            return cancelled[0];
        });
        try {
            return result.get(CHAT_WAIT_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (final java.util.concurrent.TimeoutException ex) {
            LOGGER.warn("Script handler for {} did not finish within {} ms; cancellation was not applied", event, CHAT_WAIT_MILLIS);
            return false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final java.util.concurrent.ExecutionException ex) {
            LOGGER.warn("Error in script handler for {}", event, ex.getCause());
            return false;
        }
    }

    private static Object wrapPlayer(final Player player) {
        return new ScriptObjects.PlayerObject(player);
    }

    private static <T extends Event> void listen(final Class<T> type, final boolean ignoreCancelled, final java.util.function.Consumer<T> handler) {
        Bukkit.getPluginManager().registerEvent(type, LISTENER, EventPriority.NORMAL, (listener, event) -> {
            if (type.isInstance(event)) {
                if (ignoreCancelled && event instanceof Cancellable c && c.isCancelled()) {
                    return;
                }
                handler.accept(type.cast(event));
            }
        }, owner(), ignoreCancelled);
    }

    // ---------- команды ----------

    private static void registerCommands(final List<ScriptEngine.CommandBinding> bindings) {
        final CommandMap map = Bukkit.getCommandMap();
        for (final ScriptEngine.CommandBinding binding : bindings) {
            final ScriptCommand command = new ScriptCommand(binding);
            if (!map.register(COMMAND_PREFIX, command)) {
                LOGGER.warn("Script command /{} from {} conflicts with an existing command; available as /{}:{}", binding.name(), binding.file(), COMMAND_PREFIX, binding.name());
            }
            REGISTERED.add(command);
        }
    }

    private static void unregister() {
        HandlerList.unregisterAll(LISTENER);
        final CommandMap map = Bukkit.getCommandMap();
        final Map<String, Command> known = map.getKnownCommands();
        for (final Command command : REGISTERED) {
            command.unregister(map);
            // Карта команд Paper не поддерживает удаление через entrySet/итератор — удаляем по ключу.
            final List<String> keys = new ArrayList<>();
            for (final Map.Entry<String, Command> entry : known.entrySet()) {
                if (entry.getValue() == command) {
                    keys.add(entry.getKey());
                }
            }
            for (final String key : keys) {
                known.remove(key);
            }
        }
        REGISTERED.clear();
    }

    static final class ScriptCommand extends Command {
        private final ScriptEngine.CommandBinding binding;

        ScriptCommand(final ScriptEngine.CommandBinding binding) {
            super(binding.name(), binding.description(), binding.usage(), binding.aliases());
            this.binding = binding;
            if (binding.permission() != null) {
                this.setPermission(binding.permission());
            }
            if (binding.permissionMessage() != null) {
                this.permissionMessage(ScriptObjects.color(binding.permissionMessage()));
            }
        }

        @Override
        public boolean execute(final CommandSender sender, final String label, final String[] args) {
            if (!this.testPermission(sender)) {
                return true;
            }
            final ScriptEngine e = engine;
            if (e == null) {
                return true;
            }
            final Object senderObject = sender instanceof Player p ? new ScriptObjects.PlayerObject(p) : new ScriptObjects.SenderObject(sender);
            final Object player = sender instanceof Player ? senderObject : null;
            e.runCommand(this.binding.name(), senderObject, player, Arrays.asList(args));
            return true;
        }
    }
}
