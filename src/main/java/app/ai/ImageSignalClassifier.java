package app.ai;

import java.util.List;

public final class ImageSignalClassifier {
    private ImageSignalClassifier() {}

    public static String classify(List<Integer> grayscalePixels) {
        if (grayscalePixels == null || grayscalePixels.isEmpty()) {
            return "unknown";
        }
        double avg = grayscalePixels.stream().mapToInt(Integer::intValue).average().orElse(0);
        if (avg > 200) {
            return "bright";
        }
        if (avg > 100) {
            return "normal";
        }
        return "dark";
    }
}
