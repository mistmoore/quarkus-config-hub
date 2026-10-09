package io.confighub.deployment;

import java.util.List;
import java.util.Map;

record SchemaNode(
        String name,
        String kind,
        String javaType,
        Presence presence,
        String defaultValue,
        Map<String, Object> constraints,
        List<SchemaNode> children) {

    static SchemaNode property(String name, String javaType, Presence presence, String defaultValue, Map<String, Object> constraints) {
        return new SchemaNode(name, "PROPERTY", javaType, presence, defaultValue, constraints, List.of());
    }

    static SchemaNode group(String name, Presence presence, List<SchemaNode> children) {
        return new SchemaNode(name, "GROUP", null, presence, null, Map.of(), children);
    }
}
