# Verification

Fast gate: `./gradlew test build verifyReleaseJar`. The shared profile resolver selects
`neoforge-1.21.1`; the guard stages the exact qualified release jar and `build/ci/release.json`,
checks `META-INF/neoforge.mods.toml`, metadata mixin activation, Java 21 compatibility, the MIT
license and the absence of proof classes. This project has no NeoForge GameTests, so
`.ci/mod.json` deliberately sets `gametests: false`.

Named dedicated-server smoke proof (one server at a time, BelowNormal):

```powershell
./tools/run-proof.ps1 -Name fixed-flat-1211
node tools/verify-world.mjs run/fixed-flat-1211/world/level.dat flat
```

The proof launcher waits up to 45 minutes for the machine's Java slot, creates an isolated run
directory, blocks runtime HTTP through a disconnected local proxy, starts `runServer` in the
foreground, and requires its receipt. Use a fresh name for initial creation; the launcher does not
delete a saved world. The subscriber records the generator and requests normal shutdown/saving.
The external NBT assertion requires the flat generator, exact configured layers, the
`flatworldfix:keep_overworld` marker and the Nether/End entries.

The real-registry Terralith matrix remains available when a NeoForge 1.21.1-compatible local jar is
supplied read-only:

```powershell
./tools/run-proof.ps1 -Name fixed-flat-terralith-1211 -TerralithJar C:/path/to/Terralith-1.21.x.jar
node tools/verify-world.mjs run/fixed-flat-terralith-1211/world/level.dat flat
```

Supplying the jar enables `MergeProof`: flat, single biome, debug, amplified, large biomes, normal,
default and unknown preset/fallback cases; customized flat layers; marker serialization; legacy
data without the marker; absent overrides; input immutability; and preservation of non-overworld
dimensions. Never use the locally installed Terralith 2.5.4 jar for this line: its metadata range is
`[1.20,1.21)`, so it is not a valid 1.21.1 proof input.

## Observed results, 2026-10-29

`./gradlew test build verifyReleaseJar`: **BUILD SUCCESSFUL in 53s**; four unit tests passed.
The guard verified `flatworldfix-1.21.1-neoforge-1.0.0.jar`, profile `neoforge-1.21.1`, six
compiled main classes and no proof/test classes. Direct archive inspection confirmed mod id
`flatworldfix`, NeoForge `[21.1.265,21.2)`, Minecraft `[1.21.1,1.21.2)`, and the metadata-owned
mixin config. SHA-256: `90A6DA8856F1A633ED3D2354B1B1624265DE3684740F37C5D50923E4A853B996`.

`./tools/run-proof.ps1 -Name fixed-flat-1211`: **BUILD SUCCESSFUL in 8m 49s**. NeoForge 21.1.265
loaded FlatWorldFix 1.0.0, the mixin subsystem selected JAVA_21, the server reported
`FlatLevelSource`, saved all three dimensions and stopped normally. `proof-result.txt` was `PASS`.

`node tools/verify-world.mjs run/fixed-flat-1211/world/level.dat flat`: **PASS**; generator
`minecraft:flat`, marker true, and overworld/Nether/End present. This was the one permitted server
boot. The Terralith matrix was not run because no compatible 1.21.1 jar is locally available.

Client UI checks are not run (headless only). The owner verifies singleplayer creation in game.
Workspace `sync-mod-files.ps1 -Check`, `dk check`, and `git diff --check` passed.
