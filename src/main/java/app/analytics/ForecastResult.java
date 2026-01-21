package app.analytics;

import java.util.List;

public record ForecastResult(List<Double> forecast, double slope) {}
