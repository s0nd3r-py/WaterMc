package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class VirtualThreadSupport extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".use-virtual-thread";
    }

    /** Compatibility field; mixed work is always assigned to platform threads. */
    @Deprecated
    public static boolean bukkitAsyncScheduler = false;
    /** Compatibility field; mixed work is always assigned to platform threads. */
    @Deprecated
    public static boolean foliaAsyncScheduler = false;
    /** Compatibility field; mixed work is always assigned to platform threads. */
    @Deprecated
    public static boolean asyncChatExecutor = false;
    public static boolean downloadPool = false;
    public static boolean authPool = true;
    /** Compatibility field; mixed work is always assigned to platform threads. */
    @Deprecated
    public static boolean paperConfigurationPool = false;

    @Override
    public void onLoaded() {
        bukkitAsyncScheduler = false;
        foliaAsyncScheduler = false;
        asyncChatExecutor = false;
        downloadPool = config.getBoolean(getBasePath() + ".download-pool", downloadPool,
            config.pickStringRegionBased(
                "Use the new Virtual Thread introduced in JDK 21 for profile fetching executor.",
                "是否为档案查询执行器使用虚拟线程。"));
        authPool = config.getBoolean(getBasePath() + ".auth-pool", authPool,
            config.pickStringRegionBased(
                "Use the new Virtual Thread introduced in JDK 21 for user authentication.",
                "是否为用户验证使用虚拟线程."));
        paperConfigurationPool = false;
    }
}
