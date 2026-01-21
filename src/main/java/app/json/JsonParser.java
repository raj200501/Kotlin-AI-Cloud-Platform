package app.json;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {
    private final String input;
    private int index;

    public JsonParser(String input) {
        this.input = input == null ? "" : input.trim();
        this.index = 0;
    }

    public static JsonValue parse(String input) {
        return new JsonParser(input).parseValue();
    }

    public JsonValue parseValue() {
        skipWhitespace();
        if (index >= input.length()) {
            throw new IllegalArgumentException("Empty JSON input");
        }
        char current = input.charAt(index);
        if (current == '{') {
            return parseObject();
        }
        if (current == '[') {
            return parseArray();
        }
        if (current == '"') {
            return new JsonString(parseString());
        }
        if (current == 't' || current == 'f') {
            return parseBoolean();
        }
        if (current == 'n') {
            return parseNull();
        }
        if (current == '-' || Character.isDigit(current)) {
            return parseNumber();
        }
        throw new IllegalArgumentException("Unexpected token at position " + index + ": " + current);
    }

    private JsonObject parseObject() {
        expect('{');
        Map<String, JsonValue> values = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            expect('}');
            return new JsonObject(values);
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonValue value = parseValue();
            values.put(key, value);
            skipWhitespace();
            if (peek('}')) {
                expect('}');
                break;
            }
            expect(',');
        }
        return new JsonObject(values);
    }

    private JsonArray parseArray() {
        expect('[');
        List<JsonValue> values = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            expect(']');
            return new JsonArray(values);
        }
        while (true) {
            JsonValue value = parseValue();
            values.add(value);
            skipWhitespace();
            if (peek(']')) {
                expect(']');
                break;
            }
            expect(',');
        }
        return new JsonArray(values);
    }

    private JsonValue parseBoolean() {
        if (input.startsWith("true", index)) {
            index += 4;
            return new JsonBoolean(true);
        }
        if (input.startsWith("false", index)) {
            index += 5;
            return new JsonBoolean(false);
        }
        throw new IllegalArgumentException("Invalid boolean at position " + index);
    }

    private JsonValue parseNull() {
        if (input.startsWith("null", index)) {
            index += 4;
            return JsonNull.INSTANCE;
        }
        throw new IllegalArgumentException("Invalid null at position " + index);
    }

    private JsonValue parseNumber() {
        int start = index;
        if (peek('-')) {
            index++;
        }
        while (index < input.length() && Character.isDigit(input.charAt(index))) {
            index++;
        }
        if (peek('.')) {
            index++;
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
        }
        if (peek('e') || peek('E')) {
            index++;
            if (peek('+') || peek('-')) {
                index++;
            }
            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                index++;
            }
        }
        String number = input.substring(start, index);
        try {
            return new JsonNumber(new BigDecimal(number));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: " + number, ex);
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder builder = new StringBuilder();
        while (index < input.length()) {
            char current = input.charAt(index++);
            if (current == '"') {
                return builder.toString();
            }
            if (current == '\\') {
                if (index >= input.length()) {
                    throw new IllegalArgumentException("Invalid escape sequence");
                }
                char escaped = input.charAt(index++);
                switch (escaped) {
                    case '"' -> builder.append('"');
                    case '\\' -> builder.append('\\');
                    case '/' -> builder.append('/');
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> {
                        if (index + 4 > input.length()) {
                            throw new IllegalArgumentException("Invalid unicode escape");
                        }
                        String hex = input.substring(index, index + 4);
                        builder.append((char) Integer.parseInt(hex, 16));
                        index += 4;
                    }
                    default -> throw new IllegalArgumentException("Unsupported escape: \\" + escaped);
                }
            } else {
                builder.append(current);
            }
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private void skipWhitespace() {
        while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
            index++;
        }
    }

    private void expect(char expected) {
        skipWhitespace();
        if (index >= input.length() || input.charAt(index) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + index);
        }
        index++;
    }

    private boolean peek(char expected) {
        skipWhitespace();
        return index < input.length() && input.charAt(index) == expected;
    }
}
