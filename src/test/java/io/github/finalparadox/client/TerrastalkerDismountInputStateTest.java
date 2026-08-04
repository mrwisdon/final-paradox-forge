package io.github.finalparadox.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrastalkerDismountInputStateTest {
    @Test
    void sendsSecondPressAfterVanillaTemporarilyClearsVehicle() {
        TerrastalkerDismountInputState state = new TerrastalkerDismountInputState();

        assertFalse(state.tick(false, true, false));
        assertTrue(state.tick(true, false, false));
        assertFalse(state.tick(false, false, false));
        assertTrue(state.tick(true, false, false));
    }

    @Test
    void sendsBothPressesWhenClientRemainsMounted() {
        TerrastalkerDismountInputState state = new TerrastalkerDismountInputState();

        assertFalse(state.tick(false, true, false));
        assertTrue(state.tick(true, true, false));
        assertFalse(state.tick(false, true, false));
        assertTrue(state.tick(true, true, false));
    }

    @Test
    void ignoresShiftWhenNoRoverWasRecentlyControlled() {
        TerrastalkerDismountInputState state = new TerrastalkerDismountInputState();

        assertFalse(state.tick(false, false, false));
        assertFalse(state.tick(true, false, false));
    }

    @Test
    void expiresPendingGestureAfterServerWindow() {
        TerrastalkerDismountInputState state = new TerrastalkerDismountInputState();

        assertFalse(state.tick(false, true, false));
        assertTrue(state.tick(true, true, false));
        for (int tick = 0; tick < 11; tick++) {
            state.tick(false, false, false);
        }
        assertFalse(state.tick(true, false, false));
    }

    @Test
    void openingScreenCancelsPendingGesture() {
        TerrastalkerDismountInputState state = new TerrastalkerDismountInputState();

        assertFalse(state.tick(false, true, false));
        assertTrue(state.tick(true, true, false));
        assertFalse(state.tick(false, false, true));
        assertFalse(state.tick(true, false, false));
    }
}
