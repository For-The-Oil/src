package io.github.server.server_engine.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;

import io.github.shared.data.gameobject.Deck;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parse a JSON string into a HashMap<String, Deck>
     */
    public static HashMap<String, Deck> parseDecksJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(json, mapper.getTypeFactory()
                .constructMapType(HashMap.class, String.class, Deck.class));
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * Convert a HashMap<String, Deck> back to JSON
     */
    public static String toJson(HashMap<String, Deck> decks) {
        try {
            return mapper.writeValueAsString(decks);
        } catch (IOException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
