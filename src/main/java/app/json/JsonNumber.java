package app.json;

import java.math.BigDecimal;

public final class JsonNumber implements JsonValue {
    private final BigDecimal value;

    public JsonNumber(BigDecimal value) {
        this.value = value;
    }

    @Override
    public ValueType type() {
        return ValueType.NUMBER;
    }

    @Override
    public double asNumber() {
        return value.doubleValue();
    }

    public BigDecimal asBigDecimal() {
        return value;
    }

    @Override
    public String toJson() {
        return value.stripTrailingZeros().toPlainString();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
