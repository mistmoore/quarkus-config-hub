package io.confighub.it;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class ConfigHubRemoteTestResource implements QuarkusTestResourceLifecycleManager {
    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/config/config-hub-spike/pluto", this::handleConfig);
            server.start();

            return Map.of(
                    "quarkus.config-hub.url", "http://127.0.0.1:" + server.getAddress().getPort(),
                    "quarkus.config-hub.environment", "pluto");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start Config Hub test server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void handleConfig(HttpExchange exchange) throws IOException {
        String body = """
                {
                  "application": "config-hub-spike",
                  "environment": "pluto",
                  "version": "0123456789012345678901234567890123456789",
                  "properties": {
                    "payment.url": "https://remote-payment.internal",
                    "payment.timeout": "7s",
                    "payment.retries": "9",
                    "payment.retry.attempts": "8",
                    "payment.retry.delay": "2s"
                  }
                }
                """;

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
