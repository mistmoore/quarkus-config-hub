package io.confighub.deployment;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

final class ConfigSchemaGenerator {
    private static final DotName CONFIG_MAPPING = DotName.createSimple("io.smallrye.config.ConfigMapping");
    private static final DotName OPTIONAL = DotName.createSimple(Optional.class.getName());
    private static final DotName WITH_DEFAULT = DotName.createSimple("io.smallrye.config.WithDefault");
    private static final DotName WITH_NAME = DotName.createSimple("io.smallrye.config.WithName");
    private static final DotName MIN = DotName.createSimple("jakarta.validation.constraints.Min");
    private static final DotName MAX = DotName.createSimple("jakarta.validation.constraints.Max");
    private static final DotName NOT_BLANK = DotName.createSimple("jakarta.validation.constraints.NotBlank");
    private static final DotName NOT_EMPTY = DotName.createSimple("jakarta.validation.constraints.NotEmpty");
    private static final DotName NOT_NULL = DotName.createSimple("jakarta.validation.constraints.NotNull");

    ApplicationSchema generate(
            String application,
            String applicationVersion,
            IndexView applicationIndex,
            IndexView combinedIndex) {

        List<MappingSchema> mappings = applicationIndex.getAnnotations(CONFIG_MAPPING).stream()
                .filter(annotation -> annotation.target().kind() == AnnotationTarget.Kind.CLASS)
                .map(annotation -> generateMapping(annotation, combinedIndex))
                .toList();

        return new ApplicationSchema("1", application, applicationVersion, mappings);
    }

    private MappingSchema generateMapping(AnnotationInstance configMapping, IndexView combinedIndex) {
        ClassInfo configClass = configMapping.target().asClass();
        String prefix = configMapping.value("prefix") == null
                ? ""
                : configMapping.value("prefix").asString();

        List<SchemaNode> nodes = walkGroup(
                configClass,
                normalizePrefix(prefix),
                configClass.name(),
                combinedIndex);

        return new MappingSchema(prefix, configClass.name().toString(), "application", nodes);
    }

    private List<SchemaNode> walkGroup(ClassInfo group, String prefix, DotName rootMappingName, IndexView index) {
        List<SchemaNode> result = new ArrayList<>();

        for (MethodInfo method : group.methods()) {
            if (!isConfigPropertyMethod(method)) {
                continue;
            }

            Type declaredType = method.returnType();
            boolean optional = isOptional(declaredType);
            Type valueType = unwrapOptional(declaredType);
            String segment = propertySegment(method);
            String propertyName = prefix.isBlank() ? segment : prefix + "." + segment;
            AnnotationInstance withDefault = method.annotation(WITH_DEFAULT);
            Presence presence = withDefault != null
                    ? Presence.DEFAULTED
                    : optional ? Presence.OPTIONAL : Presence.REQUIRED;

            ClassInfo nestedGroup = nestedGroup(valueType, rootMappingName, index);
            if (nestedGroup != null) {
                result.add(SchemaNode.group(
                        propertyName,
                        presence,
                        walkGroup(nestedGroup, propertyName, rootMappingName, index)));
                continue;
            }

            String defaultValue = withDefault == null ? null : withDefault.value().asString();
            result.add(SchemaNode.property(
                    propertyName,
                    valueType.toString(),
                    presence,
                    defaultValue,
                    constraints(method)));
        }

        return result;
    }

    private boolean isConfigPropertyMethod(MethodInfo method) {
        return method.parametersCount() == 0
                && !Modifier.isStatic(method.flags())
                && !method.name().equals("toString")
                && !method.name().equals("hashCode");
    }

    private boolean isOptional(Type type) {
        return type.kind() == Type.Kind.PARAMETERIZED_TYPE && type.name().equals(OPTIONAL);
    }

    private Type unwrapOptional(Type type) {
        if (!isOptional(type)) {
            return type;
        }
        ParameterizedType optional = type.asParameterizedType();
        return optional.arguments().getFirst();
    }

    private ClassInfo nestedGroup(Type type, DotName rootMappingName, IndexView index) {
        if (type.kind() != Type.Kind.CLASS && type.kind() != Type.Kind.PARAMETERIZED_TYPE) {
            return null;
        }

        DotName typeName = type.name();
        ClassInfo candidate = index.getClassByName(typeName);
        if (candidate == null || !candidate.isInterface()) {
            return null;
        }

        return typeName.toString().startsWith(rootMappingName + "$") ? candidate : null;
    }

    private String propertySegment(MethodInfo method) {
        AnnotationInstance withName = method.annotation(WITH_NAME);
        if (withName != null) {
            return withName.value().asString();
        }
        return kebabCase(method.name());
    }

    private Map<String, Object> constraints(MethodInfo method) {
        Map<String, Object> result = new LinkedHashMap<>();
        putLongConstraint(method, MIN, "min", result);
        putLongConstraint(method, MAX, "max", result);

        if (method.hasAnnotation(NOT_BLANK)) {
            result.put("notBlank", true);
        }
        if (method.hasAnnotation(NOT_EMPTY)) {
            result.put("notEmpty", true);
        }
        if (method.hasAnnotation(NOT_NULL)) {
            result.put("notNull", true);
        }

        return result;
    }

    private void putLongConstraint(
            MethodInfo method,
            DotName annotationName,
            String key,
            Map<String, Object> target) {

        AnnotationInstance annotation = method.annotation(annotationName);
        if (annotation != null) {
            target.put(key, annotation.value().asLong());
        }
    }

    private String normalizePrefix(String prefix) {
        return prefix == null ? "" : prefix.trim();
    }

    static String kebabCase(String value) {
        StringBuilder result = new StringBuilder(value.length() + 8);

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isUpperCase(current)) {
                if (i > 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }

        return result.toString();
    }
}
