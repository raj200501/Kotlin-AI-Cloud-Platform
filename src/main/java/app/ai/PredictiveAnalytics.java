package app.ai;

import java.util.ArrayList;
import java.util.List;

public final class PredictiveAnalytics {
    private PredictiveAnalytics() {}

    public static List<Double> forecast(List<Double> history, int horizon) {
        if (history == null || history.size() < 2) {
            throw new IllegalArgumentException("At least two data points required");
        }
        if (horizon <= 0) {
            throw new IllegalArgumentException("Horizon must be positive");
        }
        double slope = computeSlope(history);
        double last = history.get(history.size() - 1);
        List<Double> result = new ArrayList<>();
        for (int i = 1; i <= horizon; i++) {
            result.add(last + slope * i);
        }
        return result;
    }

    public static double computeSlope(List<Double> series) {
        int n = series.size();
        double xSum = 0;
        double ySum = 0;
        double xxSum = 0;
        double xySum = 0;
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = series.get(i);
            xSum += x;
            ySum += y;
            xxSum += x * x;
            xySum += x * y;
        }
        double numerator = n * xySum - xSum * ySum;
        double denominator = n * xxSum - xSum * xSum;
        if (denominator == 0) {
            return 0;
        }
        return numerator / denominator;
    }
}
