package app.analytics;

import java.util.List;
import java.util.Map;

public record KeywordResult(List<String> topKeywords, Map<String, Integer> frequency) {}
