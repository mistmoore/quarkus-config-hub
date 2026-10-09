package io.confighub.runtime;

import java.util.Collections;
import java.util.OptionalInt;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;

@StaticInitSafe
public final class ConfigHubConfigSourceFactory implements ConfigSourceFactory {
    static final String URL = "quarkus.config-hub.url";
    static final String ENVIRONMENT = "quarkus.config-hub.environment";
    static final String APPLICATION = "quarkus.config-hub.application";
    static final String QUARKUS_APPLICATION = "quarkus.application.name";

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9._-]+");

    private final ConfigHubClient client = new ConfigHubClient();

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        String url = value(context, URL);
        if (url == null || url.isBlank()) {
            return Collections.emptyList();
        }

        String environment = required(context, ENVIRONMENT);
        String application = value(context, APPLICATION);
        if (application == null || application.isBlank()) {
            application = required(context, QUARKUS_APPLICATION);
        }

        validateIdentifier("application", application);
        validateIdentifier("environment", environment);

        ConfigHubRemoteResponse response = client.fetch(url, application, environment);
        if (response.properties() == null) {
            throw new ConfigHubException("Config Hub response contains no properties");
        }

        return Collections.singletonList(
                new ConfigHubConfigSource(response.properties(), response.version()));
    }

    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(200);
    }

    private static String required(ConfigSourceContext context, String name) {
        String value = value(context, name);
        if (value == null || value.isBlank()) {
            throw new ConfigHubException("Missing required Config Hub bootstrap property: " + name);
        }
        return value;
    }

    private static String value(ConfigSourceContext context, String name) {
        ConfigValue value = context.getValue(name);
        return value == null ? null : value.getValue();
    }

    private static void validateIdentifier(String field, String value) {
        if (!SAFE_IDENTIFIER.matcher(value).matches() || value.equals(".") || value.equals("..")) {
            throw new ConfigHubException("Invalid Config Hub " + field + " identifier: " + value);
        }
    }
}
