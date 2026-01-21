package app;

import app.ai.SentimentAnalyzer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SentimentAnalyzerTest {
    @Test
    void returnsNeutralForEmptyText() {
        SentimentAnalyzer.SentimentScore score = SentimentAnalyzer.analyze("");
        assertEquals("neutral", score.label());
        assertEquals(0, score.positives());
        assertEquals(0, score.negatives());
    }

    @Test
    void identifiesNegativeSentiment() {
        SentimentAnalyzer.SentimentScore score = SentimentAnalyzer.analyze("bad slow broken");
        assertEquals("negative", score.label());
    }
}
