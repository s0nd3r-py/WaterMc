// Leaf Test - Async Executor

package me.s0nd3r.watermc.runtime;

import me.s0nd3r.watermc.runtime.util.AsyncExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.bukkit.support.environment.LeafTest;

import java.util.concurrent.atomic.AtomicInteger;

@LeafTest
public class AsyncExecutorTest {

    AsyncExecutor mobSpawnExecutor = new AsyncExecutor("Leaf Async Mob Spawn Test Thread");

    @Test
    public void testTaskSubmission() throws InterruptedException {
        mobSpawnExecutor.start();
        int submittedTaskCount = 0;
        AtomicInteger completedTaskCount = new AtomicInteger(0);
        System.out.println("Submitting 1");
        submittedTaskCount++;
        mobSpawnExecutor.submit(() -> {
            try {
                System.out.println(" -> 1...");
                Thread.sleep(500);
                System.out.println(" -> 1 done");
                completedTaskCount.incrementAndGet();
            } catch (InterruptedException ignored) {
            }
        });

        Thread.sleep(100);

        System.out.println("Submitting...");
        submittedTaskCount++;
        mobSpawnExecutor.submit(() -> {
            System.out.println(" -> " + completedTaskCount.incrementAndGet());
        });
        submittedTaskCount++;
        mobSpawnExecutor.submit(() -> {
            System.out.println(" -> " + completedTaskCount.incrementAndGet());
        });
        submittedTaskCount++;
        mobSpawnExecutor.submit(() -> {
            System.out.println(" -> " + completedTaskCount.incrementAndGet());
        });

        Thread.sleep(2000);
        mobSpawnExecutor.join(9999);

        final int submittedTaskCountFinal = submittedTaskCount;
        final int completedTaskCountFinal = completedTaskCount.get();

        Assertions.assertEquals(submittedTaskCountFinal, completedTaskCountFinal, "Submitted (" + submittedTaskCountFinal + ") tasks, but only (" +  completedTaskCountFinal + ") tasks completed." );
    }
}
