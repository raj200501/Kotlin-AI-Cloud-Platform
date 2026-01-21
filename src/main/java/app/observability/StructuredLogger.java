package app.observability;

import app.json.JsonUtil;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StructuredLogger {
    private static final String ENABLED_ENV = "ENABLE_STRUCTURED_LOGGING";

    private StructuredLogger() {}

    public static boolean isEnabled() {
        return "1".equals(System.getenv(ENABLED_ENV));
    }

    public static void info(String message, Map<String, Object> fields) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("level", "INFO");
        payload.put("message", message);
        if (fields != null) {
            payload.putAll(fields);
        }
        System.out.println(JsonUtil.toJsonObject(payload));
    }

    public static void warn(String message, Map<String, Object> fields) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timestamp", Instant.now().toString());
        payload.put("level", "WARN");
        payload.put("message", message);
        if (fields != null) {
            payload.putAll(fields);
        }
        System.out.println(JsonUtil.toJsonObject(payload));
    }

    public static void request(String service, String path, String method, int status, long durationMs) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("service", service);
        fields.put("path", path);
        fields.put("method", method);
        fields.put("status", status);
        fields.put("duration_ms", durationMs);
        info("request", fields);
    }
}
