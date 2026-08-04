package io.github.finalparadox.entity;

/** Pure source constants shared by the encounter controller and hostile rover. */
public final class HostileTerrastalkerRules {
    private HostileTerrastalkerRules() {
    }

    public static int spawnCountForRound(int round) {
        return switch (round) {
            case 3 -> 1;
            case 4 -> 2;
            default -> 0;
        };
    }

    public static int gunHitCooldownTicks() {
        return 10;
    }

    public static boolean canApplyGunHit(long currentTick, long lastHitTick) {
        return currentTick - lastHitTick >= gunHitCooldownTicks();
    }

    public static float turnAimToward(float current, float target, float maxStep) {
        float difference = target - current;
        while (difference <= -180.0F) difference += 360.0F;
        while (difference > 180.0F) difference -= 360.0F;
        float result = current + Math.max(-maxStep, Math.min(maxStep, difference));
        while (result <= -180.0F) result += 360.0F;
        while (result > 180.0F) result -= 360.0F;
        return result;
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
