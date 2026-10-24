# Verification

Fast gate: `./gradlew --offline test build`.

Named dedicated-server proof (one server at a time, BelowNormal, local dependencies only):

`./tools/run-proof.ps1 -Name control-flat -Control` performs the process check, waits up to
45 minutes for the machine slot, runs the following command at BelowNormal priority in the
foreground, and checks the test receipt. Other cases use `-Name fixed-flat` or
`-Name fixed-default -WorldType default`. It does not delete worlds or install into instances.

```powershell
./gradlew --offline runServer -Pproof=control-flat -Pcontrol "-PterralithJar=C:/Users/dirty/MultiMC/instances/Roundwood Test/.minecraft/mods/Terralith_1.20.x_v2.5.4.jar"
```

This uses an isolated run/control-flat directory, with level-type=flat and a test-only
ServerStartedEvent subscriber that requests normal shutdown and saving. Read level.dat after exit.
Use distinct proof names for fresh cases; reuse a name only to test reopening its saved world.
The control omits mixin activation. Terralith is staged from the local instance without modifying it.
No test classes or Terralith dependencies enter the release jar.

Use `-Pproof=fixed-flat` without `-Pcontrol` for the fix. Use
`-Pproof=fixed-default -PworldType=default` for the normal-world compatibility case.
Repeat fixed-flat to test reopening; repeat control-flat without `-Pcontrol` to test an existing save.
For each run require `run/<name>/proof-result.txt` to say PASS, normal server shutdown in the log,
and a successful external NBT assertion:

```powershell
node tools/verify-world.mjs run/fixed-flat/world/level.dat flat
node tools/verify-world.mjs run/control-flat/world/level.dat noise build/inspection/terralith-overworld.json
node tools/verify-world.mjs run/fixed-default/world/level.dat noise build/inspection/terralith-overworld.json
./tools/verify-jar.ps1
node tools/compare-worldgen.mjs build/inspection/control-flat-original.dat run/control-flat/world/level.dat
```

Extract `data/minecraft/dimension/overworld.json` read-only from the supplied Terralith jar
to `build/inspection/terralith-overworld.json`. The noise assertion checks the generator,
settings key, and all 1,705 biome IDs in order, not merely the presence of noise terrain.
The fixed server additionally runs the real-registry matrix in MergeProof before stopping.
The test run directory must be fresh for initial creation (no automatic deletion).
Copy the control's original level.dat and log into build/inspection before reopening it with
the fix. Likewise retain the first fixed-flat level.dat and compare after reopening with
`-WorldType default`; existing worlds must ignore a subsequently changed server world type.

Terralith 2.5.4 input SHA-256:
`8F65F309D8F2723754BF4B60C7B5763D3AB6ED04B01C172109BA6564E981B95F`.
Its metadata requires no TerraBlender; only Terralith is staged.

## Observed results, 2026-10-24

Fresh control: `minecraft:noise`, no marker, all 1,705 ordered Terralith biome IDs matched.
Fresh fixed flat: `minecraft:flat`, 1 bedrock + 2 dirt + 1 grass, marker true.
Fresh fixed default: `minecraft:noise`, no marker, all 1,705 ordered Terralith biome IDs matched.
Reopening fixed-flat with `-WorldType default` stayed flat, with identical WorldGenSettings.
Reopening the unmarked control with the fix installed stayed Terralith noise, also with identical
WorldGenSettings. Both comparisons used tools/compare-worldgen.mjs against the retained originals.
The fixed runs passed all eight preset/fallback cases in MergeProof, including customized flat
layers, serialization, missing-marker legacy data, absent overrides, and non-overworld dimensions.

The negative control failed as required: `AssertionError [ERR_ASSERTION]: saved overworld generator`,
with actual `'minecraft:noise'` and expected `'minecraft:flat'`. Originals and logs are retained
under build/inspection; servers saved and stopped normally.

Two setup failures were corrected before the passing runs:
`Task with name 'runServer' not found in root project 'FlatWorldFix'.` and
`No valid input files for searge could be read, processing may not be sucessful.`
See [GOTCHAS.md](GOTCHAS.md) for late task registration and TSRG2-to-SRG conversion.
The first control also fetched Forge version metadata despite Gradle offline mode; later runs
disable that check, and the proof launcher now blocks runtime HTTP via a disconnected local proxy.
No dependency download or installation was performed. Client UI checks are not run (headless only).
