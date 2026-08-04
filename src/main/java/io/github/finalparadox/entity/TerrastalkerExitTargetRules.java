package io.github.finalparadox.entity;

final class TerrastalkerExitTargetRules {
    private TerrastalkerExitTargetRules() {
    }

    static boolean canRecover(
            boolean currentVehicle,
            boolean listedPassenger,
            boolean ownedAndNearby
    ) {
        return currentVehicle || listedPassenger || ownedAndNearby;
    }

    static boolean shouldRemoveStalePassenger(
            boolean playerPointsToRover,
            boolean roverListsPlayer
    ) {
        return !playerPointsToRover && roverListsPlayer;
    }
}
