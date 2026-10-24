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
