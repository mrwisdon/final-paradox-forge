package io.github.finalparadox.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HostileTerrastalkerRulesTest {
    @Test
    void phaseFiveRoundThreeSpawnsOneTerrastalker() {
        assertEquals(1, HostileTerrastalkerRules.spawnCountForRound(3));
    }

    @Test
    void phaseFiveRoundFiveSpawnsTwoTerrastalkers() {
        assertEquals(2, HostileTerrastalkerRules.spawnCountForRound(5));
    }

    @Test
    void waitingRoundsDoNotSpawnDuplicates() {
        assertEquals(0, HostileTerrastalkerRules.spawnCountForRound(4));
        assertEquals(0, HostileTerrastalkerRules.spawnCountForRound(6));
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
