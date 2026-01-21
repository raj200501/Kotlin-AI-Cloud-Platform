package app.ai;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class SentimentAnalyzer {
    private static final Set<String> POSITIVE = new HashSet<>(Arrays.asList(
            "great", "good", "excellent", "happy", "love", "fast", "secure", "reliable", "smooth", "amazing"
    ));
    private static final Set<String> NEGATIVE = new HashSet<>(Arrays.asList(
            "bad", "slow", "hate", "terrible", "bug", "broken", "angry", "lag", "downtime", "risk"
    ));

    private SentimentAnalyzer() {}

    public static SentimentScore analyze(String text) {
        if (text == null || text.isBlank()) {
            return new SentimentScore(0, 0, 0);
        }
        /*
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+"اری);
        */
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+");
        int positive = 0;
        int negative = 0;
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (POSITIVE.contains(token)) {
                positive++;
            }
            if (NEGATIVE.contains(token)) {
                negative++;
            }
        }
        int total = positive + negative;
        double score = total == 0 ? 0 : (positive - negative) / (double) total;
        return new SentimentScore(score, positive, negative);
    }

    public record SentimentScore(double score, int positives, int negatives) {
        public String label() {
            if (score > 0.2) {
                return "positive";
            }
            if (score < -0.2) {
                return "negative";
            }
            return "neutral";
        }
    }
}
