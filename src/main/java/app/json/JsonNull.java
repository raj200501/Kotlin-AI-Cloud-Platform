package app.json;

public final class JsonNull implements JsonValue {
    public static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {}

    @Override
    public ValueType type() {
        return ValueType.NULL;
    }

    @Override
    public String toJson() {
        return "null";
    }

    @Override
    public String toString() {
        return toJson();
    }
}
