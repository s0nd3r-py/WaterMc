package me.s0nd3r.watermc.script.bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import me.s0nd3r.watermc.script.ScriptMessageable;
import me.s0nd3r.watermc.script.ScriptObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

/** Обёртки игровых объектов для скриптов. Доступны только перечисленные здесь свойства и методы. */
public final class ScriptObjects {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final int MAX_GIVE = 64 * 36;

    private ScriptObjects() {
    }

    static Component color(final String text) {
        return LEGACY.deserialize(text);
    }

    public static @Nullable Object wrap(final @Nullable Object value) {
        if (value instanceof Player player) {
            return new PlayerObject(player);
        }
        if (value instanceof Block block) {
            return new BlockObject(block);
        }
        if (value instanceof World world) {
            return new WorldObject(world);
        }
        if (value instanceof CommandSender sender) {
            return new SenderObject(sender);
        }
        return value;
    }

    private static double num(final List<Object> args, final int i, final String method) {
        if (i >= args.size() || !(args.get(i) instanceof Double d)) {
            throw new IllegalArgumentException(method + ": аргумент " + (i + 1) + " должен быть числом");
        }
        return d;
    }

    private static String text(final List<Object> args, final int i, final String method) {
        if (i >= args.size() || args.get(i) == null) {
            throw new IllegalArgumentException(method + ": не хватает аргумента " + (i + 1));
        }
        return args.get(i) instanceof String s ? s : String.valueOf(args.get(i));
    }

    static Material material(final String name, final boolean block) {
        final Material material = Material.matchMaterial(name);
        if (material == null || material.isLegacy()) {
            throw new IllegalArgumentException("неизвестный предмет/блок '" + name + "'");
        }
        if (block ? !material.isBlock() : !material.isItem()) {
            throw new IllegalArgumentException("'" + name + "' не является " + (block ? "блоком" : "предметом"));
        }
        return material;
    }

    static String key(final Material material) {
        return material.getKey().getKey();
    }

    /** Отправитель команды, не являющийся игроком (консоль, командный блок, RCON). */
    public record SenderObject(CommandSender sender) implements ScriptObject, ScriptMessageable {
        @Override
        public String typeName() {
            return "sender";
        }

        @Override
        public Object get(final String property) {
            return switch (property) {
                case "name" -> this.sender.getName();
                case "is_player" -> this.sender instanceof Player;
                case "op" -> this.sender.isOp();
                default -> MISSING;
            };
        }

        @Override
        public Object call(final String method, final List<Object> args) {
            return switch (method) {
                case "has_permission" -> this.sender.hasPermission(text(args, 0, method));
                case "message", "send" -> {
                    this.sendMessage(text(args, 0, method));
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method);
            };
        }

        @Override
        public void sendMessage(final String message) {
            this.sender.sendMessage(color(message));
        }

        @Override
        public String display() {
            return this.sender.getName();
        }
    }

    public static final class PlayerObject implements ScriptObject, ScriptMessageable {
        private final Player player;

        PlayerObject(final Player player) {
            this.player = player;
        }

        private Player live() {
            if (!this.player.isOnline()) {
                throw new IllegalArgumentException("игрок " + this.player.getName() + " уже вышел с сервера");
            }
            return this.player;
        }

        @Override
        public String typeName() {
            return "player";
        }

        @Override
        public Object get(final String property) {
            final Player p = this.player;
            return switch (property) {
                case "name" -> p.getName();
                case "display_name" -> LEGACY.serialize(p.displayName());
                case "uuid" -> p.getUniqueId().toString();
                case "online" -> p.isOnline();
                case "op" -> p.isOp();
                case "health" -> p.getHealth();
                case "max_health" -> {
                    final var attribute = p.getAttribute(Attribute.MAX_HEALTH);
                    yield attribute == null ? 20.0 : attribute.getValue();
                }
                case "food" -> (double) p.getFoodLevel();
                case "saturation" -> (double) p.getSaturation();
                case "level" -> (double) p.getLevel();
                case "exp" -> (double) p.getExp();
                case "gamemode" -> p.getGameMode().name().toLowerCase(Locale.ROOT);
                case "world" -> new WorldObject(p.getWorld());
                case "x" -> p.getLocation().getX();
                case "y" -> p.getLocation().getY();
                case "z" -> p.getLocation().getZ();
                case "yaw" -> (double) p.getLocation().getYaw();
                case "pitch" -> (double) p.getLocation().getPitch();
                case "flying" -> p.isFlying();
                case "allow_flight" -> p.getAllowFlight();
                case "sneaking" -> p.isSneaking();
                case "ping" -> (double) p.getPing();
                case "locale" -> p.locale().toString();
                case "item" -> key(p.getInventory().getItemInMainHand().getType());
                case "block" -> new BlockObject(p.getLocation().getBlock());
                default -> MISSING;
            };
        }

