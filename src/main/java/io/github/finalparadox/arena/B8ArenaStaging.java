package io.github.finalparadox.arena;

import io.github.finalparadox.entity.B8EncounterManager;
import io.github.finalparadox.entity.KorosEchoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * Owns the B8 Echo of Koros placed before the Zombie Supermatrix fight.
 * Reconciles the Koros slot idempotently: before the one-time trigger the
 * saved candidate or the nearest live echo is retained, surplus duplicates are
 * pruned and a missing echo is created only when none exists. After the
 * trigger, during an active encounter or while an active boss is recorded the
 * helper never creates another Koros and only prunes surplus duplicates.
 */
public final class B8ArenaStaging {
    private B8ArenaStaging() {
    }

    /**
     * Reconciles the B8 Echo of Koros slot to at most one arena-owned echo.
     *
     * @return the retained echo, or empty when there is nothing to keep
     */
    public static Optional<KorosEchoEntity> reconcile(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        Optional<BlockPos> recordedAnchor = data.floorAnchor();
        if (recordedAnchor.isEmpty() || !recordedAnchor.get().equals(anchor)) return Optional.empty();
        BlockPos korosPos = anchor.offset(ArenaDefinitions.B8_KOROS_OFFSET);
        level.getChunkAt(korosPos);

        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.B8, anchor);
        List<KorosEchoEntity> candidates = ArenaWaitingBatch.scanKoros(
                level, bounds, anchor, "b8");

        boolean blocked = B8EncounterManager.isActive(level)
                || data.b8Triggered()
                || data.activeBossUuid().isPresent();
        if (blocked) {
            if (candidates.isEmpty()) return Optional.empty();
            Optional<KorosEchoEntity> retained = ArenaWaitingBatch.adopt(
                    candidates, data.korosUuid(), korosPos);
            retained.ifPresent(best -> {
                ArenaWaitingBatch.pruneSurplus(candidates, best, KorosEchoEntity::depart);
                data.setKorosUuid(best.getUUID());
            });
            return retained;
        }
        return ArenaWaitingBatch.resolveSlot(
                candidates, data.korosUuid(), korosPos,
                data::setKorosUuid,
                () -> createB8Koros(level, data, anchor),
                KorosEchoEntity::depart);
    }

    private static KorosEchoEntity createB8Koros(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        KorosEchoEntity koros = KorosEchoEntity.createB8(level, anchor);
        if (koros == null) return null;
        BlockPos pos = anchor.offset(ArenaDefinitions.B8_KOROS_OFFSET);
        koros.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(koros)) return null;
        data.setKorosUuid(koros.getUUID());
        koros.playArrivalEffects();
        return koros;
    }
}
