package app;

import app.observability.MetricsRegistry;
import app.observability.StructuredLogger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservabilityTest {
    @Test
    void metricsSnapshotHasUptime() {
        String snapshot = MetricsRegistry.snapshot();
        assertTrue(snapshot.contains("uptime_seconds"));
    }

    @Test
    void structuredLoggingIsDisabledByDefault() {
        assertFalse(StructuredLogger.isEnabled());
    }
}
