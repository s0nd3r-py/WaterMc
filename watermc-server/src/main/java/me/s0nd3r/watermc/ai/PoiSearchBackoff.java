package me.s0nd3r.watermc.ai;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Отступ после неудачного поиска POI (кровати, рабочие места, точки сбора).
 *
 * <p>Безработный житель без свободного рабочего места каждые 20–40 тиков обходит POI в радиусе 48 блоков.
 * Если поиск ничего не нашёл, а с прошлой неудачи ни одна POI в мире не появилась, не исчезла и не
 * освободилась (глобальная версия не изменилась), следующий поиск откладывается дополнительно:
 * 20, 40, 80 … тиков, но не больше {@code maxDelayTicks}. Любое изменение POI (поставили кровать,
 * загрузился чанк с POI, житель освободил место) сбрасывает отступ — поиск сразу идёт в обычном ритме.</p>
 */
public final class PoiSearchBackoff {

    private static final AtomicLong VERSION = new AtomicLong();

    private long failVersion = -1;
    private int failures;

    /** Вызывается при любом изменении множества POI или их свободных мест. */
    public static void bump() {
        VERSION.incrementAndGet();
    }

    public static long version() {
        return VERSION.get();
    }

    /**
     * @param found        нашёл ли поиск хоть одну подходящую POI
     * @param maxDelayTicks верхняя граница дополнительной задержки; {@code <= 0} выключает отступ
     * @return дополнительная задержка в тиках
     */
    public int onSearchFinished(final boolean found, final int maxDelayTicks) {
        return this.onSearchFinished(found, maxDelayTicks, VERSION.get());
    }

    public int onSearchFinished(final boolean found, final int maxDelayTicks, final long currentVersion) {
        if (found || maxDelayTicks <= 0) {
            this.failures = 0;
            this.failVersion = -1;
            return 0;
        }
        if (this.failVersion != currentVersion) {
            // Первая неудача при этом состоянии мира — обычный ритм.
            this.failVersion = currentVersion;
            this.failures = 1;
            return 0;
        }
        this.failures = Math.min(this.failures + 1, 16);
        final long delay = 20L << Math.min(this.failures - 2, 10);
        return (int) Math.min(delay, maxDelayTicks);
    }

    /** Нужно ли пропустить отложенную задержку, потому что мир изменился. */
    public boolean worldChangedSinceFailure() {
        return this.failVersion != -1 && this.failVersion != VERSION.get();
    }
}
