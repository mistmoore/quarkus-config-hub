package io.confighub.server.domain;

public class ConfigurationRepositoryException extends RuntimeException {
    public ConfigurationRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
