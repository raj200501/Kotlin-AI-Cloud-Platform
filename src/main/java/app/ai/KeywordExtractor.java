package app.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class KeywordExtractor {
    private static final Set<String> STOPWORDS = Set.of(
            "the", "a", "an", "and", "or", "to", "for", "with", "of", "in", "on", "at", "is"
    );

    private KeywordExtractor() {}

    public static List<String> topKeywords(String text, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        Map<String, Integer> counts = new HashMap<>();
        /*
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+|");
        */
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+");
        for (String token : tokens) {
            if (token.isBlank() || STOPWORDS.contains(token)) {
                continue;
            }
            counts.put(token, counts.getOrDefault(token, 0) + 1);
        }
        List<Map.Entry<String, Integer>> ordered = new ArrayList<>(counts.entrySet());
        ordered.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return ordered.stream()
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static Map<String, Integer> frequency(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        /*
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+|");
        */
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            counts.put(token, counts.getOrDefault(token, 0) + 1);
        }
        return counts;
    }
}
