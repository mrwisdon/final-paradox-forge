package io.github.finalparadox.arena;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaFightParticipantsTest {
    @Test
    void onlyRecordedParticipantsCanBeDefeated() {
        UUID first = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();
        ArenaFightParticipants.Roster roster =
                new ArenaFightParticipants.Roster(List.of(first), List.of());

        assertFalse(roster.markDefeated(outsider));
        assertFalse(roster.isDefeated(outsider));
        assertTrue(roster.markDefeated(first));
        assertTrue(roster.isDefeated(first));
    }

    @Test
    void defeatRequiresEveryRecordedParticipant() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ArenaFightParticipants.Roster roster =
                new ArenaFightParticipants.Roster(List.of(first, second), List.of());

        roster.markDefeated(first);
        assertFalse(roster.allDefeated());
        roster.markDefeated(second);
        assertTrue(roster.allDefeated());
    }

    @Test
    void resetDefeatedRearmsRevivedParty() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ArenaFightParticipants.Roster roster =
                new ArenaFightParticipants.Roster(
                        List.of(first, second), List.of(first, second));

        assertTrue(roster.resetDefeated());
        assertFalse(roster.allDefeated());
        assertFalse(roster.isDefeated(first));
        assertFalse(roster.resetDefeated());
    }

    @Test
    void emptyRosterNeverReportsDefeat() {
        ArenaFightParticipants.Roster roster =
                new ArenaFightParticipants.Roster(List.of(), List.of());

        assertFalse(roster.allDefeated());
    }

    @Test
    void rosterRoundTripsAndDropsCorruptDefeatedEntries() {
        UUID participant = UUID.randomUUID();
        UUID outsider = UUID.randomUUID();
        ArenaFightParticipants.Roster original =
                new ArenaFightParticipants.Roster(
                        List.of(participant), List.of(participant, outsider));
        CompoundTag encoded = original.write();
        ArenaFightParticipants.Roster decoded =
                ArenaFightParticipants.Roster.read(encoded);

        assertTrue(decoded.isParticipant(participant));
        assertTrue(decoded.isDefeated(participant));
        assertFalse(decoded.isParticipant(outsider));
        assertFalse(decoded.isDefeated(outsider));
        assertTrue(decoded.allDefeated());
    }
}
