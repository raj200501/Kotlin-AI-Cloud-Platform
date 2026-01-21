package app;

import app.json.JsonArray;
import app.json.JsonObject;
import app.json.JsonParser;
import app.json.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonParserTest {
    @Test
    void parsesObjectWithMixedTypes() {
        String input = "{\"name\":\"cloud\",\"count\":3,\"active\":true,\"tags\":[\"ai\",\"infra\"]}";
        JsonValue value = JsonParser.parse(input);
        JsonObject object = (JsonObject) value;
        assertEquals("cloud", object.getString("name", ""));
        assertEquals(3.0, object.getNumber("count", 0));
        assertEquals(true, object.getBoolean("active", false));
        JsonArray tags = (JsonArray) object.get("tags");
        assertEquals(2, tags.size());
        assertEquals("ai", tags.get(0).asString());
    }

    @Test
    void parsesNestedStructures() {
        String input = "{\"nodes\":[{\"id\":\"n1\",\"cpu\":4}],\"meta\":{\"region\":\"us\"}}";
        JsonObject object = (JsonObject) JsonParser.parse(input);
        JsonArray nodes = (JsonArray) object.get("nodes");
        JsonObject node = (JsonObject) nodes.get(0);
        assertEquals("n1", node.getString("id", ""));
        JsonObject meta = (JsonObject) object.get("meta");
        assertEquals("us", meta.getString("region", ""));
    }

    @Test
    void rejectsInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("{"));
        assertThrows(IllegalArgumentException.class, () -> JsonParser.parse("[1,2"));
    }
}
