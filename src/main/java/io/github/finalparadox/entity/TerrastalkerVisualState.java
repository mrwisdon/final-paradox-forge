package io.github.finalparadox.entity;

/** Synchronized pose state consumed by the shared compound Terrastalker renderer. */
public interface TerrastalkerVisualState {
    int getGaitFrame();

    float getMovementYaw();

    float getPreviousMovementYaw();

    float getTurretYaw();

    float getPreviousTurretYaw();

    float getCannonPitch();

    float getPreviousCannonPitch();

    float getCabinYaw();

    float getPreviousCabinYaw();

    boolean isMeltingDown();

    default boolean isHostileVisual() {
        return false;
    }

    default int sourceVisibleParts() {
        return TerrastalkerRoverEntity.SOURCE_VISIBLE_PARTS;
    }

    default boolean showMountHint() {
        return true;
    }
}
