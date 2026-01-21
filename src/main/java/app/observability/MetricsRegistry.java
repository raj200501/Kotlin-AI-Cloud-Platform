package app.observability;

import app.json.JsonUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class MetricsRegistry {
    private static final String ENABLED_ENV = "ENABLE_OBSERVABILITY";
    private static final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> timers = new ConcurrentHashMap<>();
    private static final Instant startedAt = Instant.now();

    private MetricsRegistry() {}

    public static boolean isEnabled() {
        return "1".equals(System.getenv(ENABLED_ENV));
    }

    public static void increment(String name) {
        if (!isEnabled()) {
            return;
        }
        counters.computeIfAbsent(name, key -> new AtomicLong()).incrementAndGet();
    }

    public static void recordDuration(String name, long durationMs) {
        if (!isEnabled()) {
            return;
        }
        timers.computeIfAbsent(name, key -> new AtomicLong()).addAndGet(durationMs);
    }

    public static long nowMs() {
        return System.nanoTime();
    }

    public static long elapsedMs(long startNano) {
        return Duration.ofNanos(System.nanoTime() - startNano).toMillis();
    }

    public static String snapshot() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("uptime_seconds", Duration.between(startedAt, Instant.now()).toSeconds());
        payload.put("counters", materialize(counters));
        payload.put("timers_ms", materialize(timers));
        return JsonUtil.toJsonObject(payload);
    }

    private static Map<String, Long> materialize(Map<String, AtomicLong> source) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map.Entry<String, AtomicLong> entry : source.entrySet()) {
            result.put(entry.getKey(), entry.getValue().get());
        }
        return Collections.unmodifiableMap(result);
    }
}
