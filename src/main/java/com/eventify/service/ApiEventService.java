package com.eventify.service;

import com.eventify.model.ApiEvent;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service handling HTTP API requests using Java's HttpClient, HttpRequest, and HttpResponse.
 * Demonstrates full error handling (HTTP error, timeout, network error, invalid JSON, empty response)
 * as well as a local JSON fallback for reliable APL lab demonstrations.
 */
public class ApiEventService {

    public record ApiFetchResult(
            boolean success,
            boolean usedFallback,
            int statusCode,
            String message,
            String rawJsonPreview,
            List<ApiEvent> events
    ) {
    }

    private static final String DEFAULT_API_URL = "https://jsonplaceholder.typicode.com/posts?_limit=6";

    private static final String MOCK_FALLBACK_JSON = """
        [
          {
            "id": 101,
            "title": "National Collegiate Hackathon 2026",
            "category": "Competition",
            "date": "2026-11-05",
            "venue": "Innovation Hub Auditorium",
            "organizer": "CSE Society & IEEE Branch",
            "maxParticipants": 150,
            "body": "24-hour inter-university software hackathon focusing on EdTech and Smart Campus solutions."
          },
          {
            "id": 102,
            "title": "Cloud Native & Microservices Bootcamp",
            "category": "Workshop",
            "date": "2026-11-12",
            "venue": "Software Lab 302",
            "organizer": "Developer Student Club",
            "maxParticipants": 80,
            "body": "Practical workshop on containerization, CI/CD pipelines, and distributed systems."
          },
          {
            "id": 103,
            "title": "Annual University Cultural Fest & Robotics Show",
            "category": "Cultural Event",
            "date": "2026-11-20",
            "venue": "Central Campus Plaza",
            "organizer": "University Cultural & Robotics Club",
            "maxParticipants": 300,
            "body": "Combined showcase of autonomous line-follower robots and evening cultural performances."
          },
          {
            "id": 104,
            "title": "Higher Studies & Research Scholarship Seminar",
            "category": "Seminar",
            "date": "2026-11-28",
            "venue": "Seminar Hall B",
            "organizer": "Alumni Association",
            "maxParticipants": 120,
            "body": "Guidance on graduate admissions, research publications, and fellowship opportunities."
          }
        ]
        """;

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ApiEventService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String getDefaultApiUrl() {
        return DEFAULT_API_URL;
    }

    public ApiFetchResult fetchEventsFromApi(String urlString) {
        String targetUrl = (urlString == null || urlString.isBlank()) ? DEFAULT_API_URL : urlString.trim();

        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (IllegalArgumentException e) {
            return new ApiFetchResult(false, false, 0, "Invalid API URL syntax: " + e.getMessage(), "", List.of());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(6))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status < 200 || status >= 300) {
                return new ApiFetchResult(
                        false, false, status,
                        "HTTP Error " + status + " received from server.",
                        body != null ? body : "",
                        List.of()
                );
            }

            if (body == null || body.isBlank()) {
                return new ApiFetchResult(
                        false, false, status,
                        "Empty response body received from API.",
                        "",
                        List.of()
                );
            }

            List<ApiEvent> parsed = parseJsonPayload(body);
            if (parsed.isEmpty()) {
                return new ApiFetchResult(
                        false, false, status,
                        "API returned valid JSON, but no event records were found.",
                        body,
                        List.of()
                );
            }

            return new ApiFetchResult(
                    true, false, status,
                    "HTTP " + status + " OK — Successfully fetched and parsed " + parsed.size() + " API events.",
                    body,
                    parsed
            );

        } catch (HttpTimeoutException e) {
            return new ApiFetchResult(
                    false, false, 408,
                    "API Request Timed Out after 6 seconds. Use 'Load Local JSON Fallback' if offline.",
                    "",
                    List.of()
            );
        } catch (JsonSyntaxException e) {
            return new ApiFetchResult(
                    false, false, 200,
                    "Invalid JSON format in API response: " + e.getMessage(),
                    "",
                    List.of()
            );
        } catch (IOException e) {
            return new ApiFetchResult(
                    false, false, 0,
                    "Network Error: Unable to reach API (" + e.getMessage() + "). Try 'Load Local JSON Fallback'.",
                    "",
                    List.of()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ApiFetchResult(
                    false, false, 0,
                    "API request was interrupted.",
                    "",
                    List.of()
            );
        }
    }

    public ApiFetchResult loadFallbackEvents() {
        List<ApiEvent> parsed = parseJsonPayload(MOCK_FALLBACK_JSON);
        return new ApiFetchResult(
                true, true, 200,
                "Loaded " + parsed.size() + " university events from built-in Local JSON Fallback.",
                MOCK_FALLBACK_JSON,
                parsed
        );
    }

    public List<ApiEvent> parseJsonPayload(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (root == null || root.isJsonNull()) {
            return List.of();
        }

        JsonArray array;
        if (root.isJsonArray()) {
            array = root.getAsJsonArray();
        } else if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("events") && obj.get("events").isJsonArray()) {
                array = obj.getAsJsonArray("events");
            } else {
                array = new JsonArray();
                array.add(obj);
            }
        } else {
            throw new JsonSyntaxException("Expected JSON array or object");
        }

        String[] categories = {"Programming Contest", "Workshop", "Seminar", "Competition", "Club Program", "Cultural Event"};
        String[] venues = {"CSE Auditorium 101", "Software Lab 204", "Central Conference Hall", "Innovation Center", "Robotics Arena", "Campus Amphitheater"};
        String[] organizers = {"CSE Computer Club", "IEEE Student Branch", "Software Engineering Society", "Robotics Club", "Department of CSE", "University Debate Forum"};

        List<ApiEvent> list = new ArrayList<>();
        int idx = 0;
        for (JsonElement el : array) {
            if (!el.isJsonObject()) continue;
            ApiEvent item = gson.fromJson(el, ApiEvent.class);
            if (item.getId() <= 0) {
                item.setId(idx + 1);
            }
            if (item.getTitle() == null || item.getTitle().isBlank()) {
                item.setTitle("University Tech Event #" + item.getId());
            } else if (item.getTitle().length() > 55) {
                item.setTitle(item.getTitle().substring(0, 55));
            }
            if (!el.getAsJsonObject().has("category")) {
                item.setCategory(categories[idx % categories.length]);
            }
            if (!el.getAsJsonObject().has("date")) {
                item.setDate(String.format("2026-11-%02d", 10 + (idx % 18)));
            }
            if (!el.getAsJsonObject().has("venue")) {
                item.setVenue(venues[idx % venues.length]);
            }
            if (!el.getAsJsonObject().has("organizer")) {
                item.setOrganizer(organizers[idx % organizers.length]);
            }
            if (!el.getAsJsonObject().has("maxParticipants")) {
                item.setMaxParticipants(60 + (idx * 20));
            }
            list.add(item);
            idx++;
        }
        return list;
    }
}
