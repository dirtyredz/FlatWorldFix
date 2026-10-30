# Flat World Fix

A small MIT-licensed NeoForge 1.21.1 mod. Explicitly selected non-default world types
keep their overworld when a datapack such as Terralith supplies its own overworld dimension.
Default worlds continue using datapack terrain. No configuration or additional mods required.

Install on the dedicated server, or on the client for singleplayer world creation.
Flat customization, single biome, debug, amplified, large biomes, and registered custom presets
are covered. The Nether, End, and extra datapack dimensions retain vanilla merge precedence.

Existing saves are not opted in. Newly protected worlds retain a small marker in level.dat
so reopening them keeps the selected overworld. Keep this mod installed for those worlds;
removing it restores vanilla's datapack precedence. Datapacks can still change referenced
biomes and noise settings; this mod controls dimension selection, not all world generation.

Build: `./gradlew test build verifyReleaseJar` with NeoForge 21.1.265, ModDevGradle 2.0.148,
Gradle 8.13 and Java 21.
Release output: `build/libs/`; the version comes from `gradle.properties`.
See [verification](docs/VERIFY.md), [design](docs/DECISIONS.md), and [layout](STRUCTURE.md).

## Release

The version lives only in `gradle.properties`. Prepare non-empty `docs/releases/<version>.md`
for a release, then obtain the owner's go-ahead to push the `neoforge-1.21.1` line. Push CI runs
`./gradlew test build verifyReleaseJar`; it publishes nothing.
After green branch CI and owner authorization, the orchestrator dispatches
`gh workflow run release.yml --ref neoforge-1.21.1`. The workflow verifies again, builds
the final jar, creates its version tag, uploads a draft, reads every asset back and compares
SHA-256 before publishing the jar, its `.sha256` and `flatworldfix.pw.toml`.
The pack session consumes the published `.pw.toml`; the owner never publishes by hand.
Versions starting with `0.` or containing `-` are prereleases. Existing releases/tags are
refused, published bytes are never replaced, and stable latest never moves backward.
A failed draft requires deliberate recovery before retrying.

Local fast gate: `./gradlew test build verifyReleaseJar`.
Shared files are checked from the workspace with `tools/sync-mod-files.ps1 -Check -Mod <this-worktree>`.

CI sets `datagen: false` (no generator) and `gametests: false`: the `src/gameTest` source set
contains dedicated-server proofs launched by `runServer -Pproof=...`, not a NeoForge GameTest suite.
The release job therefore skips GameTests. These proofs stay separate; see
[verification](docs/VERIFY.md). The shared `verifyReleaseJar` guard also enforces the
NeoForge metadata/mixin activation, Java 21 config, license and absence of test classes declared in
`.ci/mod.json`.
