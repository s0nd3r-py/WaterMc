package me.s0nd3r.watermc.network;

/** Per-player, owner-thread-only budget. Rejected packets consume no allowance. */
public final class ParticleBudget {
    private boolean initialized;
    private int tick;
    private int packets;
    private long particles;

    public boolean tryConsume(int currentTick, int count, int packetLimit, int particleLimit) {
        if (!this.initialized || this.tick != currentTick) {
            this.initialized = true;
            this.tick = currentTick;
            this.packets = 0;
            this.particles = 0;
        }
        // Protocol count=0 creates one directional particle.
        long cost = Math.max(1, count);
        if (this.packets >= packetLimit || cost > (long) particleLimit - this.particles) {
            return false;
        }
        ++this.packets;
        this.particles += cost;
        return true;
    }
}
