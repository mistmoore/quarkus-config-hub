package io.confighub.runtime;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigHubConfigSource implements ConfigSource {
    static final int ORDINAL = 270;

    private final Map<String, String> properties;
    private final String version;

    ConfigHubConfigSource(Map<String, String> properties, String version) {
        this.properties = Map.copyOf(properties);
        this.version = version;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return "Quarkus Config Hub [" + version + "]";
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }
}
