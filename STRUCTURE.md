# Structure

## Layout

- `.github/workflows/`, `.github/actions/setup-mod-build/` - synced hosted checks and deliberate release dispatch.
- `.ci/mod.json` - this mod's identity, datagen/GameTest flags, pack side and declarative jar checks; `.ci/scripts/` is synced release tooling.
- `gradle/{verify-release-jar,test-world}.gradle` - applied shared jar guard and forbidden test-world resource definitions.
- `gradle/gametest-world.gradle` and `tools/test-world/datapack/` - synced but unused here; dedicated-server proofs are not CI GameTests.
- `gradle/wrapper/`, `gradlew*`, `.gitattributes`, `.gitignore` - synced workspace files; edit the root template and sync.
- `docs/releases/` - versioned release notes; add notes only for a version being prepared for release.

- `src/main/java/io/github/dirtyredz/flatworldfix/` — mod entry point.
- `src/main/java/io/github/dirtyredz/flatworldfix/world/` — selection and persistence policy.
- `src/main/java/io/github/dirtyredz/flatworldfix/mixin/` — narrowly scoped Minecraft hooks.
- `src/main/resources/` — Forge metadata, mixin config, pack metadata.
- `src/gameTest/` — dedicated-server proof and real registry/codec checks; never packaged.
- `src/test/` — pure unit tests.
- `tools/` — isolated proof launcher and artifact/saved-world verification.
- `docs/` — decisions, gotchas, verification recipe and results.
- `gradle/`, `gradlew*`, `.gitattributes`, `.gitignore` — workspace canonical shared files.
- `build/`, `run/` — ignored build output and isolated proof saves/logs.

The mod is a standalone repository. No runtime dependency on the workspace or other Redz mods.
