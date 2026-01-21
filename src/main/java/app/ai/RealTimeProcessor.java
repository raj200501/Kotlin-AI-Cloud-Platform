package app.ai;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class RealTimeProcessor {
    private final int windowSize;
    private final Deque<Double> window = new ArrayDeque<>();
    private Instant lastUpdated = Instant.now();

    public RealTimeProcessor(int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
        this.windowSize = windowSize;
    }

    public void add(double value) {
        window.addLast(value);
        if (window.size() > windowSize) {
            window.removeFirst();
        }
        lastUpdated = Instant.now();
    }

    public double movingAverage() {
        return window.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double volatility() {
        double avg = movingAverage();
        return Math.sqrt(window.stream().mapToDouble(v -> Math.pow(v - avg, 2)).average().orElse(0));
    }

    public Duration sinceLastUpdate() {
        return Duration.between(lastUpdated, Instant.now());
    }

    public List<Double> snapshot() {
        return window.stream().toList();
    }
}
