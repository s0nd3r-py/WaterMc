package me.s0nd3r.watermc.math.reference;

import ca.spottedleaf.moonrise.common.util.WorldUtil;
import ca.spottedleaf.moonrise.patches.chunk_system.world.ChunkSystemEntityGetter;
import ca.spottedleaf.moonrise.patches.collisions.block.CollisionBlockState;
import ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity;
import ca.spottedleaf.moonrise.patches.collisions.shape.CachedShapeData;
import ca.spottedleaf.moonrise.patches.collisions.shape.CollisionDiscreteVoxelShape;
import ca.spottedleaf.moonrise.patches.collisions.shape.CollisionVoxelShape;
import ca.spottedleaf.moonrise.patches.block_counting.BlockCountingChunkSection;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.OffsetDoubleList;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Arrays;
import java.util.List;
import ca.spottedleaf.moonrise.patches.collisions.CollisionUtil;
import static ca.spottedleaf.moonrise.patches.collisions.CollisionUtil.*;
import ca.spottedleaf.moonrise.patches.collisions.CollisionUtil.LazyEntityCollisionContext;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Frozen pre-Core-Math collision arithmetic for differential verification. */
public final class ReferenceCollisions {
    public static final double COLLISION_EPSILON = 1.0E-7;
    public static double collideX(final AABB target, final AABB source, final double source_move) {
        if ((source.minY - target.maxY) < -COLLISION_EPSILON && (source.maxY - target.minY) > COLLISION_EPSILON &&
            (source.minZ - target.maxZ) < -COLLISION_EPSILON && (source.maxZ - target.minZ) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minX - source.maxX; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxX - source.minX; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static double collideX(final VoxelShape target, final AABB source, final double source_move) {
        final AABB single_aabb = ((CollisionVoxelShape)target).moonrise$getSingleAABBRepresentation();
        if (single_aabb != null) {
            return collideX(single_aabb, source, source_move);
        }
        // note: this function assumes that for any i in coords that coord[i + 1] - coord[i] > COLLISION_EPSILON is true

        // offsets that should be applied to coords
        final double off_x = ((CollisionVoxelShape)target).moonrise$offsetX();
        final double off_y = ((CollisionVoxelShape)target).moonrise$offsetY();
        final double off_z = ((CollisionVoxelShape)target).moonrise$offsetZ();

        final double[] coords_x = ((CollisionVoxelShape)target).moonrise$rootCoordinatesX();
        final double[] coords_y = ((CollisionVoxelShape)target).moonrise$rootCoordinatesY();
        final double[] coords_z = ((CollisionVoxelShape)target).moonrise$rootCoordinatesZ();

        final CachedShapeData cached_shape_data = ((CollisionVoxelShape)target).moonrise$getCachedVoxelData();

        // note: size = coords.length - 1
        final int size_x = cached_shape_data.sizeX();
        final int size_y = cached_shape_data.sizeY();
        final int size_z = cached_shape_data.sizeZ();

        // note: voxel bitset with set index (x, y, z) indicates that
        //       an AABB(coords_x[x], coords_y[y], coords_z[z], coords_x[x + 1], coords_y[y + 1], coords_z[z + 1])
        //       is collidable. this is the fundamental principle of operation for the voxel collision operation


        // note: for intersection, one we find the floor of the min we can use that as the start index
        //       for the next check as source max >= source min
        // note: we can fast check intersection on the two other axis by seeing if the min index is >= size,
        //       as this implies that coords[coords.length - 1] < source min
        //       we can also fast check by seeing if max index is < 0, as this implies that coords[0] > source max

        final int floor_min_y = Math.max(
                0,
                findFloor(coords_y, off_y, source.minY + COLLISION_EPSILON, 0, size_y)
        );
        if (floor_min_y >= size_y) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_y = Math.min(
                size_y,
                findFloor(coords_y, off_y, source.maxY - COLLISION_EPSILON, floor_min_y, size_y) + 1
        );
        if (floor_min_y >= ceil_max_y) {
            // cannot intersect
            return source_move;
        }

        final int floor_min_z = Math.max(
                0,
                findFloor(coords_z, off_z, source.minZ + COLLISION_EPSILON, 0, size_z)
        );
        if (floor_min_z >= size_z) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_z = Math.min(
                size_z,
                findFloor(coords_z, off_z, source.maxZ - COLLISION_EPSILON, floor_min_z, size_z) + 1
        );
        if (floor_min_z >= ceil_max_z) {
            // cannot intersect
            return source_move;
        }

        // index = z + y*size_z + x*(size_z*size_y)

        final long[] bitset = cached_shape_data.voxelSet();

        if (source_move > 0.0) {
            final double source_max = source.maxX;
            final int ceil_max_x = findFloor(
                    coords_x, off_x, source_max - COLLISION_EPSILON, 0, size_x
            ) + 1; // add one, we are not interested in (coords[i] + COLLISION_EPSILON) < max

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index size on the collision axis for forward movement

            final int mul_x = size_y*size_z;
            for (int curr_x = ceil_max_x; curr_x < size_x; ++curr_x) {
                double max_dist = (coords_x[curr_x] + off_x) - source_max;
                if (max_dist >= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max < source_move, as coords[curr + n] < coords[curr + n + 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist >= -COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.min(max_dist, source_move);
                }
                for (int curr_y = floor_min_y; curr_y < ceil_max_y; ++curr_y) {
                    for (int curr_z = floor_min_z; curr_z < ceil_max_z; ++curr_z) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        } else {
            final double source_min = source.minX;
            final int floor_min_x = findFloor(
                    coords_x, off_x, source_min + COLLISION_EPSILON, 0, size_x
            );

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index 0 on the collision axis for backwards movement

            // note: we offset the collision axis by - 1 for the voxel bitset index, but use + 1 for the
            //       coordinate index as the voxelset stores whether the shape is solid for [index, index + 1]
            //       thus, we need to use the voxel index i-1 if we want to check that the face at index i is solid
            final int mul_x = size_y*size_z;
            for (int curr_x = floor_min_x - 1; curr_x >= 0; --curr_x) {
                double max_dist = (coords_x[curr_x + 1] + off_x) - source_min;
                if (max_dist <= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max > source_move, as coords[curr + n] > coords[curr + n - 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is possibly bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist <= COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.max(max_dist, source_move);
                }
                for (int curr_y = floor_min_y; curr_y < ceil_max_y; ++curr_y) {
                    for (int curr_z = floor_min_z; curr_z < ceil_max_z; ++curr_z) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        }
    }

    public static AABB offsetX(final AABB box, final double dx) {
        return new AABB(box.minX + dx, box.minY, box.minZ, box.maxX + dx, box.maxY, box.maxZ);
    }

    public static double performAABBCollisionsX(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideX(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double performVoxelCollisionsX(final AABB currentBoundingBox, double value, final List<VoxelShape> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final VoxelShape target = potentialCollisions.get(i);
            value = collideX(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double collideY(final AABB target, final AABB source, final double source_move) {
        if ((source.minX - target.maxX) < -COLLISION_EPSILON && (source.maxX - target.minX) > COLLISION_EPSILON &&
            (source.minZ - target.maxZ) < -COLLISION_EPSILON && (source.maxZ - target.minZ) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minY - source.maxY; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxY - source.minY; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static double collideY(final VoxelShape target, final AABB source, final double source_move) {
        final AABB single_aabb = ((CollisionVoxelShape)target).moonrise$getSingleAABBRepresentation();
        if (single_aabb != null) {
            return collideY(single_aabb, source, source_move);
        }
        // note: this function assumes that for any i in coords that coord[i + 1] - coord[i] > COLLISION_EPSILON is true

        // offsets that should be applied to coords
        final double off_x = ((CollisionVoxelShape)target).moonrise$offsetX();
        final double off_y = ((CollisionVoxelShape)target).moonrise$offsetY();
        final double off_z = ((CollisionVoxelShape)target).moonrise$offsetZ();

        final double[] coords_x = ((CollisionVoxelShape)target).moonrise$rootCoordinatesX();
        final double[] coords_y = ((CollisionVoxelShape)target).moonrise$rootCoordinatesY();
        final double[] coords_z = ((CollisionVoxelShape)target).moonrise$rootCoordinatesZ();

        final CachedShapeData cached_shape_data = ((CollisionVoxelShape)target).moonrise$getCachedVoxelData();

        // note: size = coords.length - 1
        final int size_x = cached_shape_data.sizeX();
        final int size_y = cached_shape_data.sizeY();
        final int size_z = cached_shape_data.sizeZ();

        // note: voxel bitset with set index (x, y, z) indicates that
        //       an AABB(coords_x[x], coords_y[y], coords_z[z], coords_x[x + 1], coords_y[y + 1], coords_z[z + 1])
        //       is collidable. this is the fundamental principle of operation for the voxel collision operation


        // note: for intersection, one we find the floor of the min we can use that as the start index
        //       for the next check as source max >= source min
        // note: we can fast check intersection on the two other axis by seeing if the min index is >= size,
        //       as this implies that coords[coords.length - 1] < source min
        //       we can also fast check by seeing if max index is < 0, as this implies that coords[0] > source max

        final int floor_min_x = Math.max(
                0,
                findFloor(coords_x, off_x, source.minX + COLLISION_EPSILON, 0, size_x)
        );
        if (floor_min_x >= size_x) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_x = Math.min(
                size_x,
                findFloor(coords_x, off_x, source.maxX - COLLISION_EPSILON, floor_min_x, size_x) + 1
        );
        if (floor_min_x >= ceil_max_x) {
            // cannot intersect
            return source_move;
        }

        final int floor_min_z = Math.max(
                0,
                findFloor(coords_z, off_z, source.minZ + COLLISION_EPSILON, 0, size_z)
        );
        if (floor_min_z >= size_z) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_z = Math.min(
                size_z,
                findFloor(coords_z, off_z, source.maxZ - COLLISION_EPSILON, floor_min_z, size_z) + 1
        );
        if (floor_min_z >= ceil_max_z) {
            // cannot intersect
            return source_move;
        }

        // index = z + y*size_z + x*(size_z*size_y)

        final long[] bitset = cached_shape_data.voxelSet();

        if (source_move > 0.0) {
            final double source_max = source.maxY;
            final int ceil_max_y = findFloor(
                    coords_y, off_y, source_max - COLLISION_EPSILON, 0, size_y
            ) + 1; // add one, we are not interested in (coords[i] + COLLISION_EPSILON) < max

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index size on the collision axis for forward movement

            final int mul_x = size_y*size_z;
            for (int curr_y = ceil_max_y; curr_y < size_y; ++curr_y) {
                double max_dist = (coords_y[curr_y] + off_y) - source_max;
                if (max_dist >= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max < source_move, as coords[curr + n] < coords[curr + n + 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist >= -COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.min(max_dist, source_move);
                }
                for (int curr_x = floor_min_x; curr_x < ceil_max_x; ++curr_x) {
                    for (int curr_z = floor_min_z; curr_z < ceil_max_z; ++curr_z) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        } else {
            final double source_min = source.minY;
            final int floor_min_y = findFloor(
                    coords_y, off_y, source_min + COLLISION_EPSILON, 0, size_y
            );

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index 0 on the collision axis for backwards movement

            // note: we offset the collision axis by - 1 for the voxel bitset index, but use + 1 for the
            //       coordinate index as the voxelset stores whether the shape is solid for [index, index + 1]
            //       thus, we need to use the voxel index i-1 if we want to check that the face at index i is solid
            final int mul_x = size_y*size_z;
            for (int curr_y = floor_min_y - 1; curr_y >= 0; --curr_y) {
                double max_dist = (coords_y[curr_y + 1] + off_y) - source_min;
                if (max_dist <= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max > source_move, as coords[curr + n] > coords[curr + n - 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is possibly bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist <= COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.max(max_dist, source_move);
                }
                for (int curr_x = floor_min_x; curr_x < ceil_max_x; ++curr_x) {
                    for (int curr_z = floor_min_z; curr_z < ceil_max_z; ++curr_z) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        }
    }

    public static AABB offsetY(final AABB box, final double dy) {
        return new AABB(box.minX, box.minY + dy, box.minZ, box.maxX, box.maxY + dy, box.maxZ);
    }

    public static double performAABBCollisionsY(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideY(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double performVoxelCollisionsY(final AABB currentBoundingBox, double value, final List<VoxelShape> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final VoxelShape target = potentialCollisions.get(i);
            value = collideY(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double collideZ(final AABB target, final AABB source, final double source_move) {
        if ((source.minX - target.maxX) < -COLLISION_EPSILON && (source.maxX - target.minX) > COLLISION_EPSILON &&
            (source.minY - target.maxY) < -COLLISION_EPSILON && (source.maxY - target.minY) > COLLISION_EPSILON) {
            if (source_move >= 0.0) {
                final double max_move = target.minZ - source.maxZ; // < 0.0 if no strict collision
                if (max_move < -COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.min(max_move, source_move);
            } else {
                final double max_move = target.maxZ - source.minZ; // > 0.0 if no strict collision
                if (max_move > COLLISION_EPSILON) {
                    return source_move;
                }
                return Math.max(max_move, source_move);
            }
        }
        return source_move;
    }

    public static double collideZ(final VoxelShape target, final AABB source, final double source_move) {
        final AABB single_aabb = ((CollisionVoxelShape)target).moonrise$getSingleAABBRepresentation();
        if (single_aabb != null) {
            return collideZ(single_aabb, source, source_move);
        }
        // note: this function assumes that for any i in coords that coord[i + 1] - coord[i] > COLLISION_EPSILON is true

        // offsets that should be applied to coords
        final double off_x = ((CollisionVoxelShape)target).moonrise$offsetX();
        final double off_y = ((CollisionVoxelShape)target).moonrise$offsetY();
        final double off_z = ((CollisionVoxelShape)target).moonrise$offsetZ();

        final double[] coords_x = ((CollisionVoxelShape)target).moonrise$rootCoordinatesX();
        final double[] coords_y = ((CollisionVoxelShape)target).moonrise$rootCoordinatesY();
        final double[] coords_z = ((CollisionVoxelShape)target).moonrise$rootCoordinatesZ();

        final CachedShapeData cached_shape_data = ((CollisionVoxelShape)target).moonrise$getCachedVoxelData();

        // note: size = coords.length - 1
        final int size_x = cached_shape_data.sizeX();
        final int size_y = cached_shape_data.sizeY();
        final int size_z = cached_shape_data.sizeZ();

        // note: voxel bitset with set index (x, y, z) indicates that
        //       an AABB(coords_x[x], coords_y[y], coords_z[z], coords_x[x + 1], coords_y[y + 1], coords_z[z + 1])
        //       is collidable. this is the fundamental principle of operation for the voxel collision operation


        // note: for intersection, one we find the floor of the min we can use that as the start index
        //       for the next check as source max >= source min
        // note: we can fast check intersection on the two other axis by seeing if the min index is >= size,
        //       as this implies that coords[coords.length - 1] < source min
        //       we can also fast check by seeing if max index is < 0, as this implies that coords[0] > source max

        final int floor_min_x = Math.max(
                0,
                findFloor(coords_x, off_x, source.minX + COLLISION_EPSILON, 0, size_x)
        );
        if (floor_min_x >= size_x) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_x = Math.min(
                size_x,
                findFloor(coords_x, off_x, source.maxX - COLLISION_EPSILON, floor_min_x, size_x) + 1
        );
        if (floor_min_x >= ceil_max_x) {
            // cannot intersect
            return source_move;
        }

        final int floor_min_y = Math.max(
                0,
                findFloor(coords_y, off_y, source.minY + COLLISION_EPSILON, 0, size_y)
        );
        if (floor_min_y >= size_y) {
            // cannot intersect
            return source_move;
        }

        final int ceil_max_y = Math.min(
                size_y,
                findFloor(coords_y, off_y, source.maxY - COLLISION_EPSILON, floor_min_y, size_y) + 1
        );
        if (floor_min_y >= ceil_max_y) {
            // cannot intersect
            return source_move;
        }

        // index = z + y*size_z + x*(size_z*size_y)

        final long[] bitset = cached_shape_data.voxelSet();

        if (source_move > 0.0) {
            final double source_max = source.maxZ;
            final int ceil_max_z = findFloor(
                    coords_z, off_z, source_max - COLLISION_EPSILON, 0, size_z
            ) + 1; // add one, we are not interested in (coords[i] + COLLISION_EPSILON) < max

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index size on the collision axis for forward movement

            final int mul_x = size_y*size_z;
            for (int curr_z = ceil_max_z; curr_z < size_z; ++curr_z) {
                double max_dist = (coords_z[curr_z] + off_z) - source_max;
                if (max_dist >= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max < source_move, as coords[curr + n] < coords[curr + n + 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist >= -COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.min(max_dist, source_move);
                }
                for (int curr_x = floor_min_x; curr_x < ceil_max_x; ++curr_x) {
                    for (int curr_y = floor_min_y; curr_y < ceil_max_y; ++curr_y) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        } else {
            final double source_min = source.minZ;
            final int floor_min_z = findFloor(
                    coords_z, off_z, source_min + COLLISION_EPSILON, 0, size_z
            );

            // note: only the order of the first loop matters

            // note: we cannot collide with the face at index 0 on the collision axis for backwards movement

            // note: we offset the collision axis by - 1 for the voxel bitset index, but use + 1 for the
            //       coordinate index as the voxelset stores whether the shape is solid for [index, index + 1]
            //       thus, we need to use the voxel index i-1 if we want to check that the face at index i is solid
            final int mul_x = size_y*size_z;
            for (int curr_z = floor_min_z - 1; curr_z >= 0; --curr_z) {
                double max_dist = (coords_z[curr_z + 1] + off_z) - source_min;
                if (max_dist <= source_move) {
                    // if we reach here, then we will never have a case where
                    // coords[curr + n] - source_max > source_move, as coords[curr + n] > coords[curr + n - 1]
                    // thus, we can return immediately

                    // this optimization is important since this loop is possibly bounded by size, and _not_ by
                    // a calculated max index based off of source_move - so it would be possible to check
                    // the whole intersected shape for collisions when we didn't need to!
                    return source_move;
                }
                if (max_dist <= COLLISION_EPSILON) { // only push out by up to COLLISION_EPSILON
                    max_dist = Math.max(max_dist, source_move);
                }
                for (int curr_x = floor_min_x; curr_x < ceil_max_x; ++curr_x) {
                    for (int curr_y = floor_min_y; curr_y < ceil_max_y; ++curr_y) {
                        final int index = curr_z + curr_y*size_z + curr_x*mul_x;
                        // note: JLS states long shift operators ANDS shift by 63
                        if ((bitset[index >>> 6] & (1L << index)) != 0L) {
                            return max_dist;
                        }
                    }
                }
            }

            return source_move;
        }
    }

    public static AABB offsetZ(final AABB box, final double dz) {
        return new AABB(box.minX, box.minY, box.minZ + dz, box.maxX, box.maxY, box.maxZ + dz);
    }

    public static double performAABBCollisionsZ(final AABB currentBoundingBox, double value, final List<AABB> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final AABB target = potentialCollisions.get(i);
            value = collideZ(target, currentBoundingBox, value);
        }

        return value;
    }

    public static double performVoxelCollisionsZ(final AABB currentBoundingBox, double value, final List<VoxelShape> potentialCollisions) {
        for (int i = 0, len = potentialCollisions.size(); i < len; ++i) {
            final VoxelShape target = potentialCollisions.get(i);
            value = collideZ(target, currentBoundingBox, value);
        }

        return value;
    }

    public static int findFloor(final double[] values, final double offset, final double value, int startIndex, int endIndex) {
        Objects.checkFromToIndex(startIndex, endIndex + 1, values.length);
        do {
            final int middle = (startIndex + endIndex) >>> 1;
            final double middleVal = (values[middle] + offset);

            if (value < middleVal) {
                endIndex = middle - 1;
            } else {
                startIndex = middle + 1;
            }
        } while (startIndex <= endIndex);

        return startIndex - 1;
    }

    public static Vec3 performAABBCollisions(final Vec3 moveVector, AABB axisalignedbb, final List<AABB> potentialCollisions) {
        double x = moveVector.x;
        double y = moveVector.y;
        double z = moveVector.z;

        if (y != 0.0) {
            y = performAABBCollisionsY(axisalignedbb, y, potentialCollisions);
            if (y != 0.0) {
                axisalignedbb = offsetY(axisalignedbb, y);
            }
        }

        final boolean xSmaller = Math.abs(x) < Math.abs(z);

        if (xSmaller && z != 0.0) {
            z = performAABBCollisionsZ(axisalignedbb, z, potentialCollisions);
            if (z != 0.0) {
                axisalignedbb = offsetZ(axisalignedbb, z);
            }
        }

        if (x != 0.0) {
            x = performAABBCollisionsX(axisalignedbb, x, potentialCollisions);
            if (!xSmaller && x != 0.0) {
                axisalignedbb = offsetX(axisalignedbb, x);
            }
        }

        if (!xSmaller && z != 0.0) {
            z = performAABBCollisionsZ(axisalignedbb, z, potentialCollisions);
        }

        return new Vec3(x, y, z);
    }

    public static Vec3 performCollisions(final Vec3 moveVector, AABB axisalignedbb,
                                         final List<VoxelShape> voxels,
                                         final List<AABB> aabbs) {
        if (voxels.isEmpty()) {
            // fast track only AABBs
            return performAABBCollisions(moveVector, axisalignedbb, aabbs);
        }

        double x = moveVector.x;
        double y = moveVector.y;
        double z = moveVector.z;

        if (y != 0.0) {
            y = performAABBCollisionsY(axisalignedbb, y, aabbs);
            y = performVoxelCollisionsY(axisalignedbb, y, voxels);
            if (y != 0.0) {
                axisalignedbb = offsetY(axisalignedbb, y);
            }
        }

        final boolean xSmaller = Math.abs(x) < Math.abs(z);

        if (xSmaller && z != 0.0) {
            z = performAABBCollisionsZ(axisalignedbb, z, aabbs);
            z = performVoxelCollisionsZ(axisalignedbb, z, voxels);
            if (z != 0.0) {
                axisalignedbb = offsetZ(axisalignedbb, z);
            }
        }

        if (x != 0.0) {
            x = performAABBCollisionsX(axisalignedbb, x, aabbs);
            x = performVoxelCollisionsX(axisalignedbb, x, voxels);
            if (!xSmaller && x != 0.0) {
                axisalignedbb = offsetX(axisalignedbb, x);
            }
        }

        if (!xSmaller && z != 0.0) {
            z = performAABBCollisionsZ(axisalignedbb, z, aabbs);
            z = performVoxelCollisionsZ(axisalignedbb, z, voxels);
        }

        return new Vec3(x, y, z);
    }

    public static boolean getCollisionsForBlocksOrWorldBorder(final Level world, final Entity entity, final AABB aabb,
                                                              final List<VoxelShape> intoVoxel, final List<AABB> intoAABB,
                                                              final int collisionFlags, final BiPredicate<BlockState, BlockPos> predicate) {
        final boolean checkOnly = (collisionFlags & COLLISION_FLAG_CHECK_ONLY) != 0;
        boolean ret = false;

        if ((collisionFlags & COLLISION_FLAG_CHECK_BORDER) != 0) {
            final WorldBorder worldBorder = world.getWorldBorder();
            if (CollisionUtil.isCollidingWithBorder(worldBorder, aabb) && entity != null && worldBorder.isInsideCloseToBorder(entity, aabb)) {
                if (checkOnly) {
                    return true;
                } else {
                    final VoxelShape borderShape = worldBorder.getCollisionShape();
                    intoVoxel.add(borderShape);
                    ret = true;
                }
            }
        }

        final int minSection = WorldUtil.getMinSection(world);

        final int minBlockX = Mth.floor(aabb.minX - COLLISION_EPSILON) - 1;
        final int maxBlockX = Mth.floor(aabb.maxX + COLLISION_EPSILON) + 1;

        final int minBlockY = Math.max((minSection << 4) - 1, Mth.floor(aabb.minY - COLLISION_EPSILON) - 1);
        final int maxBlockY = Math.min((WorldUtil.getMaxSection(world) << 4) + 16, Mth.floor(aabb.maxY + COLLISION_EPSILON) + 1);

        final int minBlockZ = Mth.floor(aabb.minZ - COLLISION_EPSILON) - 1;
        final int maxBlockZ = Mth.floor(aabb.maxZ + COLLISION_EPSILON) + 1;

        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final CollisionContext collisionShape = new LazyEntityCollisionContext(entity);
        final boolean useEntityCollisionShape = LazyEntityCollisionContext.useEntityCollisionShape(world, entity);

        // special cases:
        if (minBlockY > maxBlockY) {
            // no point in checking
            return ret;
        }

        final int minChunkX = minBlockX >> 4;
        final int maxChunkX = maxBlockX >> 4;

        final int minChunkY = minBlockY >> 4;
        final int maxChunkY = maxBlockY >> 4;

        final int minChunkZ = minBlockZ >> 4;
        final int maxChunkZ = maxBlockZ >> 4;

        final boolean loadChunks = (collisionFlags & COLLISION_FLAG_LOAD_CHUNKS) != 0;
        final ChunkSource chunkSource = world.getChunkSource();

        for (int currChunkZ = minChunkZ; currChunkZ <= maxChunkZ; ++currChunkZ) {
            for (int currChunkX = minChunkX; currChunkX <= maxChunkX; ++currChunkX) {
                final ChunkAccess chunk = chunkSource.getChunk(currChunkX, currChunkZ, ChunkStatus.FULL, loadChunks);

                if (chunk == null) {
                    if ((collisionFlags & COLLISION_FLAG_COLLIDE_WITH_UNLOADED_CHUNKS) != 0) {
                        if (checkOnly) {
                            return true;
                        } else {
                            intoAABB.add(getBoxForChunk(currChunkX, currChunkZ));
                            ret = true;
                        }
                    }
                    continue;
                }

                final LevelChunkSection[] sections = chunk.getSections();

                // bound y
                for (int currChunkY = minChunkY; currChunkY <= maxChunkY; ++currChunkY) {
                    final int sectionIdx = currChunkY - minSection;
                    if (sectionIdx < 0 || sectionIdx >= sections.length) {
                        continue;
                    }
                    final LevelChunkSection section = sections[sectionIdx];
                    if (section.hasOnlyAir()) {
                        // empty
                        continue;
                    }

                    final boolean hasSpecial = ((BlockCountingChunkSection)section).moonrise$hasSpecialCollidingBlocks();
                    final int sectionAdjust = !hasSpecial ? 1 : 0;

                    final PalettedContainer<BlockState> blocks = section.states;

                    final int minXIterate = currChunkX == minChunkX ? (minBlockX & 15) + sectionAdjust : 0;
                    final int maxXIterate = currChunkX == maxChunkX ? (maxBlockX & 15) - sectionAdjust : 15;
                    final int minZIterate = currChunkZ == minChunkZ ? (minBlockZ & 15) + sectionAdjust : 0;
                    final int maxZIterate = currChunkZ == maxChunkZ ? (maxBlockZ & 15) - sectionAdjust : 15;
                    final int minYIterate = currChunkY == minChunkY ? (minBlockY & 15) + sectionAdjust : 0;
                    final int maxYIterate = currChunkY == maxChunkY ? (maxBlockY & 15) - sectionAdjust : 15;

                    for (int currY = minYIterate; currY <= maxYIterate; ++currY) {
                        final int blockY = currY | (currChunkY << 4);
                        for (int currZ = minZIterate; currZ <= maxZIterate; ++currZ) {
                            final int blockZ = currZ | (currChunkZ << 4);
                            for (int currX = minXIterate; currX <= maxXIterate; ++currX) {
                                final int localBlockIndex = (currX) | (currZ << 4) | ((currY) << 8);
                                final int blockX = currX | (currChunkX << 4);

                                final int edgeCount = hasSpecial ? ((blockX == minBlockX || blockX == maxBlockX) ? 1 : 0) +
                                    ((blockY == minBlockY || blockY == maxBlockY) ? 1 : 0) +
                                    ((blockZ == minBlockZ || blockZ == maxBlockZ) ? 1 : 0) : 0;
                                if (edgeCount == 3) {
                                    continue;
                                }

                                final BlockState blockData = blocks.get(localBlockIndex);

                                if (((CollisionBlockState)blockData).moonrise$emptyContextCollisionShape()) {
                                    continue;
                                }

                                VoxelShape blockCollision = ((CollisionBlockState)blockData).moonrise$getConstantContextCollisionShape();

                                if (edgeCount == 0 || ((edgeCount != 1 || blockData.hasLargeCollisionShape()) && (edgeCount != 2 || blockData.getBlock() == Blocks.MOVING_PISTON))) {
                                    mutablePos.set(blockX, blockY, blockZ);
                                    if (useEntityCollisionShape) {
                                        blockCollision = collisionShape.getCollisionShape(blockData, world, mutablePos);
                                    } else if (blockCollision == null) {
                                        blockCollision = blockData.getCollisionShape(world, mutablePos, collisionShape);
                                    }

                                    AABB singleAABB = ((CollisionVoxelShape)blockCollision).moonrise$getSingleAABBRepresentation();
                                    if (singleAABB != null) {
                                        singleAABB = singleAABB.move((double)blockX, (double)blockY, (double)blockZ);

                                        // Not using the epsilon violates the collision rules but this is what Vanilla does.
                                        // Check BlockCollisions.class
                                        if (blockCollision == Shapes.block()) {
                                            if (!aabb.intersects(singleAABB)) {
                                                continue;
                                            }
                                        } else {
                                            if (!voxelShapeIntersect(aabb, singleAABB)) {
                                                continue;
                                            }
                                        }

                                        if (predicate != null) {
                                            if (!predicate.test(blockData, mutablePos)) {
                                                continue;
                                            }
                                        }

                                        if (checkOnly) {
                                            return true;
                                        } else {
                                            ret = true;
                                            intoAABB.add(singleAABB);
                                            continue;
                                        }
                                    }

                                    if (blockCollision.isEmpty()) {
                                        continue;
                                    }

                                    final VoxelShape blockCollisionOffset = blockCollision.move((double)blockX, (double)blockY, (double)blockZ);

                                    if (!voxelShapeIntersectNoEmpty(blockCollisionOffset, aabb)) {
                                        continue;
                                    }

                                    if (predicate != null) {
                                        if (!predicate.test(blockData, mutablePos)) {
                                            continue;
                                        }
                                    }

                                    if (checkOnly) {
                                        return true;
                                    } else {
                                        ret = true;
                                        intoVoxel.add(blockCollisionOffset);
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }
}
