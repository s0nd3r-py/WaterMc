package me.s0nd3r.watermc.math;

import ca.spottedleaf.moonrise.patches.collisions.CollisionUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Thread-confined, reentrant collision workspace. No world or entity survives close(). */
public final class FastAABB implements AutoCloseable {
    private static final int STRIDE = 6;
    private static final int INITIAL_CAPACITY = 16 * STRIDE;
    private static final ThreadLocal<FastAABB> LOCAL = ThreadLocal.withInitial(FastAABB::new);
    private double[] bounds = new double[INITIAL_CAPACITY];
    private int size;
    private FastAABB next;
    private Thread owner;
    private boolean active;
    private final ArrayList<VoxelShape> voxels = new ArrayList<>();
    private final ArrayList<AABB> entityBoxes = new ArrayList<>();
    private double resultX, resultY, resultZ;

    private FastAABB() {}

    public static FastAABB acquire() {
        FastAABB workspace = LOCAL.get();
        while (workspace.active) {
            if (workspace.next == null) workspace.next = new FastAABB();
            workspace = workspace.next;
        }
        workspace.owner = Thread.currentThread();
        workspace.active = true;
        return workspace;
    }

    public List<VoxelShape> voxels() { return this.voxels; }
    public List<AABB> entityBoxes() { return this.entityBoxes; }
    public int size() { return this.size; }
    public double minY(int index) { return this.bounds[Objects.checkIndex(index, this.size) * STRIDE + 1]; }
    public double maxY(int index) { return this.bounds[Objects.checkIndex(index, this.size) * STRIDE + 4]; }
    public double resultX() { return this.resultX; }
    public double resultY() { return this.resultY; }
    public double resultZ() { return this.resultZ; }
    public Vec3 movement() { return new Vec3(this.resultX, this.resultY, this.resultZ); }

    public void add(AABB box) { this.add(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ); }
    public void addAll(List<AABB> boxes) {
        for (int i = 0, len = boxes.size(); i < len; ++i) this.add(boxes.get(i));
    }

