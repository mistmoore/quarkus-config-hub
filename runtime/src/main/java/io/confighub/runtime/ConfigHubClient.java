package io.confighub.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

final class ConfigHubClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    ConfigHubClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build(), new ObjectMapper());
    }

    ConfigHubClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    ConfigHubRemoteResponse fetch(String baseUrl, String application, String environment) {
        URI uri = URI.create(stripTrailingSlash(baseUrl)
                + "/api/config/" + application
                + "/" + environment);

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ConfigHubException(
                        "Config Hub returned HTTP " + response.statusCode() + " for " + uri);
            }

            return objectMapper.readValue(response.body(), ConfigHubRemoteResponse.class);
        } catch (IOException e) {
            throw new ConfigHubException("Unable to read configuration from Config Hub: " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConfigHubException("Interrupted while reading configuration from Config Hub: " + uri, e);
        }
    }

    private static String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
