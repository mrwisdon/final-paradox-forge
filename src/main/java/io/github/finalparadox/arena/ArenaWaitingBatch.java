package io.github.finalparadox.arena;

import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shared server-side helpers that keep each arena at most one waiting batch.
 *
 * <p>Reconciliation always interprets {@link ServerLevel#getEntity(UUID)} only
 * after the arena's own chunks have been loaded, so a saved UUID is never
 * cleared merely because its chunk was unloaded. The saved UUID wins when it
 * still matches a live candidate; surplus waiting entities are pruned and
 * missing members are created individually.
 */
public final class ArenaWaitingBatch {
    private static final double RECONCILE_PLAYER_RADIUS_SQR = 256.0D * 256.0D;

    private ArenaWaitingBatch() {
    }

    public static AABB arenaBounds(ArenaDefinition definition, BlockPos anchor) {
        return new AABB(definition.minimumCorner(anchor), definition.maximumCorner(anchor).offset(1, 1, 1));
    }

    public static void loadChunk(ServerLevel level, BlockPos pos) {
        level.getChunkAt(pos);
    }

    /**
     * True when the arena should be reconciled this tick. Arenas whose chunks
     * are already loaded are always reconciled. A distant arena is reconciled
     * only when a player is close enough to enter it, or when the deployment
     * has no recorded entities yet and therefore needs its initial batch.
     */
    public static boolean shouldReconcile(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return false;
        BlockPos anchor = anchorResult.get();
        if (level.hasChunkAt(anchor)) return true;
        boolean hasRecord = data.activeBossUuid().isPresent()
                || data.korosUuid().isPresent()
                || data.stagedKoyoUuid().isPresent()
                || data.stagedGariUuid().isPresent()
                || data.eotharUuid().isPresent();
        if (!hasRecord) return true;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(anchor.getX() + 0.5D, anchor.getY() + 0.5D, anchor.getZ() + 0.5D)
                    <= RECONCILE_PLAYER_RADIUS_SQR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves one waiting-entity slot to a single retained instance.
     *
     * <p>The saved UUID is authoritative only when it matches a candidate that
     * is already inside this arena's filtered list; an unrelated or unloaded
     * live entity is never inspected as authoritative. Surplus candidates are
     * pruned, a missing member is created only when no candidate exists, and
     * the retained or created UUID is persisted.
     *
     * @param candidates live candidates already filtered to this arena
     * @param saved      the persistent UUID from SavedData, when present
     * @param expected   expected spawn position used for nearest adoption
     * @param persist    records the retained UUID in SavedData
     * @param create     creates the missing member (may return null)
     * @param discard    cleanup used for surplus waiting entities
     */
    public static <T extends Entity> Optional<T> resolveSlot(
            List<T> candidates,
            Optional<UUID> saved,
            BlockPos expected,
            Consumer<UUID> persist,
            Supplier<T> create,
            Consumer<T> discard
    ) {
        if (saved.isPresent()) {
            for (T candidate : candidates) {
                if (saved.get().equals(candidate.getUUID())) {
                    pruneSurplus(candidates, candidate, discard);
                    return Optional.of(candidate);
                }
            }
        }
        Optional<T> adopted = adopt(candidates, saved, expected);
        if (adopted.isPresent()) {
            T chosen = adopted.get();
            pruneSurplus(candidates, chosen, discard);
            persist.accept(chosen.getUUID());
            return adopted;
        }
        T created = create.get();
        if (created != null) persist.accept(created.getUUID());
        return Optional.ofNullable(created);
    }

    /** Selects the retained candidate for one slot, preferring the saved UUID. */
    public static <T extends Entity> Optional<T> adopt(
            List<T> candidates,
            Optional<UUID> saved,
            BlockPos expected
    ) {
        List<ArenaEntitySelection.Ref> refs = refs(candidates, expected);
        Optional<ArenaEntitySelection.Ref> chosen = ArenaEntitySelection.select(saved, refs);
        if (chosen.isEmpty()) return Optional.empty();
        UUID uuid = chosen.get().uuid();
        for (T candidate : candidates) {
            if (candidate.getUUID().equals(uuid)) return Optional.of(candidate);
        }
        return Optional.empty();
    }

    /** Runs {@code discard} for every candidate except {@code retained}. */
    public static <T extends Entity> void pruneSurplus(
            List<T> candidates,
            T retained,
            Consumer<T> discard
    ) {
        for (T candidate : candidates) {
            if (!candidate.getUUID().equals(retained.getUUID())) discard.accept(candidate);
        }
    }

    public static List<KorosEchoEntity> scanKoros(
            ServerLevel level,
            AABB bounds,
            BlockPos anchor,
            String guideMode
    ) {
        return level.getEntities(ModEntities.KOROS_ECHO.get(), bounds,
                echo -> echo.isAlive()
                        && guideMode.equals(echo.guideMode())
                        && anchor.equals(echo.arenaAnchor()));
    }

    private static <T extends Entity> List<ArenaEntitySelection.Ref> refs(
            List<T> candidates,
            BlockPos expected
    ) {
        Vec3 center = Vec3.atCenterOf(expected);
        List<ArenaEntitySelection.Ref> refs = new ArrayList<>(candidates.size());
        for (T candidate : candidates) {
            refs.add(new ArenaEntitySelection.Ref(candidate.getUUID(), candidate.distanceToSqr(center)));
        }
        return refs;
    }
}
