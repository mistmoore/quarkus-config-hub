package io.confighub.server.domain;

import java.util.Map;

public record ResolvedConfiguration(
        String application,
        String environment,
        String version,
        Map<String, String> properties) {
}
