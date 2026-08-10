package io.github.finalparadox.arena;

import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.entity.B8EncounterManager;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import net.minecraft.server.level.ServerLevel;

/**
 * Read-only aggregation of every real boss-fight state in the arena dimension.
 * It decides whether the Arena Compass may return a player to the overworld.
 *
 * <p>This class deliberately never clears or rewrites SavedData and never
 * force-loads arena chunks: a saved boss UUID counts only while it resolves to
 * a currently loaded living boss. Normal fights keep their boss loaded through
 * the participating players and the existing encounter controllers.
 */
public final class ArenaBossFightState {
    private ArenaBossFightState() {
    }

    /**
     * Returns true while any real boss fight is in progress in {@code level}.
     * Waiting bosses and pre-battle dialogue do not count; countdowns, intros,
     * combat, and defeat/victory wrap-up do, as long as the authoritative
     * encounter or entity is still active.
     */
    public static boolean isAnyActive(ServerLevel level) {
        return anyActive(
                activeB1(level),
                activeB2(level),
                B5EncounterManager.isActive(level),
                B8EncounterManager.isActive(level),
                activeMarawThar(level));
    }

    /** Pure OR over the five arena states; package-private for unit tests. */
    static boolean anyActive(boolean b1, boolean b2, boolean b5, boolean b8, boolean maraw) {
        return b1 || b2 || b5 || b8 || maraw;
    }

    /** B1/B2 rule: a living boss that is not waiting counts as an active fight. */
    static boolean livingNonWaiting(boolean alive, boolean waiting) {
        return alive && !waiting;
    }

    /** Maraw'Thar rule: the saved boss counts as active whenever it is alive. */
    static boolean livingBoss(boolean alive) {
        return alive;
    }

    private static boolean activeB1(ServerLevel level) {
        return ArenaDeploymentData.get(level, ArenaDefinitions.B1)
                .activeBossUuid()
                .map(level::getEntity)
                .filter(ApigloBossEntity.class::isInstance)
                .map(ApigloBossEntity.class::cast)
                .filter(boss -> livingNonWaiting(boss.isAlive(), boss.isWaiting()))
                .isPresent();
    }

    private static boolean activeB2(ServerLevel level) {
        return ArenaDeploymentData.get(level, ArenaDefinitions.B2)
                .activeBossUuid()
                .map(level::getEntity)
                .filter(TharKrooBossEntity.class::isInstance)
                .map(TharKrooBossEntity.class::cast)
                .filter(boss -> livingNonWaiting(boss.isAlive(), boss.isWaiting()))
                .isPresent();
    }

    private static boolean activeMarawThar(ServerLevel level) {
        return ArenaDeploymentData.get(level, ArenaDefinitions.MARAWTHAR)
                .activeBossUuid()
                .map(level::getEntity)
                .filter(MarawTharBossEntity.class::isInstance)
                .map(MarawTharBossEntity.class::cast)
                .filter(boss -> livingBoss(boss.isAlive()))
                .isPresent();
    }
}
