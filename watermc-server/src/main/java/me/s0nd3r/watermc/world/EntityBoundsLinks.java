package me.s0nd3r.watermc.world;

import net.minecraft.world.phys.AABB;

/**
 * Связи сущности со списками секций, которые хранят копию её габаритов.
 *
 * <p>Сущность одновременно состоит в нескольких списках одной секции: общем, «жёстких» коллизий, по типу
 * и по каждому запрошенному классу ({@code getEntitiesOfClass}). Каждый список хранит её габариты под своим
 * номером. {@code Entity.setBoundingBox} обходит эти связи и обновляет все копии; обычно связей 2–5.</p>
 *
 * <p>Не потокобезопасно: как и сами списки секций, меняется только потоком, владеющим сущностью.</p>
 */
public final class EntityBoundsLinks {

    private EntityBoundsSink[] owners = new EntityBoundsSink[4];
    private int[] indices = new int[4];
    private int count;

    /** Связать с {@code owner} под номером {@code index}; повторная связь с тем же списком меняет номер. */
    public void link(final EntityBoundsSink owner, final int index) {
        final int slot = this.find(owner);
        if (slot >= 0) {
            this.indices[slot] = index;
            return;
        }
        if (this.count == this.owners.length) {
            this.owners = java.util.Arrays.copyOf(this.owners, this.count * 2);
            this.indices = java.util.Arrays.copyOf(this.indices, this.count * 2);
        }
        this.owners[this.count] = owner;
        this.indices[this.count] = index;
        this.count++;
    }

    /** Список переставил сущность на новый номер (при удалении другой сущности). */
    public void move(final EntityBoundsSink owner, final int index) {
        final int slot = this.find(owner);
        if (slot >= 0) {
            this.indices[slot] = index;
        }
    }

    public void unlink(final EntityBoundsSink owner) {
        final int slot = this.find(owner);
        if (slot < 0) {
            return;
        }
        final int last = --this.count;
        this.owners[slot] = this.owners[last];
        this.indices[slot] = this.indices[last];
        this.owners[last] = null;
    }

    public void write(final AABB bb) {
        final EntityBoundsSink[] owners = this.owners;
        final int[] indices = this.indices;
        for (int i = 0, count = this.count; i < count; i++) {
            owners[i].waterWriteBounds(indices[i], bb);
        }
    }

    public int count() {
        return this.count;
    }

    /** Номер в списке {@code owner} или -1, если связи нет. */
    public int indexIn(final EntityBoundsSink owner) {
        final int slot = this.find(owner);
        return slot < 0 ? -1 : this.indices[slot];
    }

    private int find(final EntityBoundsSink owner) {
        final EntityBoundsSink[] owners = this.owners;
        for (int i = 0, count = this.count; i < count; i++) {
            if (owners[i] == owner) {
                return i;
            }
        }
        return -1;
    }
}
