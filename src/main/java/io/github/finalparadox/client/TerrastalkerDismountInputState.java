package io.github.finalparadox.client;

final class TerrastalkerDismountInputState {
    private static final int DISMOUNT_WINDOW_TICKS = 10;
    private static final int RECENT_ROVER_GRACE_TICKS = 2;

    private boolean wasShiftDown;
    private int pendingDismountTicks;
    private int recentRoverTicks;

    boolean tick(boolean shiftDown, boolean ridingRover, boolean screenOpen) {
        boolean pressed = shiftDown && !wasShiftDown;
        wasShiftDown = shiftDown;

        if (screenOpen) {
            pendingDismountTicks = 0;
            recentRoverTicks = 0;
            return false;
        }

        if (ridingRover) {
            recentRoverTicks = RECENT_ROVER_GRACE_TICKS;
        } else if (recentRoverTicks > 0) {
            recentRoverTicks--;
        }

        boolean eligible = ridingRover
                || recentRoverTicks > 0
                || pendingDismountTicks > 0;
        if (pressed && eligible) {
            pendingDismountTicks = pendingDismountTicks > 0
                    ? 0
                    : DISMOUNT_WINDOW_TICKS;
            return true;
        }

        if (pendingDismountTicks > 0) pendingDismountTicks--;
        return false;
    }

    void reset() {
        wasShiftDown = false;
        pendingDismountTicks = 0;
        recentRoverTicks = 0;
    }
}
