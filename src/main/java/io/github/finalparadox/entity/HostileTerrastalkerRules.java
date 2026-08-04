package io.github.finalparadox.entity;

/** Pure source constants shared by the encounter controller and hostile rover. */
public final class HostileTerrastalkerRules {
    private HostileTerrastalkerRules() {
    }

    public static int spawnCountForRound(int round) {
        return round == 5 ? 2 : 0;
    }

    public static int maxHealth(int activePlayers) {
        return activePlayers <= 1 ? 200 : 300;
    }

    public static int overheatThreshold(int activePlayers) {
        return activePlayers <= 1 ? 350 : 200;
    }

    public static int roverEnergyDamage(boolean ridingTerrastalker) {
        return ridingTerrastalker ? 1 : 0;
    }

    public static int playerDamage(boolean ridingTerrastalker) {
        return ridingTerrastalker ? 0 : 12;
    }
}
