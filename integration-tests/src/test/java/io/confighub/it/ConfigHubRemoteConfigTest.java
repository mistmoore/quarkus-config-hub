package io.confighub.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Duration;

import jakarta.inject.Inject;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(
        value = ConfigHubRemoteTestResource.class,
        restrictToAnnotatedClass = true)
class ConfigHubRemoteConfigTest {

    @Inject
    PaymentConfig payment;

    @Test
    void remoteConfigOverridesApplicationPropertiesAndFeedsConfigMapping() {
        assertEquals(URI.create("https://remote-payment.internal"), payment.url());
        assertTrue(payment.timeout().isPresent());
        assertEquals(Duration.ofSeconds(7), payment.timeout().orElseThrow());
        assertEquals(9, payment.retries());
        assertEquals(8, payment.retry().attempts());
        assertEquals(Duration.ofSeconds(2), payment.retry().delay());
    }
}
