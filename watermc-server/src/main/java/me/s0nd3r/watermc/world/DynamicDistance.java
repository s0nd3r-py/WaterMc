package me.s0nd3r.watermc.world;

import me.s0nd3r.watermc.core.config.modules.opt.DynamicDistanceConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

/**
 * Lowers simulation distance, then view distance, one step at a time while the server is over its MSPT target, and
 * restores them (view first) once it has headroom again. Idea from ServerCore "dynamic" (Wesley1808, MIT),
 * reimplemented. Runs on the main thread from {@code MinecraftServer.tickServer}.
 */
public final class DynamicDistance {
    private static int maxView = -1;
    private static int maxSim = -1;

    private DynamicDistance() {
    }

    public static void tick(final MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) {
            return;
        }
        final PlayerList players = server.getPlayerList();
        if (!DynamicDistanceConfig.enabled) {
            if (maxView > 0) { // switched off by a reload: give back the configured distances
                players.setViewDistance(maxView);
                players.setSimulationDistance(maxSim);
                maxView = maxSim = -1;
            }
            return;
        }
        if (maxView < 0) {
            maxView = players.getViewDistance();
            maxSim = players.getSimulationDistance();
        }
        if ((server.getTickCount() / 20) % DynamicDistanceConfig.intervalSeconds != 0) {
            return;
        }
        final int view = players.getViewDistance();
        final int sim = players.getSimulationDistance();
        final int[] next = step(server.getCurrentSmoothedTickTime(), DynamicDistanceConfig.targetMspt, view, sim,
            DynamicDistanceConfig.minViewDistance, DynamicDistanceConfig.minSimulationDistance, maxView, maxSim);
        if (next[0] != view) {
            players.setViewDistance(next[0]);
        }
        if (next[1] != sim) {
            players.setSimulationDistance(next[1]);
        }
    }

    /** One step: {view, sim}. Over target+5 ms: sim down, then view. Under target-5 ms: view up, then sim. */
    static int[] step(final double mspt, final double target, final int view, final int sim,
                      final int minView, final int minSim, final int maxView, final int maxSim) {
        if (mspt > target + 5.0D) {
            if (sim > minSim) {
                return new int[] {view, sim - 1};
            }
            if (view > minView) {
                return new int[] {view - 1, sim};
            }
        } else if (mspt < Math.max(target - 5.0D, 2.0D)) {
            if (view < maxView) {
                return new int[] {view + 1, sim};
            }
            if (sim < maxSim) {
                return new int[] {view, sim + 1};
            }
        }
        return new int[] {view, sim};
    }
}
