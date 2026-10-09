package io.confighub.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConfigSchemaGeneratorTest {
    @Test
    void convertsCamelCaseToKebabCase() {
        assertEquals("read-timeout", ConfigSchemaGenerator.kebabCase("readTimeout"));
        assertEquals("url", ConfigSchemaGenerator.kebabCase("url"));
    }
}
