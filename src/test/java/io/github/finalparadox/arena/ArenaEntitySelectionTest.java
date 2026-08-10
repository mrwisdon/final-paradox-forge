package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaEntitySelectionTest {
    private static final UUID ALPHA = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BETA = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID GAMMA = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void savedUuidWinsWhenPresentInCandidates() {
        List<ArenaEntitySelection.Ref> candidates = List.of(
                new ArenaEntitySelection.Ref(ALPHA, 100.0D),
                new ArenaEntitySelection.Ref(BETA, 5.0D));
        Optional<ArenaEntitySelection.Ref> chosen =
                ArenaEntitySelection.select(Optional.of(ALPHA), candidates);
        assertTrue(chosen.isPresent());
        assertEquals(ALPHA, chosen.get().uuid());
    }

    @Test
    void staleSavedUuidFallsBackToNearest() {
        List<ArenaEntitySelection.Ref> candidates = List.of(
                new ArenaEntitySelection.Ref(GAMMA, 3.0D),
                new ArenaEntitySelection.Ref(BETA, 9.0D));
        Optional<ArenaEntitySelection.Ref> chosen =
                ArenaEntitySelection.select(Optional.of(ALPHA), candidates);
        assertTrue(chosen.isPresent());
        assertEquals(GAMMA, chosen.get().uuid());
    }

    @Test
    void nearestWinsWithoutSaved() {
        List<ArenaEntitySelection.Ref> candidates = List.of(
                new ArenaEntitySelection.Ref(BETA, 20.0D),
                new ArenaEntitySelection.Ref(GAMMA, 2.0D),
                new ArenaEntitySelection.Ref(ALPHA, 7.0D));
        Optional<ArenaEntitySelection.Ref> chosen =
                ArenaEntitySelection.select(Optional.empty(), candidates);
        assertTrue(chosen.isPresent());
        assertEquals(GAMMA, chosen.get().uuid());
    }

    @Test
    void uuidBreaksDistanceTies() {
        List<ArenaEntitySelection.Ref> candidates = List.of(
                new ArenaEntitySelection.Ref(BETA, 6.0D),
                new ArenaEntitySelection.Ref(GAMMA, 6.0D),
                new ArenaEntitySelection.Ref(ALPHA, 6.0D));
        Optional<ArenaEntitySelection.Ref> chosen =
                ArenaEntitySelection.select(Optional.empty(), candidates);
        assertTrue(chosen.isPresent());
        assertEquals(ALPHA, chosen.get().uuid());
    }

    @Test
    void emptyCandidatesReturnEmpty() {
        assertTrue(ArenaEntitySelection.select(Optional.of(ALPHA), List.of()).isEmpty());
        assertTrue(ArenaEntitySelection.select(Optional.empty(), List.of()).isEmpty());
    }

    @Test
    void surplusExcludesOnlyTheRetainedCandidate() {
        List<ArenaEntitySelection.Ref> candidates = List.of(
                new ArenaEntitySelection.Ref(ALPHA, 1.0D),
                new ArenaEntitySelection.Ref(BETA, 2.0D),
                new ArenaEntitySelection.Ref(GAMMA, 3.0D));
        List<ArenaEntitySelection.Ref> surplus =
                ArenaEntitySelection.surplus(new ArenaEntitySelection.Ref(BETA, 2.0D), candidates);
        assertEquals(2, surplus.size());
        assertEquals(List.of(ALPHA, GAMMA),
                surplus.stream().map(ArenaEntitySelection.Ref::uuid).toList());
    }
}
