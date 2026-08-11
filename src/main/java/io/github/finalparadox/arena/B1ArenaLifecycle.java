package io.github.finalparadox.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Pure policy for the B1 encounter's low-cost lifecycle and boss ownership. */
public final class B1ArenaLifecycle {
    /** Waiting batches heal twice per second; active fights never run the arena scan. */
    public static final int WAITING_RECONCILE_TICKS = 40;

    private B1ArenaLifecycle() {
    }

    public static boolean shouldReconcile(long gameTime, boolean fightActive) {
        return !fightActive && gameTime % WAITING_RECONCILE_TICKS == 0L;
    }

    /**
     * Chooses one encounter owner. A running boss always outranks a waiting
     * copy; within the selected phase, the saved UUID wins and distance/UUID
     * provide a deterministic legacy fallback.
     */
    public static Optional<BossRef> selectAuthoritative(
            Optional<UUID> saved,
            List<BossRef> candidates
    ) {
        boolean hasActive = candidates.stream().anyMatch(candidate -> !candidate.waiting());
        List<BossRef> eligible = new ArrayList<>();
        for (BossRef candidate : candidates) {
            if (!hasActive || !candidate.waiting()) eligible.add(candidate);
        }
        if (eligible.isEmpty()) return Optional.empty();
        if (saved.isPresent()) {
            for (BossRef candidate : eligible) {
                if (saved.get().equals(candidate.uuid())) return Optional.of(candidate);
            }
        }
        BossRef best = eligible.get(0);
        for (int index = 1; index < eligible.size(); index++) {
            BossRef candidate = eligible.get(index);
            int byDistance = Double.compare(candidate.distanceSqr(), best.distanceSqr());
            if (byDistance < 0
                    || (byDistance == 0 && candidate.uuid().compareTo(best.uuid()) < 0)) {
                best = candidate;
            }
        }
        return Optional.of(best);
    }

    /** Constant-time ownership decision used by each loaded Apiglo. */
    public static AuthorityDecision authorityDecision(
            UUID self,
            boolean selfWaiting,
            Optional<UUID> saved,
            boolean savedResolved,
            boolean savedValid,
            boolean savedWaiting
    ) {
        if (saved.isEmpty() || (savedResolved && !savedValid)) {
            return AuthorityDecision.CLAIM;
        }
        if (self.equals(saved.get())) return AuthorityDecision.KEEP;
        if (!savedResolved) return AuthorityDecision.DISCARD_SELF;
        if (!selfWaiting && savedWaiting) return AuthorityDecision.REPLACE_WAITING;
        return AuthorityDecision.DISCARD_SELF;
    }

    /** Preserve unresolved UUIDs; clear only a resolved invalid record or a known discarded owner. */
    public static boolean shouldClearRecordedBoss(
            boolean recordedBossDiscarded,
            boolean savedResolved,
            boolean savedValid
    ) {
        return recordedBossDiscarded || (savedResolved && !savedValid);
    }

    public static boolean shouldFinishVictory(boolean serverSide, boolean victoryCondition) {
        return serverSide && victoryCondition;
    }

    public enum AuthorityDecision {
        KEEP,
        CLAIM,
        REPLACE_WAITING,
        DISCARD_SELF
    }

    public record BossRef(UUID uuid, boolean waiting, double distanceSqr) {
    }
}
