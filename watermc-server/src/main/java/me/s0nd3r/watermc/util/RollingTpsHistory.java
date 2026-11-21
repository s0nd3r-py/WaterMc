package me.s0nd3r.watermc.util;

/** One ticking writer publishes a chronological IEEE-754 mean for lock-free readers. */
public final class RollingTpsHistory {
    private final double[] values;
    private int cursor;
    private int size;
    private volatile double average = 0.1;
    public RollingTpsHistory(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        this.values = new double[capacity];
    }
    public void add(double value) {
        this.values[this.cursor] = value;
        if (++this.cursor == this.values.length) this.cursor = 0;
        if (this.size < this.values.length) ++this.size;
        double sum = 0;
        int index = this.size == this.values.length ? this.cursor : 0;
        for (int i = 0; i < this.size; ++i) {
            sum += this.values[index];
            if (++index == this.values.length) index = 0;
        }
        this.average = sum / this.size;
    }
    public double average() { return this.average; }
}
