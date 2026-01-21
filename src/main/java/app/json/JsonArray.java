package app.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JsonArray implements JsonValue {
    private final List<JsonValue> values;

    public JsonArray(List<JsonValue> values) {
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }

    @Override
    public ValueType type() {
        return ValueType.ARRAY;
    }

    @Override
    public List<JsonValue> asArray() {
        return values;
    }

    public JsonValue get(int index) {
        return values.get(index);
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toJson() {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i).toJson());
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
