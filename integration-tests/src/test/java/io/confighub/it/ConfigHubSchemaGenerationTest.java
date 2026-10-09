package io.confighub.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ConfigHubSchemaGenerationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void generatesExpectedSchemaFromConfigMapping() throws Exception {
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("META-INF/config-hub-schema.json")) {

            assertNotNull(input, "Config Hub schema should be generated during augmentation");

            JsonNode root = MAPPER.readTree(input);

            assertEquals("1", root.path("schemaFormatVersion").asText());
            assertEquals("config-hub-spike", root.path("application").asText());
            assertEquals("0.1.0", root.path("applicationVersion").asText());

            JsonNode mappings = root.path("mappings");
            assertTrue(mappings.isArray());
            assertEquals(1, mappings.size());

            JsonNode payment = mappings.get(0);
            assertEquals("payment", payment.path("prefix").asText());
            assertEquals("io.confighub.it.PaymentConfig", payment.path("javaType").asText());
            assertEquals("application", payment.path("origin").asText());

            JsonNode url = findProperty(payment.path("properties"), "payment.url");
            assertEquals("PROPERTY", url.path("kind").asText());
            assertEquals("REQUIRED", url.path("presence").asText());

            JsonNode timeout = findProperty(payment.path("properties"), "payment.timeout");
            assertEquals("OPTIONAL", timeout.path("presence").asText());

            JsonNode retries = findProperty(payment.path("properties"), "payment.retries");
            assertEquals("DEFAULTED", retries.path("presence").asText());
            assertEquals("3", retries.path("defaultValue").asText());

            JsonNode retry = findProperty(payment.path("properties"), "payment.retry");
            assertEquals("GROUP", retry.path("kind").asText());
            assertEquals("REQUIRED", retry.path("presence").asText());

            JsonNode attempts = findProperty(retry.path("children"), "payment.retry.attempts");
            assertEquals("REQUIRED", attempts.path("presence").asText());
            assertEquals(1, attempts.path("constraints").path("min").asLong());
            assertEquals(10, attempts.path("constraints").path("max").asLong());

            JsonNode delay = findProperty(retry.path("children"), "payment.retry.delay");
            assertEquals("DEFAULTED", delay.path("presence").asText());
            assertEquals("1s", delay.path("defaultValue").asText());
        }
    }

    private static JsonNode findProperty(JsonNode nodes, String name) {
        for (JsonNode node : nodes) {
            if (name.equals(node.path("name").asText())) {
                return node;
            }
        }
        throw new AssertionError("Property not found in generated schema: " + name);
    }
}
