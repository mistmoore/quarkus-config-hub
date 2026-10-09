package io.confighub.server.application;

import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import io.confighub.server.domain.ConfigurationRepository;
import io.confighub.server.domain.ResolvedConfiguration;

@ApplicationScoped
public class ConfigurationService {
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9._-]+");

    private final ConfigurationRepository repository;

    public ConfigurationService(ConfigurationRepository repository) {
        this.repository = repository;
    }

    public ResolvedConfiguration resolve(String application, String environment) {
        validateIdentifier("application", application);
        validateIdentifier("environment", environment);
        return repository.resolve(application, environment);
    }

    private static void validateIdentifier(String field, String value) {
        if (value == null || !SAFE_IDENTIFIER.matcher(value).matches() || value.equals(".") || value.equals("..")) {
            throw new IllegalArgumentException("Invalid " + field + " identifier: " + value);
        }
    }
}
