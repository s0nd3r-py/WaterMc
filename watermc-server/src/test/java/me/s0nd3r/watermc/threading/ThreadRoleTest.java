package me.s0nd3r.watermc.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import me.s0nd3r.watermc.core.async.ExecutorFactory;
import me.s0nd3r.watermc.core.config.modules.opt.VirtualThreadSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreadRoleTest {
    @org.junit.jupiter.api.BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void mixedComputePoolsStayPlatformEvenWithLegacyVirtualFlags() throws Exception {
        VirtualThreadSupport.bukkitAsyncScheduler = true;
        VirtualThreadSupport.foliaAsyncScheduler = true;
        VirtualThreadSupport.asyncChatExecutor = true;
        VirtualThreadSupport.paperConfigurationPool = true;
        try (ExecutorService chat = ExecutorFactory.buildChatExecutor();
             ExecutorService config = ExecutorFactory.buildPaperConfigurationPool();
             ExecutorService bukkit = ExecutorFactory.buildBukkitAsyncSchedulerExecutor();
             ExecutorService folia = ExecutorFactory.buildFoliaAsyncSchedulerExecutor()) {
            for (ExecutorService executor : new ExecutorService[]{chat, config, bukkit, folia}) {
                assertFalse(executor.submit(() -> Thread.currentThread().isVirtual()).get(10, TimeUnit.SECONDS));
            }
        } finally {
            VirtualThreadSupport.bukkitAsyncScheduler = false;
            VirtualThreadSupport.foliaAsyncScheduler = false;
            VirtualThreadSupport.asyncChatExecutor = false;
            VirtualThreadSupport.paperConfigurationPool = false;
        }
    }

    @Test
    void knownBlockingIoPoolsCanUseVirtualThreads() throws Exception {
        boolean oldAuth = VirtualThreadSupport.authPool, oldDownload = VirtualThreadSupport.downloadPool;
        VirtualThreadSupport.authPool = true; VirtualThreadSupport.downloadPool = true;
        try (ExecutorService auth = ExecutorFactory.buildAuthPoolExecutor();
             ExecutorService download = ExecutorFactory.buildDownloadPoolExecutor()) {
            assertTrue(auth.submit(() -> Thread.currentThread().isVirtual()).get(10, TimeUnit.SECONDS));
            assertTrue(download.submit(() -> Thread.currentThread().isVirtual()).get(10, TimeUnit.SECONDS));
        } finally { VirtualThreadSupport.authPool = oldAuth; VirtualThreadSupport.downloadPool = oldDownload; }
    }
}
