package me.s0nd3r.watermc.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Счётчик записей с ключом-UUID в {@link Scoreboard} должен точно отражать содержимое команд при любых
 * последовательностях операций: иначе {@code Entity.getTeam()} потеряет команду сущности.
 */
class EntityTeamsTest {

    @Test
    void uuidEntryCounterMatchesTeamContents() {
        final Random random = new Random(20261119L);
        final Scoreboard scoreboard = new Scoreboard();
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            names.add(new UUID(random.nextLong(), random.nextLong()).toString());
            names.add("player" + i);
        }
        names.add("a".repeat(36)); // не UUID, но той же длины — счётчик консервативно учитывает
        final List<String> teamNames = List.of("red", "blue", "green");
        int sawEntityEntries = 0;
        for (int step = 0; step < 50_000; step++) {
            final String name = names.get(random.nextInt(names.size()));
            final String teamName = teamNames.get(random.nextInt(teamNames.size()));
            switch (random.nextInt(10)) {
                case 0, 1, 2, 3, 4 -> scoreboard.addPlayerToTeam(name, scoreboard.addPlayerTeam(teamName));
                case 5, 6, 7 -> scoreboard.removePlayerFromTeam(name);
                case 8 -> {
                    final PlayerTeam team = scoreboard.getPlayerTeam(teamName);
                    if (team != null) {
                        scoreboard.removePlayerTeam(team);
                    }
                }
                default -> {
                    final PlayerTeam team = scoreboard.getPlayersTeam(name);
                    if (team != null) {
                        scoreboard.removePlayerFromTeam(name, team);
                    }
                }
            }
            boolean expected = false;
            for (final String candidate : names) {
                if (candidate.length() == EntityTeams.UUID_NAME_LENGTH && scoreboard.getPlayersTeam(candidate) != null) {
                    expected = true;
                    break;
                }
            }
            assertEquals(expected, scoreboard.waterHasEntityTeamEntries(), "step " + step);
            if (expected) {
                sawEntityEntries++;
            }
        }
        assertTrue(sawEntityEntries > 1_000, "the sequence must reach states with entity team entries");
        assertTrue(sawEntityEntries < 49_000, "and states without them");
    }

    @Test
    void entityNamesAreUuidLengthAndPlayerNamesAreShorter() {
        assertEquals(EntityTeams.UUID_NAME_LENGTH, UUID.randomUUID().toString().length());
        assertEquals(EntityTeams.UUID_NAME_LENGTH, new UUID(0L, 0L).toString().length());
        assertTrue(16 < EntityTeams.UUID_NAME_LENGTH);
    }
}
