package app.analytics;

public record SentimentResult(double score, String label, int positives, int negatives) {}
