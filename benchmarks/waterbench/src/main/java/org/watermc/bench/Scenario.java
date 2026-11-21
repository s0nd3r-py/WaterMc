package org.watermc.bench;

/**
 * A reproducible load scenario. {@link #tick()} is called once per server tick on the main thread.
 */
interface Scenario {

    String name();

    /** One-time preparation (world creation, chunk tickets). Ticks spent here are never counted. */
    void prepare();

    /**
     * Advances the scenario.
     *
     * @return what the recorder should do with the current tick
     */
    TickResult tick();

    /** Removes everything the scenario created. */
    void cleanup();

    enum TickResult {
        /** Measured tick. */
        COUNT,
        /** Setup work (building, teleporting); excluded from statistics. */
        SKIP,
        /** Scenario is done; this tick is not counted. */
        FINISHED
    }
}
