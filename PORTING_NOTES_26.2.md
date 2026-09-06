# Lunamatic 26.2 port

Target: Paper 26.2 build 121 / `paper-api:26.2.build.121-stable`

## Changes

- Migrated `TimeSkip.java` from `TimeSkipEvent.SkipReason` to `ClockTimeSkipEvent.SkipReason` for Paper 26.2.
- Updated the Paper API dependency to `26.2.build.121-stable`.
- Updated the Java toolchain/release target from 21 to 25, required by Paper 26.x.
- Updated MockBukkit to `mockbukkit-v26.2:4.116.1`; its matching Paper API test dependency is `26.2.build.111-stable`.
- Removed the explicit Adventure 4.18 dependency so the plugin uses Paper 26.2's Adventure 5 API instead of bundling an older Adventure API.
- Updated plugin `api-version` to `26.2`.
- Updated Lunamatic's server-version warning to target 26.2.
- Updated local Paper/Folia run-server tasks and CI to use 26.2 and Java 25.
- Removed the obsolete duplicate 1.21 interactive run task.

## Build

Use Java 25 and run:

```bash
./gradlew clean build
```

The generated shaded plugin is under `build/libs/` and uses the `*-all.jar` artifact.
