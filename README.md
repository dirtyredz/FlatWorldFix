# Flat World Fix

A small MIT-licensed Forge 1.20.1 mod. Explicitly selected non-default world types
keep their overworld when a datapack such as Terralith supplies its own overworld dimension.
Default worlds continue using datapack terrain. No configuration or additional mods required.

Install on the dedicated server, or on the client for singleplayer world creation.
Flat customization, single biome, debug, amplified, large biomes, and registered custom presets
are covered. The Nether, End, and extra datapack dimensions retain vanilla merge precedence.

Existing saves are not opted in. Newly protected worlds retain a small marker in level.dat
so reopening them keeps the selected overworld. Keep this mod installed for those worlds;
removing it restores vanilla's datapack precedence. Datapacks can still change referenced
biomes and noise settings; this mod controls dimension selection, not all world generation.

Build: `./gradlew --offline test build` with cached Forge 47.4.10, Gradle 8.13, Java 17.
Release output: `build/libs/`; the version comes from `gradle.properties`.
See [verification](docs/VERIFY.md), [design](docs/DECISIONS.md), and [layout](STRUCTURE.md).

## Release

The version lives only in `gradle.properties`. Prepare non-empty `docs/releases/<version>.md`
for a release, then obtain the owner's go-ahead to push and land on main. Push CI runs
`./gradlew test build verifyReleaseJar`; it publishes nothing.
After green main CI and owner authorization, the agent dispatches
`gh workflow run release.yml --ref main`. The workflow verifies again, builds
the final jar, creates its version tag, uploads a draft, reads every asset back and compares
SHA-256 before publishing the jar, its `.sha256` and `flatworldfix.pw.toml`.
The pack session consumes the published `.pw.toml`; the owner never publishes by hand.
Versions starting with `0.` or containing `-` are prereleases. Existing releases/tags are
refused, published bytes are never replaced, and stable latest never moves backward.
A failed draft requires deliberate recovery before retrying.

Local fast gate: `./gradlew --offline test build verifyReleaseJar` with cached dependencies.
Shared files are checked from the workspace with `tools/sync-mod-files.ps1 -Check -Mod <this-worktree>`.

CI sets `datagen: false` (no generator) and `gametests: false`: the `src/gameTest` source set
contains dedicated-server proofs launched by `runServer -Pproof=...`, not a Forge GameTest suite.
The release job therefore skips Forge GameTests. These proofs stay separate; see
[verification](docs/VERIFY.md). The shared `verifyReleaseJar` guard also enforces the
mixin config, manifest activation, SRG refmap and license checks declared in `.ci/mod.json`.
