package me.s0nd3r.watermc.core.command.subcommands;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import me.s0nd3r.watermc.bootstrap.ServerLayout;
import me.s0nd3r.watermc.core.command.PermissionedWaterSubcommand;
import me.s0nd3r.watermc.core.command.WaterCommand;
import me.s0nd3r.watermc.storage.WdbExport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 * {@code /watermc data [list]} — journals in {@code data/} with size and entry count;
 * {@code /watermc data export <name|all>} — readable JSON copy in {@code data/export/}. File work runs off the main thread.
 */
public final class DataCommand extends PermissionedWaterSubcommand {

    public static final String LITERAL_ARGUMENT = "data";
    public static final String PERM = WaterCommand.BASE_PERM + "." + LITERAL_ARGUMENT;

    public DataCommand() {
        super(PERM, PermissionDefault.OP);
    }

    private static void reply(final CommandSender sender, final Component message) {
        MinecraftServer.getServer().execute(() -> sender.sendMessage(message));
    }

    private static void async(final CommandSender sender, final Consumer<Path> work) {
        final Path dataDir = ServerLayout.current().dataDir();
        CompletableFuture.runAsync(() -> work.accept(dataDir)).exceptionally(failure -> {
            final Throwable cause = failure.getCause() != null ? failure.getCause() : failure;
            reply(sender, Component.text("Data command failed: " + cause.getMessage(), NamedTextColor.RED));
            return null;
        });
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            async(sender, dataDir -> {
                try {
                    final List<Path> journals = WdbExport.journals(dataDir);
                    if (journals.isEmpty()) {
                        reply(sender, Component.text("No .wdb journals in " + dataDir, NamedTextColor.GRAY));
                        return;
                    }
                    reply(sender, Component.text("Journals in " + dataDir + ":", NamedTextColor.GOLD));
                    for (final Path journal : journals) {
                        final WdbExport.Info info = WdbExport.info(journal);
                        reply(sender, Component.text(String.format(Locale.ROOT, "  %-22s %6d entries  %8d records  %s",
                            info.name(), info.entries(), info.records(), human(info.bytes())), NamedTextColor.GRAY));
                    }
                } catch (final java.io.IOException ex) {
                    throw new java.io.UncheckedIOException(ex);
                }
            });
            return true;
        }
        if (args[0].equalsIgnoreCase("export") && args.length == 2) {
            final String name = args[1].toLowerCase(Locale.ROOT);
            if (!name.equals("all") && !name.matches("[a-z0-9_-]+")) {
                sender.sendMessage(Component.text("Journal name may contain only a-z, 0-9, '-' and '_'.", NamedTextColor.RED));
                return true;
            }
            async(sender, dataDir -> {
                try {
                    final Path out = dataDir.resolve("export");
                    final LocalDateTime now = LocalDateTime.now();
                    final List<WdbExport.Result> results;
                    if (name.equals("all")) {
                        results = WdbExport.exportAll(dataDir, out, now);
                    } else {
                        final Path journal = dataDir.resolve(name + ".wdb");
                        if (!java.nio.file.Files.isRegularFile(journal)) {
                            reply(sender, Component.text("No journal " + name + ".wdb in " + dataDir, NamedTextColor.RED));
                            return;
                        }
                        results = List.of(WdbExport.export(journal, out, now));
                    }
                    for (final WdbExport.Result result : results) {
                        reply(sender, Component.text("Exported " + result.entries() + " entries to " + result.output()
                            + (result.truncatedTail() ? " (an incomplete last record was skipped)" : ""), NamedTextColor.GREEN));
                    }
                    if (results.isEmpty()) reply(sender, Component.text("Nothing to export.", NamedTextColor.GRAY));
                } catch (final java.io.IOException ex) {
                    throw new java.io.UncheckedIOException(ex);
                }
            });
            return true;
        }
        sender.sendMessage(Component.text("Usage: /" + WaterCommand.COMMAND_LABEL + " data [list] | data export <name|all>", NamedTextColor.RED));
        return true;
    }

    private static String human(final long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KiB", bytes / 1024.0);
        return String.format(Locale.ROOT, "%.1f MiB", bytes / (1024.0 * 1024.0));
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        if (args.length == 1) return List.of("list", "export");
        if (args.length == 2 && args[0].equalsIgnoreCase("export")) {
            final List<String> names = new ArrayList<>();
            names.add("all");
            try {
                for (final Path journal : WdbExport.journals(ServerLayout.current().dataDir())) names.add(WdbExport.baseName(journal));
            } catch (final java.io.IOException ignored) {
                // completion only
            }
            return names;
        }
        return List.of();
    }
}
