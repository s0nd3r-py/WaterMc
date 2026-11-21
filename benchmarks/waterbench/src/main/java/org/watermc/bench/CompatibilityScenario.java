package org.watermc.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

/** Проверяет механики через публичный API в отдельном тестовом мире. */
final class CompatibilityScenario implements Scenario {
    private final Plugin plugin;
    private final Listener listener = new Listener() {};
    private final List<String> taskOrder = new ArrayList<>();
    private final List<String> eventOrder = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private World world;
    private UUID worldId;
    private UUID standId;
    private Mob mob;
    private NamespacedKey key;
    private int tick;
    private int repeats;
    private int redstoneEvents;
    private boolean cancelledTaskRan;

    CompatibilityScenario(Plugin plugin) { this.plugin = plugin; }
    @Override public String name() { return "compat"; }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @Override
    public void prepare() {
        require(Bukkit.isPrimaryThread(), "Подготовка вне главного потока");
        this.world = BenchWorld.get();
        this.worldId = this.world.getUID();
        require(this.world.getPlayers().isEmpty(), "Тест сохранения требует мир без игроков");
        this.key = new NamespacedKey(this.plugin, "compat-saved-value");
        this.world.getChunkAt(8, 8);
        this.world.addPluginChunkTicket(8, 8, this.plugin);
        // Площадка полностью внутри одного чанка: 128..143 по X/Z.
        for (int x = 128; x < 144; ++x) {
            for (int z = 128; z < 144; ++z) {
                this.world.getBlockAt(x, 63, z).setType(Material.STONE, false);
                for (int y = 64; y <= 70; ++y) this.world.getBlockAt(x, y, z).setType(Material.AIR, false);
            }
        }
        setupEvents();
        for (String name : List.of("a", "b", "c")) {
            this.tasks.add(Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                require(Bukkit.isPrimaryThread(), "Синхронная задача вне главного потока");
                this.taskOrder.add(name);
            }, 1));
        }
        BukkitTask cancelled = Bukkit.getScheduler().runTaskLater(this.plugin,
            () -> this.cancelledTaskRan = true, 2);
        cancelled.cancel();
        this.tasks.add(cancelled);
        this.tasks.add(new BukkitRunnable() {
            @Override public void run() { if (++repeats == 3) cancel(); }
        }.runTaskTimer(this.plugin, 1, 2));

        for (int x = 129; x <= 132; ++x) this.world.getBlockAt(x, 64, 128).setType(Material.REDSTONE_WIRE);
        this.world.getBlockAt(133, 64, 128).setType(Material.REDSTONE_LAMP);
        this.world.getBlockAt(128, 64, 128).setType(Material.REDSTONE_BLOCK);
        this.world.getBlockAt(136, 64, 134).setType(Material.WATER);
        this.world.getBlockAt(136, 68, 136).setType(Material.GLOWSTONE);

        this.world.getBlockAt(129, 64, 136).setType(Material.CHEST);
        Chest chest = (Chest) this.world.getBlockAt(129, 64, 136).getState();
        chest.getPersistentDataContainer().set(this.key, PersistentDataType.LONG, 20261108L);
        require(chest.update(), "Не обновлён сундук");
        this.world.getBlockAt(128, 64, 136).setType(Material.HOPPER);
        Directional data = (Directional) this.world.getBlockAt(128, 64, 136).getBlockData();
        data.setFacing(BlockFace.EAST);
        this.world.getBlockAt(128, 64, 136).setBlockData(data);
        ((Hopper) this.world.getBlockAt(128, 64, 136).getState()).getInventory().addItem(new ItemStack(Material.DIAMOND, 7));

        ArmorStand stand = (ArmorStand) this.world.spawnEntity(new Location(this.world, 140.5, 64, 140.5), EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setPersistent(true);
        stand.setCustomName("waterbench-persistence");
        this.standId = stand.getUniqueId();
        this.mob = (Mob) this.world.spawnEntity(new Location(this.world, 130.5, 64, 142.5), EntityType.ZOMBIE);
        this.mob.setRemoveWhenFarAway(false);
        var nearby = this.world.getNearbyEntities(new BoundingBox(128, 63, 138, 144, 68, 143));
        require(nearby.contains(stand) && nearby.contains(this.mob), "Пространственный запрос потерял сущности");
        require(nearby.stream().map(Entity::getUniqueId).distinct().count() == nearby.size(), "Запрос вернул дубликаты");
    }

    private void setupEvents() {
        var manager = Bukkit.getPluginManager();
        manager.registerEvent(ProbeEvent.class, this.listener, EventPriority.LOWEST, (listener, event) -> {
            require(Bukkit.isPrimaryThread(), "Синхронное событие вне главного потока");
            this.eventOrder.add("lowest");
        }, this.plugin);
        manager.registerEvent(ProbeEvent.class, this.listener, EventPriority.NORMAL, (listener, event) -> {
            this.eventOrder.add("normal");
            ((ProbeEvent) event).setCancelled(true);
        }, this.plugin);
        manager.registerEvent(ProbeEvent.class, this.listener, EventPriority.HIGH,
            (listener, event) -> this.eventOrder.add("must-be-skipped"), this.plugin, true);
        manager.registerEvent(ProbeEvent.class, this.listener, EventPriority.MONITOR,
            (listener, event) -> this.eventOrder.add("monitor:" + ((ProbeEvent) event).isCancelled()), this.plugin);
        manager.callEvent(new ProbeEvent());
        require(this.eventOrder.equals(List.of("lowest", "normal", "monitor:true")), "Нарушен порядок или отмена событий: " + this.eventOrder);
        manager.registerEvent(BlockRedstoneEvent.class, this.listener, EventPriority.MONITOR, (listener, event) -> {
            if (((BlockRedstoneEvent) event).getBlock().getWorld() == this.world) {
                require(Bukkit.isPrimaryThread(), "Редстоун-событие вне главного потока");
                ++this.redstoneEvents;
            }
        }, this.plugin);
    }

    @Override
    public TickResult tick() {
        require(Bukkit.isPrimaryThread(), "Проверка вне главного потока");
        ++this.tick;
        if (this.tick == 20) {
            // Сразу после spawn моб ещё не прошёл физику и не имеет onGround.
            var path = this.mob.getPathfinder().findPath(new Location(this.world, 138.5, 64, 142.5));
            require(path != null && path.canReachFinalPoint(), "Не построен путь по площадке: onGround="
                + this.mob.isOnGround() + ", position=" + this.mob.getLocation() + ", result=" + path);
        }
        if (this.tick == 40) {
            require(this.taskOrder.equals(List.of("a", "b", "c")), "Нарушен FIFO задач: " + this.taskOrder);
            require(!this.cancelledTaskRan, "Отменённая задача выполнена");
            require(this.repeats == 3, "Неверное выполнение повторяющейся задачи");
            require(((Lightable) this.world.getBlockAt(133, 64, 128).getBlockData()).isLit(), "Лампа не включилась");
            require(this.redstoneEvents > 0, "Не было событий редстоуна");
            require(this.world.getBlockAt(137, 64, 134).getType() == Material.WATER, "Не выполнился запланированный тик жидкости");
            require(this.world.getBlockAt(137, 68, 136).getLightFromBlocks() == 14, "Неверное распространение света");
        }
        if (this.tick == 60) {
            this.world.getBlockAt(128, 64, 128).setType(Material.AIR);
            this.world.getBlockAt(136, 68, 136).setType(Material.AIR);
        }
        if (this.tick == 100) {
            require(!((Lightable) this.world.getBlockAt(133, 64, 128).getBlockData()).isLit(), "Лампа не выключилась после задержки");
            require(this.world.getBlockAt(137, 68, 136).getLightFromBlocks() == 0, "Свет сохранился после удаления источника");
            verifyInventory();
            this.mob.remove();
        }
        if (this.tick == 140) {
            // Запланированная жидкость должна продолжить тикать после save/unload/reload.
            this.world.getBlockAt(136, 66, 136).setType(Material.WATER);
            this.world.save();
            this.world.removePluginChunkTicket(8, 8, this.plugin);
            require(Bukkit.unloadWorld(this.world, true), "Мир не выгружен");
            this.world = new WorldCreator(BenchWorld.NAME).type(WorldType.FLAT).seed(20261108L).generateStructures(false).createWorld();
            require(this.world != null && this.world.getUID().equals(this.worldId), "Не восстановлен тот же мир");
            this.world.getChunkAt(8, 8);
            this.world.addPluginChunkTicket(8, 8, this.plugin);
            verifyInventory();
            Entity restored = this.world.getEntity(this.standId);
            require(restored instanceof ArmorStand && "waterbench-persistence".equals(restored.getCustomName()), "Не восстановлена сущность по UUID");
            Chest chest = (Chest) this.world.getBlockAt(129, 64, 136).getState();
            require(Long.valueOf(20261108L).equals(chest.getPersistentDataContainer().get(this.key, PersistentDataType.LONG)), "Потерян PDC block entity");
        }
        if (this.tick == 200) {
            require(this.world.getBlockAt(136, 65, 136).getType() == Material.WATER, "Не восстановлены scheduled ticks жидкости");
            this.plugin.getLogger().info("COMPAT_CHECKS scheduler_fifo=true cancellation=true repeating=true event_order=true event_cancellation=true redstone=true fluid_ticks=true lighting=true hopper=true pathfinding=true spatial_query=true save_reload=true entity_uuid=true block_entity_pdc=true saved_ticks=true");
            return TickResult.FINISHED;
        }
        return TickResult.COUNT;
    }

    private void verifyInventory() {
        Chest chest = (Chest) this.world.getBlockAt(129, 64, 136).getState();
        int diamonds = chest.getInventory().all(Material.DIAMOND).values().stream().mapToInt(ItemStack::getAmount).sum();
        require(diamonds == 7, "Воронка или сохранение инвентаря потеряли предметы: " + diamonds);
        Hopper hopper = (Hopper) this.world.getBlockAt(128, 64, 136).getState();
        require(hopper.getInventory().isEmpty(), "Воронка не передала все предметы");
    }

    @Override
    public void cleanup() {
        for (BukkitTask task : this.tasks) task.cancel();
        HandlerList.unregisterAll(this.listener);
        if (this.mob != null && this.mob.isValid()) this.mob.remove();
        if (this.world != null) this.world.removePluginChunkTicket(8, 8, this.plugin);
    }

    public static final class ProbeEvent extends Event implements Cancellable {
        private static final HandlerList HANDLERS = new HandlerList();
        private boolean cancelled;
        @Override public boolean isCancelled() { return this.cancelled; }
        @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
        @Override public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }
}