    public void add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        final int offset = Math.multiplyExact(this.size, STRIDE);
        final int required = Math.addExact(offset, STRIDE);
        if (required > this.bounds.length) {
            final long grown = Math.max((long) required, (long) this.bounds.length + (this.bounds.length >> 1));
            this.bounds = Arrays.copyOf(this.bounds, (int) Math.min(Integer.MAX_VALUE - 8L, grown));
        }
        this.bounds[offset] = Math.min(minX, maxX);
        this.bounds[offset + 1] = Math.min(minY, maxY);
        this.bounds[offset + 2] = Math.min(minZ, maxZ);
        this.bounds[offset + 3] = Math.max(minX, maxX);
        this.bounds[offset + 4] = Math.max(minY, maxY);
        this.bounds[offset + 5] = Math.max(minZ, maxZ);
        ++this.size;
    }

    /** Same Y-first and magnitude-dependent horizontal order as Moonrise. */
    public void resolve(double x, double y, double z, AABB source) {
        double minX = source.minX, minY = source.minY, minZ = source.minZ;
        double maxX = source.maxX, maxY = source.maxY, maxZ = source.maxZ;
        if (y != 0.0) {
            y = this.collideY(minX, minY, minZ, maxX, maxY, maxZ, y);
            if (y != 0.0) {
                final double lo = minY + y, hi = maxY + y;
                minY = Math.min(lo, hi); maxY = Math.max(lo, hi);
            }
        }
        final boolean xSmaller = Math.abs(x) < Math.abs(z);
        if (xSmaller && z != 0.0) {
            z = this.collideZ(minX, minY, minZ, maxX, maxY, maxZ, z);
            if (z != 0.0) {
                final double lo = minZ + z, hi = maxZ + z;
                minZ = Math.min(lo, hi); maxZ = Math.max(lo, hi);
            }
        }
        if (x != 0.0) {
            x = this.collideX(minX, minY, minZ, maxX, maxY, maxZ, x);
            if (!xSmaller && x != 0.0) {
                final double lo = minX + x, hi = maxX + x;
                minX = Math.min(lo, hi); maxX = Math.max(lo, hi);
            }
        }
        if (!xSmaller && z != 0.0) z = this.collideZ(minX, minY, minZ, maxX, maxY, maxZ, z);
        this.resultX = x; this.resultY = y; this.resultZ = z;
    }

    private double collideX(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double value) {
        for (int i = 0, end = this.size * STRIDE; i < end; i += STRIDE) {
            if ((minY - this.bounds[i + 4]) < -CollisionUtil.COLLISION_EPSILON && (maxY - this.bounds[i + 1]) > CollisionUtil.COLLISION_EPSILON &&
                (minZ - this.bounds[i + 5]) < -CollisionUtil.COLLISION_EPSILON && (maxZ - this.bounds[i + 2]) > CollisionUtil.COLLISION_EPSILON) {
                if (value >= 0.0) {
                    final double gap = this.bounds[i + 0] - maxX;
                    if (!(gap < -CollisionUtil.COLLISION_EPSILON)) value = Math.min(gap, value);
                } else {
                    final double gap = this.bounds[i + 3] - minX;
                    if (!(gap > CollisionUtil.COLLISION_EPSILON)) value = Math.max(gap, value);
                }
            }
        }
        for (int i = 0, len = this.voxels.size(); i < len; ++i) {
            value = CollisionUtil.collideX(this.voxels.get(i), minX, minY, minZ, maxX, maxY, maxZ, value);
        }
        return value;
    }

    private double collideY(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double value) {
        for (int i = 0, end = this.size * STRIDE; i < end; i += STRIDE) {
            if ((minX - this.bounds[i + 3]) < -CollisionUtil.COLLISION_EPSILON && (maxX - this.bounds[i + 0]) > CollisionUtil.COLLISION_EPSILON &&
                (minZ - this.bounds[i + 5]) < -CollisionUtil.COLLISION_EPSILON && (maxZ - this.bounds[i + 2]) > CollisionUtil.COLLISION_EPSILON) {
                if (value >= 0.0) {
                    final double gap = this.bounds[i + 1] - maxY;
                    if (!(gap < -CollisionUtil.COLLISION_EPSILON)) value = Math.min(gap, value);
                } else {
                    final double gap = this.bounds[i + 4] - minY;
                    if (!(gap > CollisionUtil.COLLISION_EPSILON)) value = Math.max(gap, value);
                }
            }
        }
        for (int i = 0, len = this.voxels.size(); i < len; ++i) {
            value = CollisionUtil.collideY(this.voxels.get(i), minX, minY, minZ, maxX, maxY, maxZ, value);
        }
        return value;
    }

    private double collideZ(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double value) {
        for (int i = 0, end = this.size * STRIDE; i < end; i += STRIDE) {
            if ((minX - this.bounds[i + 3]) < -CollisionUtil.COLLISION_EPSILON && (maxX - this.bounds[i + 0]) > CollisionUtil.COLLISION_EPSILON &&
                (minY - this.bounds[i + 4]) < -CollisionUtil.COLLISION_EPSILON && (maxY - this.bounds[i + 1]) > CollisionUtil.COLLISION_EPSILON) {
                if (value >= 0.0) {
                    final double gap = this.bounds[i + 2] - maxZ;
                    if (!(gap < -CollisionUtil.COLLISION_EPSILON)) value = Math.min(gap, value);
                } else {
                    final double gap = this.bounds[i + 5] - minZ;
                    if (!(gap > CollisionUtil.COLLISION_EPSILON)) value = Math.max(gap, value);
                }
            }
        }
        for (int i = 0, len = this.voxels.size(); i < len; ++i) {
            value = CollisionUtil.collideZ(this.voxels.get(i), minX, minY, minZ, maxX, maxY, maxZ, value);
        }
        return value;
    }

    @Override
    public void close() {
        if (!this.active || this.owner != Thread.currentThread()) {
            throw new IllegalStateException("Collision workspace closed twice or from another thread");
        }
        this.voxels.clear();
        this.entityBoxes.clear();
        this.size = 0;
        this.resultX = this.resultY = this.resultZ = 0.0;
        if (this.bounds.length > 65_536) this.bounds = new double[INITIAL_CAPACITY];
        this.owner = null;
        this.active = false;
    }
}
