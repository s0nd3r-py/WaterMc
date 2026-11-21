package org.spigotmc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.text.DecimalFormat;

import static net.kyori.adventure.text.Component.text;

public class TicksPerSecondCommand extends Command {

    private boolean hasShownMemoryWarning; // Paper
    private static final ThreadLocal<DecimalFormat> ONE_DECIMAL_PLACES = ThreadLocal.withInitial(() -> {
        return new DecimalFormat("########0.0");
    });

    public TicksPerSecondCommand(String name) {
        super(name);
        this.description = "Gets the current ticks per second for the server";
        this.usageMessage = "/tps";
        this.setPermission("bukkit.command.tps");
    }

    // Paper start
    private static final Component WARN_MSG = text()
        .append(text("Warning: ", NamedTextColor.RED))
        .append(text("Memory usage on modern garbage collectors is not a stable value and it is perfectly normal to see it reach max. Please do not pay it much attention.", NamedTextColor.GOLD))
        .build();
    // Paper end

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        // Paper start - Further improve tick handling
        double[] tps = org.bukkit.Bukkit.getTPSIncluding5SecondAverage(); // Gale - Purpur - 5 second TPS average
        Component[] tpsAvg = new Component[tps.length];

        for (int i = 0; i < tps.length; i++) {
            tpsAvg[i] = TicksPerSecondCommand.format(tps[i]);
        }

        TextComponent.Builder builder = text();
        builder.append(text("TPS from last 5s, 1m, 5m, 15m: ", NamedTextColor.GOLD)); // Gale - Purpur - 5 second TPS average
        builder.append(Component.join(JoinConfiguration.commas(true), tpsAvg));
        sender.sendMessage(builder.asComponent());
        // Gale start - YAPFA - last tick time - in TPS command
        if (me.s0nd3r.watermc.tuning.configuration.TuningGlobalConfiguration.get().misc.lastTickTimeInTpsCommand.enabled) {
            long lastTickProperTime = net.minecraft.server.MinecraftServer.lastTickProperTime;
            long lastTickOversleepTime = net.minecraft.server.MinecraftServer.lastTickOversleepTime;
            var lastTickTimeMessage = text("Last tick: ")
                .append(formatTickTimeDuration(lastTickProperTime, 44, 50, 51));

            if (me.s0nd3r.watermc.tuning.configuration.TuningGlobalConfiguration.get().misc.lastTickTimeInTpsCommand.addOversleep) {
                lastTickTimeMessage = lastTickTimeMessage.append(text(" self + "))
                    .append(formatTickTimeDuration(lastTickOversleepTime, Math.max(1, 51 - lastTickProperTime), Math.max(2, 52 - lastTickProperTime), Math.max(3, 53 - lastTickProperTime)))
                    .append(text(" oversleep = "))
                    .append(formatTickTimeDuration(lastTickProperTime + lastTickOversleepTime, 51, 52, 53));
            }

            lastTickTimeMessage = lastTickTimeMessage.color(NamedTextColor.GOLD);
            sender.sendMessage(lastTickTimeMessage);
        }
        // Gale end - YAPFA - last tick time - in TPS command
        if (args.length > 0 && args[0].equals("mem") && sender.hasPermission("bukkit.command.tpsmemory")) {
            sender.sendMessage(text()
                .append(text("Current Memory Usage: ", NamedTextColor.GOLD))
                .append(text(((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)) + "/" + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + " mb (Max: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " mb)", NamedTextColor.GREEN))
            );
            if (!this.hasShownMemoryWarning) {
                sender.sendMessage(WARN_MSG);
                this.hasShownMemoryWarning = true;
            }
        }
        // Paper end

        return true;
    }

    private static Component format(double tps) { // Paper - Made static
        // Paper start
        TextColor color = ((tps > 18.0) ? NamedTextColor.GREEN : (tps > 16.0) ? NamedTextColor.YELLOW : NamedTextColor.RED);
        String amount = ONE_DECIMAL_PLACES.get().format(tps); // Paper - only print * at 21, we commonly peak to 20.02 as the tick sleep is not accurate enough, stop the noise
        return text(amount, color);
        // Paper end
    }

    // Gale start - YAPFA - last tick time - in TPS command
    private static final TextColor safeColor = NamedTextColor.GREEN;
    private static final TextColor closeColor = NamedTextColor.YELLOW;
    private static final TextColor problematicColor = TextColor.color(0xf77c1e);
    private static final TextColor severeColor = NamedTextColor.RED;

    private static Component formatTickTimeDuration(long ms, long safeLimit, long closeLimit, long nonSevereLimit) {
        return text(ms + " ", ms <= safeLimit ? safeColor : ms <= closeLimit ? closeColor : ms <= nonSevereLimit ? problematicColor : severeColor)
            .append(text("ms", NamedTextColor.GOLD));
    }
    // Gale end - YAPFA - last tick time - in TPS command
}
