package io.github.finalparadox.client;

final class TerrastalkerRoverTrackingState {
    static final int NO_ROVER = -1;

    private int lastKnownRoverId = NO_ROVER;

    void observe(int roverId) {
        if (roverId != NO_ROVER) lastKnownRoverId = roverId;
    }

    int lastKnownRoverId() {
        return lastKnownRoverId;
    }

    void reset() {
        lastKnownRoverId = NO_ROVER;
    }
}
