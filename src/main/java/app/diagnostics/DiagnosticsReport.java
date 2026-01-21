package app.diagnostics;

import app.json.JsonUtil;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DiagnosticsReport {
    private final Map<String, Object> details = new LinkedHashMap<>();

    public DiagnosticsReport() {
        details.put("generated_at", Instant.now().toString());
    }

    public void add(String key, Object value) {
        details.put(key, value);
    }

    public Map<String, Object> asMap() {
        return details;
    }

    public String toJson() {
        return JsonUtil.toJsonObject(details);
    }
}
