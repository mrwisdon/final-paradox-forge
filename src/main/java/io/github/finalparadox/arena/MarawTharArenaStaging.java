package io.github.finalparadox.arena;

import io.github.finalparadox.entity.EotharEchoEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.item.ArenaCompassDestination;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public final class MarawTharArenaStaging {
    private MarawTharArenaStaging() {
    }

    /**
     * Reconciles the Maraw'Thar waiting Eothar slot to at most one
     * arena-owned echo. The recorded anchor, triggered/active state and
     * 30-block player proximity are all checked before any chunk is loaded,
     * so a distant arena never force-loads the Eothar chunk. A saved UUID is
     * authoritative only when it resolves to a live candidate of this arena,
     * identified by its persisted anchor; an unresolved UUID is preserved so
     * returning players adopt the unloaded entity rather than duplicating it.
     * Surplus candidates are pruned and a missing echo is created only when
     * the arena is un-triggered and a non-spectating player is inside the
     * original 18-block arrival range while another is inside the 30-block
     * recall range.
     *
     * @return the retained echo and whether this call created it, or empty
     *         when nothing should be kept
     */
    public static Optional<Stage> reconcile(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        Optional<BlockPos> recordedAnchor = data.floorAnchor();
        if (recordedAnchor.isEmpty() || !recordedAnchor.get().equals(anchor)) return Optional.empty();
        // A triggered or active fight never keeps a waiting Eothar and never
        // recreates one; it must also never force-load the Eothar chunk.
        if (data.marawTharTriggered() || data.activeBossUuid().isPresent()) {
            return Optional.empty();
        }
        // The echo is a wait-area guide: without any non-spectating player in
        // recall range the whole staged batch is cleaned up. A recorded
        // Eothar is cleaned only when its entity is already resolved/loaded;
        // an unresolved UUID is preserved so returning players adopt the
        // unloaded entity rather than duplicating it.
        if (!anyPlayerNear(level, anchor, 30.0D)) {
            data.eotharUuid().ifPresent(uuid -> {
                Entity resolved = level.getEntity(uuid);
                if (resolved instanceof EotharEchoEntity echo
                        && echo.isAlive()
                        && anchor.equals(echo.arenaAnchor())) {
                    echo.depart();
                    data.clearEothar();
                } else if (resolved != null) {
                    data.clearEothar();
                }
            });
            return Optional.empty();
        }

        BlockPos eotharPos = anchor.offset(ArenaDefinitions.MARAWTHAR_EOTHAR_OFFSET);
        level.getChunkAt(eotharPos);

        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.MARAWTHAR, anchor);
        List<EotharEchoEntity> candidates = level.getEntities(
                ModEntities.EOTHAR_ECHO.get(), bounds,
                echo -> echo.isAlive() && anchor.equals(echo.arenaAnchor()));
        // A live candidate is kept even before anyone enters the 18-block
        // arrival range; a missing echo is created only once someone does.
        if (candidates.isEmpty() && !anyPlayerNear(level, anchor, 18.0D)) {
            return Optional.empty();
        }
        boolean[] created = {false};
        Optional<EotharEchoEntity> echo = ArenaWaitingBatch.resolveSlot(
                candidates, data.eotharUuid(), eotharPos,
                data::setEotharUuid,
                () -> {
                    EotharEchoEntity spawned = spawnEothar(level, data, anchor).orElse(null);
                    if (spawned != null) created[0] = true;
                    return spawned;
                },
                EotharEchoEntity::depart);
        return echo.map(entity -> new Stage(entity, created[0]));
    }

    public static Optional<EotharEchoEntity> spawnEothar(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        BlockPos position = anchor.offset(ArenaDefinitions.MARAWTHAR_EOTHAR_OFFSET);
        level.getChunkAt(position);
        EotharEchoEntity eothar = EotharEchoEntity.create(level, anchor);
        if (eothar == null) return Optional.empty();
        eothar.moveTo(position.getX(), position.getY(), position.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(eothar)) return Optional.empty();
        data.setEotharUuid(eothar.getUUID());
        eothar.playArrivalEffects();
        return Optional.of(eothar);
    }

    /**
     * Removes every arena-owned waiting Eothar after loading the expected
     * chunks, not only the saved UUID, so unloaded or duplicated instances
     * are never left behind. Other arenas' Eothar instances are untouched.
     */
    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) {
            data.clearEothar();
            return;
        }
        BlockPos anchor = anchorResult.get();
        level.getChunkAt(anchor.offset(ArenaDefinitions.MARAWTHAR_EOTHAR_OFFSET));
        AABB bounds = ArenaWaitingBatch.arenaBounds(ArenaDefinitions.MARAWTHAR, anchor);
        level.getEntities(ModEntities.EOTHAR_ECHO.get(), bounds,
                        echo -> echo.isAlive() && anchor.equals(echo.arenaAnchor()))
                .forEach(EotharEchoEntity::depart);
        data.clearEothar();
    }

    public static Optional<MarawTharBossEntity> spawnBoss(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        BlockPos spawn = ArenaDefinitions.MARAWTHAR.bossSpawnBlock(anchor);
        level.getChunkAt(spawn);
        MarawTharBossEntity boss = ModEntities.MARAWTHAR.get().create(level);
        if (boss == null) return Optional.empty();
        boss.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), 0.0F, 0.0F);
        if (!level.addFreshEntity(boss)) return Optional.empty();
        data.setActiveBossUuid(boss.getUUID());
        data.setMarawTharTriggered(true);
        data.clearEothar();
        ArenaCompassDestination.setForArena(level, ArenaDefinitions.MARAWTHAR, anchor);
        return Optional.of(boss);
    }

    public static boolean anyPlayerNear(ServerLevel level, BlockPos anchor, double radius) {
        double radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                    && player.distanceToSqr(anchor.getX() + 0.5D,
                            anchor.getY() + 0.5D, anchor.getZ() + 0.5D) <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    public record Stage(EotharEchoEntity echo, boolean created) {
    }
}
