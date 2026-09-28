package com.eventify.service;

import com.eventify.model.ApiEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * Service handling HTTP API requests using Java's HttpClient, HttpRequest, and HttpResponse,
 * and parsing JSON payloads using Jackson ObjectMapper and JsonNode.
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
            "organizer": "EVA PODDER",
            "maxParticipants": 150,
            "body": "24-hour inter-university software hackathon focusing on EdTech and Smart Campus solutions."
          },
          {
            "id": 102,
            "title": "Cloud Native & Microservices Bootcamp",
            "category": "Workshop",
            "date": "2026-11-12",
            "venue": "Software Lab 302",
            "organizer": "EVA PODDER",
            "maxParticipants": 80,
            "body": "Practical workshop on containerization, CI/CD pipelines, and distributed systems."
          },
          {
            "id": 103,
            "title": "Annual University Cultural Fest & Robotics Show",
            "category": "Cultural Event",
            "date": "2026-11-20",
            "venue": "Central Campus Plaza",
            "organizer": "EVA PODDER",
            "maxParticipants": 300,
            "body": "Combined showcase of autonomous line-follower robots and evening cultural performances."
          },
          {
            "id": 104,
            "title": "Higher Studies & Research Scholarship Seminar",
            "category": "Seminar",
            "date": "2026-11-28",
            "venue": "Seminar Hall B",
            "organizer": "EVA PODDER",
            "maxParticipants": 120,
            "body": "Guidance on graduate admissions, research publications, and fellowship opportunities."
          }
        ]
        """;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiEventService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                    "HTTP " + status + " OK — Successfully fetched and parsed " + parsed.size() + " API events via Jackson.",
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
        } catch (JsonProcessingException e) {
            return new ApiFetchResult(
                    false, false, 200,
                    "Invalid JSON format in API response (Jackson): " + e.getOriginalMessage(),
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
        try {
            List<ApiEvent> parsed = parseJsonPayload(MOCK_FALLBACK_JSON);
            return new ApiFetchResult(
                    true, true, 200,
                    "Loaded " + parsed.size() + " university events from Local JSON Fallback using Jackson ObjectMapper.",
                    MOCK_FALLBACK_JSON,
                    parsed
            );
        } catch (JsonProcessingException e) {
            return new ApiFetchResult(false, true, 500, "Fallback JSON error: " + e.getMessage(), MOCK_FALLBACK_JSON, List.of());
        }
    }

    public List<ApiEvent> parseJsonPayload(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        if (root == null || root.isNull() || root.isMissingNode()) {
            return List.of();
        }

        List<JsonNode> nodes = new ArrayList<>();
        if (root.isArray()) {
            root.forEach(nodes::add);
        } else if (root.isObject()) {
            if (root.has("events") && root.get("events").isArray()) {
                root.get("events").forEach(nodes::add);
            } else {
                nodes.add(root);
            }
        } else {
            throw new JsonProcessingException("Expected JSON array or object") {};
        }

        String[] categories = {"Programming Contest", "Workshop", "Seminar", "Competition", "Club Program", "Cultural Event"};
        String[] venues = {"CSE Auditorium 101", "Software Lab 204", "Central Conference Hall", "Innovation Center", "Robotics Arena", "Campus Amphitheater"};

        List<ApiEvent> list = new ArrayList<>();
        int idx = 0;
        for (JsonNode node : nodes) {
            if (!node.isObject()) continue;
            ApiEvent item = objectMapper.treeToValue(node, ApiEvent.class);
            if (item.getId() <= 0) {
                item.setId(idx + 1);
            }
            if (item.getTitle() == null || item.getTitle().isBlank()) {
                item.setTitle("University Tech Event #" + item.getId());
            } else if (item.getTitle().length() > 55) {
                item.setTitle(item.getTitle().substring(0, 55));
            }
            if (!node.has("category")) {
                item.setCategory(categories[idx % categories.length]);
            }
            if (!node.has("date")) {
                item.setDate(String.format("2026-11-%02d", 10 + (idx % 18)));
            }
            if (!node.has("venue")) {
                item.setVenue(venues[idx % venues.length]);
            }
            if (!node.has("organizer")) {
                item.setOrganizer("EVA PODDER");
            }
            if (!node.has("maxParticipants")) {
                item.setMaxParticipants(60 + (idx * 20));
            }
            list.add(item);
            idx++;
        }
        return list;
    }
}
