package com.eventify.util;

import com.eventify.model.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for converting Event objects to/from JSON files using Google Gson.
 */
public class JsonUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private JsonUtil() {
    }

    public static void exportEventToFile(Event event, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(event, writer);
        }
    }

    public static void exportEventListToFile(List<Event> events, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(events, writer);
        }
    }

    public static List<Event> importEventsFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            com.google.gson.JsonElement element = com.google.gson.JsonParser.parseReader(reader);
            if (element == null || element.isJsonNull()) {
                return Collections.emptyList();
            }
            if (element.isJsonArray()) {
                Type listType = new TypeToken<List<Event>>() {}.getType();
                return GSON.fromJson(element, listType);
            } else if (element.isJsonObject()) {
                Event single = GSON.fromJson(element, Event.class);
                return single != null ? List.of(single) : Collections.emptyList();
            }
            return Collections.emptyList();
        }
    }

    public static String toJsonString(Object object) {
        return GSON.toJson(object);
    }
}
