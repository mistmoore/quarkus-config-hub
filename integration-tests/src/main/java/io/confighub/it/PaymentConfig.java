package io.confighub.it;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@ConfigMapping(prefix = "payment")
public interface PaymentConfig {
    URI url();
    Optional<Duration> timeout();
    @WithDefault("3") int retries();
    Retry retry();

    interface Retry {
        @Min(1) @Max(10) int attempts();
        @WithDefault("1s") Duration delay();
    }
}
