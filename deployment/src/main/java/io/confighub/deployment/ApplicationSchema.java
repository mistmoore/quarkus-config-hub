package io.confighub.deployment;

import java.util.List;

record ApplicationSchema(
        String schemaFormatVersion,
        String application,
        String applicationVersion,
        List<MappingSchema> mappings) {
}
