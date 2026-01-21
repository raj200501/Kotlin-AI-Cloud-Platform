package app.json;

public final class JsonBoolean implements JsonValue {
    private final boolean value;

    public JsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public ValueType type() {
        return ValueType.BOOLEAN;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public String toJson() {
        return value ? "true" : "false";
    }

    @Override
    public String toString() {
        return toJson();
    }
}
