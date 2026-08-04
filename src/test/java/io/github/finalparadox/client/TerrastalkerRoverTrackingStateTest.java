package io.github.finalparadox.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TerrastalkerRoverTrackingStateTest {
    @Test
    void keepsLastRoverIdWhenClientTemporarilyLosesPassengerLink() {
        TerrastalkerRoverTrackingState state = new TerrastalkerRoverTrackingState();

        state.observe(42);
        state.observe(TerrastalkerRoverTrackingState.NO_ROVER);

        assertEquals(42, state.lastKnownRoverId());
    }

    @Test
    void replacesLastRoverIdAfterMountingAnotherRover() {
        TerrastalkerRoverTrackingState state = new TerrastalkerRoverTrackingState();

        state.observe(42);
        state.observe(84);

        assertEquals(84, state.lastKnownRoverId());
    }

    @Test
    void resetForgetsLastRoverId() {
        TerrastalkerRoverTrackingState state = new TerrastalkerRoverTrackingState();

        state.observe(42);
        state.reset();

        assertEquals(TerrastalkerRoverTrackingState.NO_ROVER, state.lastKnownRoverId());
    }
}
