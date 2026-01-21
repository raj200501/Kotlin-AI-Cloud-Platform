package app.json;

import java.util.List;
import java.util.Map;

public interface JsonValue {
    ValueType type();

    default String asString() {
        throw new IllegalStateException("Not a string: " + type());
    }

    default double asNumber() {
        throw new IllegalStateException("Not a number: " + type());
    }

    default boolean asBoolean() {
        throw new IllegalStateException("Not a boolean: " + type());
    }

    default Map<String, JsonValue> asObject() {
        throw new IllegalStateException("Not an object: " + type());
    }

    default List<JsonValue> asArray() {
        throw new IllegalStateException("Not an array: " + type());
    }

    String toJson();

    enum ValueType {
        OBJECT,
        ARRAY,
        STRING,
        NUMBER,
        BOOLEAN,
        NULL
    }
}
