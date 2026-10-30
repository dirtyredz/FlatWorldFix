# Decisions

## 2026-10-29 - NeoForge 1.21.1 qualified line

Port the v1.0.0 behaviour to a loader-qualified `neoforge-1.21.1` branch with NeoForge 21.1.265,
ModDevGradle 2.0.148 and Java 21. Keep the mod id, persisted marker and all selection semantics.
Use the shared qualified release identity, producing
`flatworldfix-1.21.1-neoforge-1.0.0.jar` without moving the Forge tag.

Minecraft 1.21 changed `WorldDimensions` from a registry-backed record to a map-backed record.
Track marked `WorldDimensions` and baked registries in separate weak identity sets, and inject the
registry constructor to carry intent into the object created for serialization. Equality-based weak
maps were rejected because separately created worlds can have equal dimension maps. NeoForge uses
official names, so metadata owns mixin activation and no SRG refmap is generated or packaged.

The server caller remains `DedicatedServerProperties.WorldDimensionData.create`, the client caller
remains `CreateWorldScreen.onCreate`, and the bake/customization hooks retain their 1.20.1 scope.
The headless flat creation/save smoke proves the server hook and persisted marker on 1.21.1; the
Terralith matrix remains available for a compatible 1.21 jar.

## 2026-10-25 - Shared hosted CI and deliberate releases

Adopt the workspace template as byte-identical standalone files. Pushes verify without publishing;
owner-authorized release dispatch after green main CI verifies, builds, drafts, hashes readback
and publishes immutable assets. Gradle resolves the exact release manifest beside the staged jar.

Neither datagen nor GameTests are configured. Dedicated-server proofs use runServer with
a proof property and remain separate qualification. The shared jar guard enforces the mixin/refmap/license checks declared in `.ci/mod.json`,
replacing the duplicated manual `tools/verify-jar.ps1` check.

## 2026-10-24 — Explicit creation opt-in, persisted with world generation settings

`WorldDimensions.bake` prefers datapack entries. Terralith 2.5.4 supplies
`data/minecraft/dimension/overworld.json`, so flat, single biome, debug, amplified,
and large biomes all lose their selected overworld. Normal worlds need that override.

Use the **resolved preset identity**, not the generator class. On the client, intercept only
the bake in `CreateWorldScreen.onCreate`, where `uiState` still identifies the preset.
On a dedicated server, intercept the resolved `WorldPreset.createWorldDimensions` call in
`DedicatedServerProperties.WorldDimensionData.create`. Missing/invalid server types follow
vanilla's resolution/fallback; normal and unknown client selections do not opt in.
Registered custom presets opt in just like built-in non-default presets.
The selection mixins live in the config's client/server lists; only the marker/merge mixin
is common, so neither side has to resolve the other side's creation classes.

There is an essential reload complication: `LevelStorageSource` also calls `bake` with the
saved dimensions and current datapacks. A creation-only fix would revert on next load.
Add the optional boolean `flatworldfix:keep_overworld` to the WorldDimensions map codec
(stored in `Data.WorldGenSettings`). Its default is false, so old saves remain untouched.
Only explicitly selected new worlds receive it. Keep the marker across flat customization,
the server's pre-bake codec round trip, the baked registry, and serialization from RegistryAccess.

In-memory opt-in belongs to the dimensions registry. A synchronized weak-key set prevents
retaining closed worlds; Minecraft's MappedRegistry uses object identity. A new WorldDimensions
wrapper around that same registry (the saving path) sees the same marker. There is no global
current-world flag or thread-local state that could leak into another creation attempt.

For a marked bake, pass a frozen copy of the datapack registry excluding **only** the overworld
entry. Vanilla then falls back to the selected overworld while doing its own ordering, stability,
special-world classification, and merge. Keep all other entries and their lifecycles; never
mutate the loaded input registry. Bake's result registry retains the marker for future saves.
Unmarked calls use the original argument and ordinary vanilla behavior.

Rejected: blanket flat-generator detection (changes old saves and misses other presets),
changing Terralith resources (changes normal worlds), replacing the entire merge (duplicates
vanilla internals), a creation-only hook (reverts on reload), and a world-local generated datapack
(adds filesystem/pack-order management and freezes a snapshot of generator definitions).

Caller audit of cached Forge 47.4.10 bytecode: CreateWorldScreen, WorldOpenFlows, Main,
GameTestServer, and LevelStorageSource invoke bake. Only the two explicit selection paths opt in;
other paths preserve a decoded marker or remain vanilla. No terrain-generation or chunk hooks.
PresetEditor and DedicatedServerProperties.WorldDimensionData are the callers of
replaceOverworldGenerator; its return hook carries intent across their flat customization.
Removing the mod restores vanilla precedence; it does not remove existing chunks or repair saves.
