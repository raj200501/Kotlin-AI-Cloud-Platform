package app.json;

public final class JsonString implements JsonValue {
    private final String value;

    public JsonString(String value) {
        this.value = value;
    }

    @Override
    public ValueType type() {
        return ValueType.STRING;
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public String toJson() {
        return '"' + JsonUtil.escape(value) + '"';
    }

    @Override
    public String toString() {
        return toJson();
    }
}
