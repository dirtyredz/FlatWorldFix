# Architecture

FlatWorldFix is a mixin-only NeoForge 1.21.1 mod with no blocks, items or config.

## The problem it changes

Minecraft builds a world's dimensions in `WorldDimensions.bake`, preferring a datapack's
`dimension/overworld.json` over the overworld from the selected world preset. Terralith ships
one, so Superflat, single biome and similar presets silently become Terralith noise terrain. The
same merge runs again every time a save is opened.

## Flow

1. **Creation** (client `CreateWorldScreen`, or a dedicated server reading `level-type`): when the
   selected preset is not the default one, the mod keeps the selected overworld instead of the
   datapack's, and writes a marker into the new world's settings.
2. **Reopen:** a world carrying the marker keeps its saved overworld through the merge. A world
   without the marker, including every save made before the mod was installed, gets vanilla
   behaviour.
3. The Nether, the End and any other datapack dimensions are never touched.

Minecraft 1.21 stores a map inside `WorldDimensions` instead of retaining its input registry. The
opt-in therefore follows the `WorldDimensions` object by weak identity, follows the baked registry,
and is restored onto the saving path's `new WorldDimensions(registry)` copy. Identity comparison is
deliberate: two different worlds with structurally equal settings must not share intent.

The design choices and rejected alternatives are in [DECISIONS.md](DECISIONS.md); the proof runs
are in [VERIFY.md](VERIFY.md).
