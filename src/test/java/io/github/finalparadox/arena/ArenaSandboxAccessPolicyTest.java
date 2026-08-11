package io.github.finalparadox.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArenaSandboxAccessPolicyTest {
    @Test
    void sandboxCannotBypassFightEscapeLock() {
        assertFalse(ArenaSandboxAccessPolicy.mayRequest(true, true));
        assertTrue(ArenaSandboxAccessPolicy.mayRequest(true, false));
        assertTrue(ArenaSandboxAccessPolicy.mayRequest(false, true));
        assertTrue(ArenaSandboxAccessPolicy.mayRequest(false, false));
    }
}
