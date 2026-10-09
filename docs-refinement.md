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
2. **COMPLETE:** Minimal Config Hub Server backed by JGit with `GET /api/config/{application}/{environment}`.
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


## Milestone 2 — Config Hub Server

Implemented decisions:

- Quarkus server module.
- Git-backed source of truth using JGit.
- Repository URI, worktree and branch are configurable.
- The server clones the repository when no local working copy exists.
- Before resolving configuration, the server fetches the remote and hard-resets the configured branch to `origin/{branch}`.
- Configuration precedence:
  1. `global/application.properties`
  2. `global/{environment}.properties`
  3. `{application}/application.properties`
  4. `{application}/{environment}.properties`
- The Git HEAD commit SHA is returned as the configuration version.
- Application and environment names are semantically free but restricted to path-safe identifiers.
- Repository synchronization is guarded by a process-local lock.
- The adapter is behind the `ConfigurationRepository` domain port.

Still pending beyond this milestone:

- authentication for private Git repositories;
- improved Git failure handling / stale local checkout strategy;
- observability and health information;
- schema-aware validation;
- runtime ConfigSource client.
