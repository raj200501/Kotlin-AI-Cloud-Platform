package app.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class JsonUtil {
    private JsonUtil() {}

    public static String escape(String value) {
        StringBuilder builder = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                }
            }
        }
        return builder.toString();
    }

    public static JsonObject object(Map<String, JsonValue> values) {
        return new JsonObject(values);
    }

    public static JsonArray array(List<JsonValue> values) {
        return new JsonArray(values);
    }

    public static JsonValue string(String value) {
        return new JsonString(value);
    }

    public static JsonValue number(double value) {
        return new JsonNumber(java.math.BigDecimal.valueOf(value));
    }

    public static JsonValue bool(boolean value) {
        return new JsonBoolean(value);
    }

    public static JsonValue nullValue() {
        return JsonNull.INSTANCE;
    }

    public static String toJsonObject(Map<String, Object> map) {
        Map<String, JsonValue> values = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            values.put(entry.getKey(), fromJava(entry.getValue()));
        }
        return new JsonObject(values).toJson();
    }

    public static JsonValue fromJava(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof JsonValue jsonValue) {
            return jsonValue;
        }
        if (value instanceof String str) {
            return new JsonString(str);
        }
        if (value instanceof Number num) {
            return new JsonNumber(new java.math.BigDecimal(num.toString()));
        }
        if (value instanceof Boolean bool) {
            return new JsonBoolean(bool);
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, JsonValue> converted = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), fromJava(entry.getValue()));
            }
            return new JsonObject(converted);
        }
        if (value instanceof List<?> list) {
            return new JsonArray(list.stream().map(JsonUtil::fromJava).collect(Collectors.toList()));
        }
        return new JsonString(value.toString());
    }
}
