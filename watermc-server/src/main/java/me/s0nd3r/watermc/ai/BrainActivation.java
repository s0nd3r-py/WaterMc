package me.s0nd3r.watermc.ai;

/** Owner-confined state for an exponentially increasing interval while a mob is unseen. */
public final class BrainActivation {
    private BrainActivation() {}

    public static int interval(int unseenTicks, int stepTicks, int maximum) {
        maximum = Math.max(1, maximum);
        int exponent = Math.max(0, unseenTicks) / Math.max(1, stepTicks);
        return exponent >= 31 ? maximum : Math.min(maximum, 1 << exponent);
    }

    public static boolean shouldTick(int tick, int entityId, int interval) {
        return interval <= 1 || ((long) tick + entityId) % interval == 0;
    }

    public static final class State {
        private boolean initialised;
        private boolean visible = true;
        private int nextCheck;
        private int unseenTicks;

        public boolean needsVisibilityCheck(int tick) {
            return !this.initialised || tick - this.nextCheck >= 0;
        }

        public void observe(boolean visible, int tick, int checkInterval) {
            this.visible = visible;
            this.initialised = true;
            this.nextCheck = tick + Math.max(1, Math.min(20, checkInterval));
            if (visible) this.unseenTicks = 0;
        }

        public int tick(int stepTicks, int maximum) {
            if (this.visible) this.unseenTicks = 0;
            else if (this.unseenTicks < Integer.MAX_VALUE) ++this.unseenTicks;
            return interval(this.unseenTicks, stepTicks, maximum);
        }

        public void reset() {
            this.initialised = false;
            this.visible = true;
            this.unseenTicks = 0;
        }
    }
}
