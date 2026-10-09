package io.confighub.server.domain;

public interface ConfigurationRepository {
    ResolvedConfiguration resolve(String application, String environment);
}
