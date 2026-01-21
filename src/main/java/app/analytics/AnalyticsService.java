package app.analytics;

import app.ai.KeywordExtractor;
import app.ai.PredictiveAnalytics;
import app.ai.SentimentAnalyzer;
import java.util.List;

public final class AnalyticsService {
    public ForecastResult forecast(List<Double> series, int horizon) {
        List<Double> forecast = PredictiveAnalytics.forecast(series, horizon);
        double slope = PredictiveAnalytics.computeSlope(series);
        return new ForecastResult(forecast, slope);
    }

    public SentimentResult sentiment(String text) {
        SentimentAnalyzer.SentimentScore score = SentimentAnalyzer.analyze(text);
        return new SentimentResult(score.score(), score.label(), score.positives(), score.negatives());
    }

    public KeywordResult keywords(String text, int limit) {
        return new KeywordResult(KeywordExtractor.topKeywords(text, limit), KeywordExtractor.frequency(text));
    }
}
