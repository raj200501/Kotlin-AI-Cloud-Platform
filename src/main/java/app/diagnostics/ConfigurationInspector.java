package app.diagnostics;

import app.observability.MetricsRegistry;
import app.observability.OpenTelemetryScaffolding;
import app.observability.StructuredLogger;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfigurationInspector {
    private ConfigurationInspector() {}

    public static DiagnosticsReport inspect() {
        DiagnosticsReport report = new DiagnosticsReport();
        Map<String, Object> toggles = new LinkedHashMap<>();
        toggles.put("structured_logging", StructuredLogger.isEnabled());
        toggles.put("observability", MetricsRegistry.isEnabled());
        toggles.put("otel", OpenTelemetryScaffolding.initialize().isPresent());
        report.add("feature_toggles", toggles);
        report.add("java_version", System.getProperty("java.version"));
        report.add("available_processors", Runtime.getRuntime().availableProcessors());
        return report;
    }
}
