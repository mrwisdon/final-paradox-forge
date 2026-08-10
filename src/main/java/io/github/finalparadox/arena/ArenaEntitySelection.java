package io.github.finalparadox.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure-Java policy used to reconcile one waiting-entity slot in an arena to a
 * single retained instance. The SavedData UUID wins when it still matches a
 * live candidate; otherwise the candidate nearest the expected spawn is
 * retained, with the UUID as a deterministic tie-breaker.
 */
public final class ArenaEntitySelection {
    private ArenaEntitySelection() {
    }

    /** A candidate waiting entity and its squared distance to the expected spawn. */
    public record Ref(UUID uuid, double distanceSqr) {
    }

    /**
     * Selects the entity to retain for one slot.
     *
     * @param saved      the persistent UUID from SavedData, when present
     * @param candidates live candidates found inside the arena bounds
     * @return the retained candidate, or empty when there are no candidates
     */
    public static Optional<Ref> select(Optional<UUID> saved, List<Ref> candidates) {
        if (candidates.isEmpty()) return Optional.empty();
        if (saved.isPresent()) {
            for (Ref ref : candidates) {
                if (saved.get().equals(ref.uuid())) return Optional.of(ref);
            }
        }
        Ref best = candidates.get(0);
        for (int index = 1; index < candidates.size(); index++) {
            Ref ref = candidates.get(index);
            int byDistance = Double.compare(ref.distanceSqr(), best.distanceSqr());
            if (byDistance < 0 || (byDistance == 0 && ref.uuid().compareTo(best.uuid()) < 0)) {
                best = ref;
            }
        }
        return Optional.of(best);
    }

    /** All candidates except {@code retained}, in input order. */
    public static List<Ref> surplus(Ref retained, List<Ref> candidates) {
        List<Ref> result = new ArrayList<>();
        for (Ref ref : candidates) {
            if (!ref.uuid().equals(retained.uuid())) result.add(ref);
        }
        return result;
    }
}
