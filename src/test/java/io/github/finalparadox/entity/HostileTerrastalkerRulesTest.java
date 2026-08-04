package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HostileTerrastalkerRulesTest {
    @Test
    void phaseFiveRoundThreeSpawnsOneTerrastalker() {
        assertEquals(1, HostileTerrastalkerRules.spawnCountForRound(3));
    }

    @Test
    void phaseFiveRoundFourSpawnsTwoTerrastalkers() {
        assertEquals(2, HostileTerrastalkerRules.spawnCountForRound(4));
    }

    @Test
    void waitingRoundsDoNotSpawnDuplicates() {
        assertEquals(0, HostileTerrastalkerRules.spawnCountForRound(5));
        assertEquals(0, HostileTerrastalkerRules.spawnCountForRound(6));
    }

    @Test
    void gunHitsHaveHalfSecondCooldownPerPlayer() {
        assertEquals(10, HostileTerrastalkerRules.gunHitCooldownTicks());
        assertFalse(HostileTerrastalkerRules.canApplyGunHit(109, 100));
        assertTrue(HostileTerrastalkerRules.canApplyGunHit(110, 100));
    }

    @Test
    void turretAimTurnsGraduallyInsteadOfSnappingToTarget() {
        assertEquals(4.0F, HostileTerrastalkerRules.turnAimToward(0.0F, 90.0F, 4.0F));
        assertEquals(-4.0F, HostileTerrastalkerRules.turnAimToward(0.0F, -90.0F, 4.0F));
        assertEquals(-179.0F, HostileTerrastalkerRules.turnAimToward(179.0F, -179.0F, 4.0F));
    }

    @Test
    void multiplayerRaisesSourceHealthFromTwoHundredToThreeHundred() {
        assertEquals(200, HostileTerrastalkerRules.maxHealth(1));
        assertEquals(300, HostileTerrastalkerRules.maxHealth(2));
        assertEquals(300, HostileTerrastalkerRules.maxHealth(8));
    }

    @Test
    void multiplayerOverheatsEarlierThanSingleplayer() {
        assertEquals(350, HostileTerrastalkerRules.overheatThreshold(1));
        assertEquals(200, HostileTerrastalkerRules.overheatThreshold(2));
    }

    @Test
    void autocannonHitDamagesOccupiedRoverBeforeItsRider() {
        assertEquals(1, HostileTerrastalkerRules.roverEnergyDamage(true));
        assertEquals(0, HostileTerrastalkerRules.playerDamage(true));
    }

    @Test
    void autocannonHitDamagesPlayerWhenThereIsNoRover() {
        assertEquals(0, HostileTerrastalkerRules.roverEnergyDamage(false));
        assertEquals(12, HostileTerrastalkerRules.playerDamage(false));
    }

}
