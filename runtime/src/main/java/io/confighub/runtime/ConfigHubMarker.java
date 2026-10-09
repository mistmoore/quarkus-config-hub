package io.confighub.runtime;

/**
 * Runtime marker for the Quarkus Config Hub extension.
 *
 * <p>The first spike only implements build-time schema generation. Runtime
 * ConfigSource integration is intentionally deferred to the next milestone.</p>
 */
public final class ConfigHubMarker {
    private ConfigHubMarker() {
    }
}
