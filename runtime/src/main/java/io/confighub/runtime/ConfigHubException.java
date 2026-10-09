package io.confighub.runtime;

public class ConfigHubException extends RuntimeException {
    public ConfigHubException(String message) {
        super(message);
    }

    public ConfigHubException(String message, Throwable cause) {
        super(message, cause);
    }
}
