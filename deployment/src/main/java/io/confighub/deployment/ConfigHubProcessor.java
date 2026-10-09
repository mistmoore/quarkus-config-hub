package io.confighub.deployment;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.builditem.ApplicationInfoBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ConfigMappingBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;

final class ConfigHubProcessor {
    static final String SCHEMA_RESOURCE = "META-INF/config-hub-schema.json";

    private final ConfigSchemaGenerator generator = new ConfigSchemaGenerator();
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @BuildStep
    void generateSchema(List<ConfigMappingBuildItem> mappings,
            CombinedIndexBuildItem combinedIndex,
            ApplicationInfoBuildItem applicationInfo,
            BuildProducer<GeneratedResourceBuildItem> generatedResources) {
        ApplicationSchema schema = generator.generate(applicationInfo.getName(), applicationInfo.getVersion(), mappings, combinedIndex.getIndex());
        generatedResources.produce(new GeneratedResourceBuildItem(SCHEMA_RESOURCE, serialize(schema).getBytes(StandardCharsets.UTF_8)));
    }

    private String serialize(ApplicationSchema schema) {
        try {
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize Config Hub schema", e);
        }
    }
}