        @Override
        public boolean set(final String property, final Object value) {
            final Player p = this.live();
            switch (property) {
                case "health" -> {
                    final double max = (Double) this.get("max_health");
                    p.setHealth(Math.max(0, Math.min(max, requireNumber(property, value))));
                }
                case "food" -> p.setFoodLevel((int) Math.max(0, Math.min(20, requireNumber(property, value))));
                case "saturation" -> p.setSaturation((float) Math.max(0, Math.min(20, requireNumber(property, value))));
                case "level" -> p.setLevel((int) Math.max(0, Math.min(Integer.MAX_VALUE, requireNumber(property, value))));
                case "exp" -> p.setExp((float) Math.max(0, Math.min(0.9999, requireNumber(property, value))));
                case "gamemode" -> {
                    final GameMode mode;
                    try {
                        mode = GameMode.valueOf(String.valueOf(value).toUpperCase(Locale.ROOT));
                    } catch (final IllegalArgumentException ex) {
                        throw new IllegalArgumentException("неизвестный режим игры '" + value + "' (survival, creative, adventure, spectator)");
                    }
                    p.setGameMode(mode);
                }
                case "allow_flight" -> p.setAllowFlight(Boolean.TRUE.equals(value));
                case "flying" -> {
                    if (Boolean.TRUE.equals(value) && !p.getAllowFlight()) {
                        throw new IllegalArgumentException("сначала разрешите полёт: player.allow_flight = true");
                    }
                    p.setFlying(Boolean.TRUE.equals(value));
                }
                case "display_name" -> p.displayName(color(String.valueOf(value)));
                default -> {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object call(final String method, final List<Object> args) {
            final Player p = this.live();
            return switch (method) {
                case "has_permission" -> p.hasPermission(text(args, 0, method));
                case "message", "send" -> {
                    this.sendMessage(text(args, 0, method));
                    yield null;
                }
                case "actionbar" -> {
                    p.sendActionBar(color(text(args, 0, method)));
                    yield null;
                }
                case "title" -> {
                    p.showTitle(Title.title(color(text(args, 0, method)), args.size() > 1 ? color(text(args, 1, method)) : Component.empty()));
                    yield null;
                }
                case "kick" -> {
                    p.kick(color(args.isEmpty() ? "Kicked" : text(args, 0, method)));
                    yield null;
                }
                case "teleport" -> {
                    if (!args.isEmpty() && args.get(0) instanceof PlayerObject other) {
                        yield p.teleport(other.live().getLocation());
                    }
                    final World world = args.size() > 3 ? worldNamed(text(args, 3, method)) : p.getWorld();
                    yield p.teleport(new Location(world, num(args, 0, method), num(args, 1, method), num(args, 2, method), p.getLocation().getYaw(), p.getLocation().getPitch()));
                }
                case "give" -> {
                    final Material material = material(text(args, 0, method), false);
                    final int amount = args.size() > 1 ? (int) Math.max(1, Math.min(MAX_GIVE, num(args, 1, method))) : 1;
                    int left = amount;
                    int dropped = 0;
                    while (left > 0) {
                        final int stack = Math.min(left, material.getMaxStackSize());
                        for (final ItemStack rest : p.getInventory().addItem(new ItemStack(material, stack)).values()) {
                            p.getWorld().dropItemNaturally(p.getLocation(), rest);
                            dropped += rest.getAmount();
                        }
                        left -= stack;
                    }
                    yield (double) (amount - dropped);
                }
                case "has_item" -> {
                    final Material material = material(text(args, 0, method), false);
                    final int amount = args.size() > 1 ? (int) Math.max(1, num(args, 1, method)) : 1;
                    yield p.getInventory().containsAtLeast(new ItemStack(material), amount);
                }
                case "take_item" -> {
                    final Material material = material(text(args, 0, method), false);
                    final int amount = args.size() > 1 ? (int) Math.max(1, Math.min(MAX_GIVE, num(args, 1, method))) : 1;
                    if (!p.getInventory().containsAtLeast(new ItemStack(material), amount)) {
                        yield false;
                    }
                    int left = amount;
                    while (left > 0) {
                        final int stack = Math.min(left, material.getMaxStackSize());
                        p.getInventory().removeItem(new ItemStack(material, stack));
                        left -= stack;
                    }
                    yield true;
                }
                case "clear_inventory" -> {
                    p.getInventory().clear();
                    yield null;
                }
                case "heal" -> {
                    p.setHealth((Double) this.get("max_health"));
                    p.setFoodLevel(20);
                    p.setSaturation(20);
                    p.setFireTicks(0);
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method);
            };
        }

        @Override
        public void sendMessage(final String message) {
            if (this.player.isOnline()) {
                this.player.sendMessage(color(message));
            }
        }

        @Override
        public String display() {
            return this.player.getName();
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof PlayerObject other && other.player.getUniqueId().equals(this.player.getUniqueId());
        }

        @Override
        public int hashCode() {
            return this.player.getUniqueId().hashCode();
        }
    }

    public record BlockObject(Block block) implements ScriptObject {
        @Override
        public String typeName() {
            return "block";
        }

        @Override
        public Object get(final String property) {
            return switch (property) {
                case "type" -> key(this.block.getType());
                case "x" -> (double) this.block.getX();
                case "y" -> (double) this.block.getY();
                case "z" -> (double) this.block.getZ();
                case "world" -> new WorldObject(this.block.getWorld());
                case "light" -> (double) this.block.getLightLevel();
                case "powered" -> this.block.isBlockPowered();
                case "air" -> this.block.getType().isAir();
                default -> MISSING;
            };
        }

        @Override
        public boolean set(final String property, final Object value) {
            if (property.equals("type")) {
                this.block.setType(material(String.valueOf(value), true));
                return true;
            }
            return false;
        }

        @Override
        public Object call(final String method, final List<Object> args) {
            return switch (method) {
                case "relative" -> new BlockObject(this.block.getRelative((int) num(args, 0, method), (int) num(args, 1, method), (int) num(args, 2, method)));
                case "break" -> this.block.breakNaturally();
                default -> throw new UnsupportedOperationException(method);
            };
        }

        @Override
        public String display() {
            return key(this.block.getType()) + "@" + this.block.getX() + "," + this.block.getY() + "," + this.block.getZ();
        }

        @Override
        public boolean equals(final Object o) {
            return o instanceof BlockObject other && other.block.equals(this.block);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.block.getWorld().getUID(), this.block.getX(), this.block.getY(), this.block.getZ());
        }
    }

    public record WorldObject(World world) implements ScriptObject {
        @Override
        public String typeName() {
            return "world";
        }

        @Override
        public Object get(final String property) {
            return switch (property) {
                case "name" -> this.world.getName();
                case "time" -> (double) this.world.getTime();
                case "storm" -> this.world.hasStorm();
                case "thundering" -> this.world.isThundering();
                case "players" -> {
                    final List<Object> players = new ArrayList<>();
                    for (final Player p : this.world.getPlayers()) {
                        players.add(new PlayerObject(p));
                    }
                    yield players;
                }
                case "spawn_x" -> this.world.getSpawnLocation().getX();
                case "spawn_y" -> this.world.getSpawnLocation().getY();
                case "spawn_z" -> this.world.getSpawnLocation().getZ();
                default -> MISSING;
            };
        }

        @Override
        public boolean set(final String property, final Object value) {
            switch (property) {
                case "time" -> this.world.setTime((long) requireNumber(property, value));
                case "storm" -> this.world.setStorm(Boolean.TRUE.equals(value));
                case "thundering" -> this.world.setThundering(Boolean.TRUE.equals(value));
                default -> {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object call(final String method, final List<Object> args) {
            return switch (method) {
                case "block_at" -> new BlockObject(this.world.getBlockAt((int) Math.floor(num(args, 0, method)), (int) Math.floor(num(args, 1, method)), (int) Math.floor(num(args, 2, method))));
                case "strike_lightning" -> {
                    this.world.strikeLightningEffect(new Location(this.world, num(args, 0, method), num(args, 1, method), num(args, 2, method)));
                    yield null;
                }
                default -> throw new UnsupportedOperationException(method);
            };
        }

        @Override
        public String display() {
            return this.world.getName();
        }
    }

    private static World worldNamed(final String name) {
        final World world = Bukkit.getWorld(name);
        if (world == null) {
            throw new IllegalArgumentException("мир '" + name + "' не найден");
        }
        return world;
    }

    private static double requireNumber(final String property, final Object value) {
        if (value instanceof Double d && !d.isNaN() && !d.isInfinite()) {
            return d;
        }
        throw new IllegalArgumentException(property + " должно быть числом");
    }
}
