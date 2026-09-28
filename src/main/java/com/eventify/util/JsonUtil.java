package com.eventify.util;

import com.eventify.model.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for converting Event objects to/from JSON files using Jackson ObjectMapper.
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtil() {
    }

    public static void exportEventToFile(Event event, File file) throws IOException {
        MAPPER.writeValue(file, event);
    }

    public static void exportEventListToFile(List<Event> events, File file) throws IOException {
        MAPPER.writeValue(file, events);
    }

    public static List<Event> importEventsFromFile(File file) throws IOException {
        JsonNode root = MAPPER.readTree(file);
        if (root == null || root.isNull() || root.isMissingNode()) {
            return Collections.emptyList();
        }
        if (root.isArray()) {
            return MAPPER.convertValue(root, new TypeReference<List<Event>>() {});
        } else if (root.isObject()) {
            Event single = MAPPER.treeToValue(root, Event.class);
            return single != null ? List.of(single) : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public static String toJsonString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (IOException e) {
            return "{}";
        }
    }
}
