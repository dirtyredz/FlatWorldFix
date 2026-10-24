# Structure

## Layout

- `src/main/java/io/github/dirtyredz/flatworldfix/` — mod entry point.
- `src/main/java/io/github/dirtyredz/flatworldfix/world/` — selection and persistence policy.
- `src/main/java/io/github/dirtyredz/flatworldfix/mixin/` — narrowly scoped Minecraft hooks.
- `src/main/resources/` — Forge metadata, mixin config, pack metadata.
- `src/gameTest/` — dedicated-server proof and real registry/codec checks; never packaged.
- `src/test/` — pure unit tests.
- `tools/` — read-only saved-world verification.
- `docs/` — decisions, gotchas, verification recipe and results.
- `gradle/`, `gradlew*`, `.gitattributes`, `.gitignore` — workspace canonical shared files.
- `build/`, `run/` — ignored build output and isolated proof saves/logs.

The mod is a standalone repository. No runtime dependency on the workspace or other Redz mods.
