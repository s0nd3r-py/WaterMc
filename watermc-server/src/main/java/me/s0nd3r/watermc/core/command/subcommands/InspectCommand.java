package me.s0nd3r.watermc.core.command.subcommands;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import me.s0nd3r.watermc.core.command.WaterCommand;
import me.s0nd3r.watermc.core.command.PermissionedWaterSubcommand;
import me.s0nd3r.watermc.diagnostics.ChunkInspector;

/** {@code /watermc inspect [seconds]} — топ-10 чанков по времени тика сущностей, block entities и воронок. */
public final class InspectCommand extends PermissionedWaterSubcommand {

    public static final String LITERAL_ARGUMENT = "inspect";
    public static final String PERM = WaterCommand.BASE_PERM + "." + LITERAL_ARGUMENT;

    public InspectCommand() {
        super(PERM, PermissionDefault.OP);
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        int seconds = 10;
        if (args.length > 0) {
            try {
                seconds = Math.max(1, Math.min(120, Integer.parseInt(args[0])));
            } catch (final NumberFormatException ex) {
                sender.sendMessage(Component.text("Usage: /" + WaterCommand.COMMAND_LABEL + " inspect [seconds 1-120]", NamedTextColor.RED));
                return true;
            }
        }
        if (ChunkInspector.active) {
            sender.sendMessage(Component.text("Chunk inspection is already running.", NamedTextColor.RED));
            return true;
        }
        final MinecraftServer server = MinecraftServer.getServer();
        ChunkInspector.begin(server.getTickCount());
        sender.sendMessage(Component.text("Sampling chunk tick time for " + seconds + "s...", NamedTextColor.GRAY));
        CompletableFuture.delayedExecutor(seconds, TimeUnit.SECONDS).execute(() -> server.execute(() -> report(sender, ChunkInspector.finish(server.getTickCount(), 10))));
        return true;
    }

    private static void report(final CommandSender sender, final ChunkInspector.Result result) {
        sender.sendMessage(Component.text("Heaviest chunks over " + result.ticks() + " ticks (ms/tick):", NamedTextColor.GOLD));
        if (result.rows().isEmpty()) {
            sender.sendMessage(Component.text("  nothing ticked", NamedTextColor.GRAY));
            return;
        }
        int rank = 1;
        for (final ChunkInspector.Row row : result.rows()) {
            final ChunkInspector.Sample s = row.sample();
            sender.sendMessage(Component.text(String.format(Locale.ROOT,
                "%2d. %s [%d, %d] (block %d, %d): %.3f | entities %.3f (%d) | block entities %.3f (%d) | hoppers %.3f (%d)",
                rank++, row.world(), row.chunkX(), row.chunkZ(), row.chunkX() << 4, row.chunkZ() << 4,
                result.msPerTick(row.totalNanos()),
                result.msPerTick(s.entityNanos), s.entityTicks / Math.max(1L, result.ticks()),
                result.msPerTick(s.blockEntityNanos), s.blockEntityTicks / Math.max(1L, result.ticks()),
                result.msPerTick(s.hopperNanos), s.hopperTicks / Math.max(1L, result.ticks())), NamedTextColor.YELLOW));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        return args.length == 1 ? List.of("10", "30", "60") : List.of();
    }
}
