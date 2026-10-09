package io.confighub.deployment;

import java.util.List;

record MappingSchema(
        String prefix,
        String javaType,
        String origin,
        List<SchemaNode> properties) {
}
