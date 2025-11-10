# Repository Guidelines

## Project Structure & Module Organization
SimpleFIN Kotlin is a single-module Gradle build defined in `settings.gradle.kts`. Runtime sources live in `lib/src/main/kotlin/com/lukelast/simplefin/` (e.g., `SimplefinClient.kt`, `DefaultHttpClient.kt`, data models). Tests and usage samples sit in `lib/src/test/kotlin`. Build logic stays in `lib/build.gradle.kts`, dependency versions in `gradle/libs.versions.toml`, and the Gradle wrapper plus docs sit at the root.

## Build, Test, and Development Commands
- `./gradlew clean assemble` — rebuilds the library and surfaces stale API issues.
- `./gradlew test` — runs the full JUnit Jupiter suite (unit + integration).
- `./gradlew :lib:publishToMavenLocal` — publishes `com.github.lukelast:simplefin-kotlin` for downstream projects.
- `./gradlew -t test` — keeps tests running on every file save during iterative work.

## Coding Style & Naming Conventions
Code targets Kotlin 2.2.21 on the JVM. Use 4-space indentation, Kotlin brace placement, and favor expression bodies when they improve clarity. Classes stay in `PascalCase`, functions/properties in `camelCase`, constants in `ALL_CAPS`, and packages remain under `com.lukelast.simplefin`. Prefer Kotlin data classes for payloads, nullable types instead of sentinel values, and Ktor client extension points for HTTP customization. Keep serialized property names aligned with the SimpleFIN API schema so upgrades remain non-breaking.

## Testing Guidelines
JUnit Jupiter 6 backs both unit tests and Ktor MockClient-based integration tests. Mirror the existing naming (`AccessTokenUrlTest`, `SimplefinClientIntegrationTest`) by suffixing files with `Test` or `IntegrationTest`. Place fast unit coverage next to the code under test, isolate network-facing logic behind mocks unless you intentionally hit the real service, and refresh fixtures whenever request or response models change. Always run `./gradlew test` before raising a PR.

## Commit & Pull Request Guidelines
Recent history favors short, imperative summaries (`format`, `more unit tests`). Follow that style, expanding in the body only when context is needed, and reference issues where relevant. Pull requests should describe the change, list verification steps (commands or reasoning), and call out API or dependency impacts that consumers must know about. Keep diffs focused and link related tickets.

## Security & Configuration Tips
Never commit live SimpleFIN setup tokens or access URLs. Inject secrets through environment variables or local Gradle properties (`~/.gradle/gradle.properties`) and scrub example values (see `ExampleUsage.kt`). Confirm `.gitignore` covers build outputs before adding new tooling.
