# Quarkus Config Hub — Refinement

## Closed decisions

- Client integration is a Quarkus extension with `runtime` and `deployment` modules.
- Config Hub Server will be a separate Quarkus application.
- Git is the source of truth; server-side Git access uses JGit.
- Environments are free-form identifiers and are independent from `quarkus.profile` (for example `pluto`).
- No dynamic refresh: each instance loads one configuration snapshot at startup and keeps it for its lifetime.
- If Config Hub is unavailable, the application logs the failure and may use a local cache only when `quarkus.config-hub.cache.enabled=true`; otherwise startup fails fast.
- The schema contains both application configuration and Quarkus/extension configuration, differentiated by origin metadata.
- SmallRye presence semantics are preserved as `REQUIRED`, `OPTIONAL`, and `DEFAULTED`.
- Bean Validation constraints are captured when possible.
- CI/CD publishes schemas; running applications do not publish them.
- A CLI is planned after the MVP: `schema publish`, `validate`, `diff`.
- Maven and Gradle must both be supported; the core design must not depend on Maven-specific behavior.
- JVM and Native Image are target runtimes; Native does not block the first spike.
- Secrets are out of scope for the first MVP and remain delegated to external secret managers.

## MVP milestones

1. Generate `META-INF/config-hub-schema.json` during Quarkus augmentation from application `@ConfigMapping` metadata.
2. Minimal Config Hub Server backed by JGit with `GET /api/config/{application}/{environment}`.
3. Runtime `ConfigSource` that loads a resolved snapshot at startup.
4. Optional local cache fallback.
5. Schema/config validation.
6. CI schema publication.
7. CLI.
8. Gradle verification.
9. Native Image integration tests.

## Milestone 1 spike

The first implementation uses Quarkus `ConfigMappingBuildItem` plus Jandex during augmentation. It currently targets:

- `@ConfigMapping` discovery;
- default kebab-case naming;
- `@WithName`;
- `Optional<T>`;
- `@WithDefault`;
- nested mapping interfaces;
- basic Bean Validation constraints;
- generation of a JSON resource through `GeneratedResourceBuildItem`.

Later passes will cover more SmallRye mapping semantics such as collections, maps, converters and additional annotations.
