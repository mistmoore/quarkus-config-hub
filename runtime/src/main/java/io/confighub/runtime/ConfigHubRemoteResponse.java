package io.confighub.runtime;

import java.util.Map;

record ConfigHubRemoteResponse(
        String application,
        String environment,
        String version,
        Map<String, String> properties) {
}
