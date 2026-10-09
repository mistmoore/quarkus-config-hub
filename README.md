# Quarkus Config Hub

Experimental Quarkus extension and configuration platform focused on type-safe, versioned configuration management.

## Current status

**Milestone 2:** JGit-backed Config Hub Server. Milestone 1 (schema generation during augmentation) is complete.

Target output:

```text
META-INF/config-hub-schema.json
```

The first spike models:

- application `@ConfigMapping` mappings;
- `REQUIRED`, `OPTIONAL`, and `DEFAULTED` presence semantics;
- `@WithDefault`;
- nested mapping groups;
- default SmallRye kebab-case property naming;
- `@WithName`;
- basic Bean Validation constraints (`@Min`, `@Max`, `@NotNull`, `@NotBlank`, `@NotEmpty`);
- application name and version.

It deliberately does **not** implement remote configuration loading yet.

## Modules

```text
quarkus-config-hub
├── runtime            Quarkus runtime artifact (minimal in milestone 1)
├── deployment         augmentation/build-time schema generator
└── integration-tests  sample Quarkus application
```

## Why a Quarkus extension?

The application keeps using standard SmallRye Config APIs. Config Hub integrates below application code instead of introducing calls such as `configHub.get(...)`.

## Build

The project targets Java 21 and Quarkus 3.40 LTS.

```bash
mvn verify
```

The integration-test application should contain `META-INF/config-hub-schema.json` in its Quarkus build output.

## Roadmap

1. **Schema generator spike** — complete.
2. **Config Hub Server backed by JGit** — implemented in Milestone 2.
3. Runtime ConfigSource that loads a configuration snapshot at startup.
4. Optional local cache fallback when Config Hub is unavailable.
5. Schema/config validation.
6. CI publication of schemas.
7. CLI: `schema publish`, `validate`, `diff`.
8. Gradle compatibility verification.
9. Native Image integration tests.

See [`docs-refinement.md`](docs-refinement.md) for the working architecture decisions.


## Config Hub Server

The server exposes:

```text
GET /api/config/{application}/{environment}
```

Configuration is resolved from Git with this precedence:

```text
global/application.properties
        ↓
global/{environment}.properties
        ↓
{application}/application.properties
        ↓
{application}/{environment}.properties
```

Example server configuration:

```properties
config-hub.git.repository-uri=https://github.com/example/config-repository.git
config-hub.git.worktree=.config-hub/repository
config-hub.git.branch=main
```

The response includes the resolved properties and the Git commit SHA used as the configuration version.
