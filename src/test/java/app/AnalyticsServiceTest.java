package app;

import app.analytics.AnalyticsService;
import app.analytics.ForecastResult;
import app.analytics.KeywordResult;
import app.analytics.SentimentResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsServiceTest {
    private final AnalyticsService analytics = new AnalyticsService();

    @Test
    void forecastsLinearTrend() {
        ForecastResult result = analytics.forecast(List.of(1.0, 2.0, 3.0, 4.0), 2);
        assertEquals(2, result.forecast().size());
        assertTrue(result.slope() > 0.9 && result.slope() < 1.1);
        assertEquals(5.0, result.forecast().get(0));
    }

    @Test
    void analyzesSentiment() {
        SentimentResult result = analytics.sentiment("great fast reliable");
        assertEquals("positive", result.label());
        assertTrue(result.score() > 0);
    }

    @Test
    void extractsKeywords() {
        KeywordResult result = analytics.keywords("cloud ai cloud platform platform", 2);
        assertEquals(2, result.topKeywords().size());
        assertTrue(result.frequency().containsKey("cloud"));
    }
}
