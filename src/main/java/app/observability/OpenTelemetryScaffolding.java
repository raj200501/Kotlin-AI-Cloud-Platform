package app.observability;

import java.util.Optional;

public final class OpenTelemetryScaffolding {
    private static final String ENABLED_ENV = "ENABLE_OTEL";

    private OpenTelemetryScaffolding() {}

    public static Optional<String> initialize() {
        if (!"1".equals(System.getenv(ENABLED_ENV))) {
            return Optional.empty();
        }
        String serviceName = System.getenv().getOrDefault("OTEL_SERVICE_NAME", "kotlin-ai-cloud-platform");
        return Optional.of("OpenTelemetry scaffolding initialized for " + serviceName);
    }
}
