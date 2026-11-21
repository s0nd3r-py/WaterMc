package me.s0nd3r.watermc.diagnostics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAuditTest {

    @Test
    void serverCodeHasNoPluginOnTheStack() {
        assertNull(SecurityAudit.callerPlugin(), "test classes are not loaded by a plugin class loader");
        assertEquals("server or command", SecurityAudit.by(null));
        assertEquals("plugin Evil", SecurityAudit.by("Evil"));
    }

    @Test
    void auditCallsAreSafeWithStackTracesOn() {
        boolean old = SecurityAudit.stackTraces;
        try {
            SecurityAudit.stackTraces = true;
            assertDoesNotThrow(() -> {
                SecurityAudit.opGranted("Alice");
                SecurityAudit.opRemoved("Alice");
                SecurityAudit.consoleCommand("say hi");
                SecurityAudit.gameModeChanged("Alice", "survival", "creative", "COMMAND");
                SecurityAudit.gameModeChanged("Alice", "survival", "adventure", "PLUGIN");
                SecurityAudit.pluginDisabled("Example");
            });
        } finally {
            SecurityAudit.stackTraces = old;
        }
    }
}
