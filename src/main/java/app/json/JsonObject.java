package app.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonObject implements JsonValue {
    private final Map<String, JsonValue> values;

    public JsonObject(Map<String, JsonValue> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @Override
    public ValueType type() {
        return ValueType.OBJECT;
    }

    @Override
    public Map<String, JsonValue> asObject() {
        return values;
    }

    public JsonValue get(String key) {
        return values.get(key);
    }

    public String getString(String key, String defaultValue) {
        JsonValue value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value.type() == ValueType.STRING) {
            return value.asString();
        }
        return defaultValue;
    }

    public double getNumber(String key, double defaultValue) {
        JsonValue value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value.type() == ValueType.NUMBER) {
            return value.asNumber();
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        JsonValue value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value.type() == ValueType.BOOLEAN) {
            return value.asBoolean();
        }
        return defaultValue;
    }

    @Override
    public String toJson() {
        StringBuilder builder = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, JsonValue> entry : values.entrySet()) {
            if (index++ > 0) {
                builder.append(',');
            }
            builder.append('"').append(JsonUtil.escape(entry.getKey())).append('"').append(':');
            builder.append(entry.getValue().toJson());
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
