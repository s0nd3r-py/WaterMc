package me.s0nd3r.watermc.diagnostics;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Плавный вывод сервера из работы ({@code /watermc drain [host:port]}).
 *
 * <ol>
 *     <li>Новые входы отклоняются ({@link #isDraining()} проверяется в {@code PlayerList.canPlayerLogin}).</li>
 *     <li>Онлайн-игроки переводятся на резервный узел пакетом {@code ClientboundTransferPacket}
 *     ({@code Player#transfer}); без адреса — отключаются с сообщением о перезапуске.</li>
 *     <li>Когда игроки вышли (или истёк таймаут), дожидаемся записи данных игроков и {@code data/*.wdb}
 *     и вызываем штатную остановку — она сохраняет миры и ждёт RegionFile I/O.</li>
 * </ol>
 * Целевой узел должен принимать переводы ({@code accepts-transfers=true} в его server.properties).
 */
public final class WaterDrain {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    public static final String RESTART_MESSAGE = "Server is restarting, please reconnect shortly.";
    private static volatile boolean draining;

    private WaterDrain() {
    }

    public static boolean isDraining() {
        return draining;
    }

    public record Target(String host, int port) {
        /** {@code host}, {@code host:port} или {@code [ipv6]:port}; порт по умолчанию 25565. */
        public static Target parse(final String value) {
            final String text = value.trim();
            if (text.isEmpty()) {
                throw new IllegalArgumentException("empty address");
            }
            final String host;
            String portText = null;
            if (text.startsWith("[")) {
                final int end = text.indexOf(']');
                if (end < 0) {
                    throw new IllegalArgumentException("bad IPv6 address: " + value);
                }
                host = text.substring(1, end);
                if (end + 1 < text.length()) {
                    if (text.charAt(end + 1) != ':') {
                        throw new IllegalArgumentException("bad address: " + value);
                    }
                    portText = text.substring(end + 2);
                }
            } else {
                final int colon = text.lastIndexOf(':');
                if (colon >= 0 && text.indexOf(':') == colon) {
                    host = text.substring(0, colon);
                    portText = text.substring(colon + 1);
                } else {
                    host = text;
                }
            }
            final int port = portText == null ? 25565 : Integer.parseInt(portText);
            if (host.isEmpty() || port < 1 || port > 65535) {
                throw new IllegalArgumentException("bad address: " + value);
            }
            return new Target(host, port);
        }
    }

    /** Переводит или отключает каждого игрока; возвращает число переведённых. */
    public static <P> int moveAll(final Iterable<P> players, final @Nullable Target target, final BiConsumer<P, Target> transfer, final Consumer<P> kick) {
        int moved = 0;
        for (final P player : players) {
            if (target != null) {
                transfer.accept(player, target);
                moved++;
            } else {
                kick.accept(player);
            }
        }
        return moved;
    }

    /** Запускает drain; вызывается в основном потоке. */
    public static synchronized boolean start(final MinecraftServer server, final @Nullable Target target, final long timeoutMillis) {
        if (draining) {
            return false;
        }
        draining = true;
        final java.util.List<ServerPlayer> players = java.util.List.copyOf(server.getPlayerList().getPlayers());
        final int moved = moveAll(players, target,
            (player, t) -> player.getBukkitEntity().transfer(t.host(), t.port()),
            player -> player.getBukkitEntity().kick(net.kyori.adventure.text.Component.text(RESTART_MESSAGE), org.bukkit.event.player.PlayerKickEvent.Cause.RESTART_COMMAND));
        LOGGER.info("Drain: new logins closed, players transferred: {}{}", moved, target == null ? "" : " на " + target.host() + ":" + target.port());
        final long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        scheduleCheck(server, deadline);
        return true;
    }

    private static void scheduleCheck(final MinecraftServer server, final long deadline) {
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS).execute(() -> server.execute(() -> {
            if (server.getPlayerCount() > 0 && System.nanoTime() < deadline) {
                scheduleCheck(server, deadline);
                return;
            }
            LOGGER.info("Drain: {} players online, waiting for data writes, then stopping", server.getPlayerCount());
            me.s0nd3r.watermc.storage.PersistenceBarrier.beforeWorldFlush(60L, TimeUnit.SECONDS);
            server.server.shutdown();
        }));
    }

    static void resetForTests() {
        draining = false;
    }
}
