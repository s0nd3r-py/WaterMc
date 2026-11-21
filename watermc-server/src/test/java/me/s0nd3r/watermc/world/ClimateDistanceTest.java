package me.s0nd3r.watermc.world;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Плоская дистанция R-дерева биомов должна совпадать с ванильной до бита, иначе изменятся биомы мира. */
class ClimateDistanceTest {

    private static long vanilla(Climate.Parameter[] space, long[] values) {
        long sum = 0L;
        for (int i = 0; i < ClimateDistance.DIMENSIONS; i++) sum += Mth.square(space[i].distance(values[i]));
        return sum;
    }

    private static long fitness(Climate.ParameterPoint p, Climate.TargetPoint t) {
        return Mth.square(p.temperature().distance(t.temperature())) + Mth.square(p.humidity().distance(t.humidity()))
            + Mth.square(p.continentalness().distance(t.continentalness())) + Mth.square(p.erosion().distance(t.erosion()))
            + Mth.square(p.depth().distance(t.depth())) + Mth.square(p.weirdness().distance(t.weirdness()))
            + Mth.square(p.offset());
    }

    private static long value(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> random.nextLong(-20_000L, 20_000L);
            case 1 -> Climate.quantizeCoord(random.nextFloat() * 4 - 2);
            case 2 -> random.nextBoolean() ? Long.MAX_VALUE / 4 : Long.MIN_VALUE / 4;
            default -> random.nextLong();
        };
    }

    @Test
    void flatDistanceIsBitIdenticalToVanilla() {
        Random random = new Random(42);
        for (int n = 0; n < 200_000; n++) {
            Climate.Parameter[] space = new Climate.Parameter[ClimateDistance.DIMENSIONS];
            long[] values = new long[ClimateDistance.DIMENSIONS];
            for (int i = 0; i < space.length; i++) {
                long a = value(random), b = value(random);
                space[i] = new Climate.Parameter(Math.min(a, b), Math.max(a, b));
                values[i] = value(random);
            }
            assertEquals(vanilla(space, values), ClimateDistance.distance(ClimateDistance.flatten(space), values));
        }
    }

    @Test
    void treeSearchFindsAnOptimalBiome() {
        Random random = new Random(7);
        List<Pair<Climate.ParameterPoint, Integer>> points = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            float[] r = new float[12];
            for (int k = 0; k < r.length; k++) r[k] = random.nextFloat() * 2 - 1;
            points.add(Pair.of(Climate.parameters(
                Climate.Parameter.span(Math.min(r[0], r[1]), Math.max(r[0], r[1])),
                Climate.Parameter.span(Math.min(r[2], r[3]), Math.max(r[2], r[3])),
                Climate.Parameter.span(Math.min(r[4], r[5]), Math.max(r[4], r[5])),
                Climate.Parameter.span(Math.min(r[6], r[7]), Math.max(r[6], r[7])),
                Climate.Parameter.point(random.nextBoolean() ? 0.0F : 1.0F),
                Climate.Parameter.span(Math.min(r[8], r[9]), Math.max(r[8], r[9])),
                r[10] * 0.5F), i));
        }
        Climate.ParameterList<Integer> list = new Climate.ParameterList<>(points);
        for (int n = 0; n < 20_000; n++) {
            Climate.TargetPoint target = Climate.target(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1,
                random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat(), random.nextFloat() * 2 - 1);
            int tree = list.findValue(target);
            int brute = list.findValueBruteForce(target);
            assertEquals(fitness(points.get(brute).getFirst(), target), fitness(points.get(tree).getFirst(), target),
                "tree search must return a biome with the minimal distance");
        }
    }
}
