# Gotchas

- A flat preset alone loses to Terralith's dimension resource in `WorldDimensions.bake`.
  Disabling structures does not change this precedence.
- `bake` also runs on save reload. A blanket generator-type fix would change existing worlds;
  creation must opt in, and the opt-in must survive serialization.
- The selected preset's generator settings and biome references still come from the active
  datapacks. In particular, selecting single biome does not restore vanilla noise settings.
- Unit tests do not supply the loader's transformation environment. Exercise real Minecraft
  registries and serialization in the dedicated-server proof source set.
- Forge 1.20.1 only: Minecraft-targeting mixins require an SRG refmap in the release jar. Dev launches remap that
  refmap back to official names. Inspect both config and refmap after reobfuscation.
  ForgeGradle's createMcpToSrg emits output.tsrg (TSRG2); createMixinMappings converts it
  to traditional SRG with ForgeGradle's existing srgutils dependency for Mixin's processor.
- The preceding refmap rule applies to the Forge 1.20.1 line only. NeoForge 1.21.1 runs official
  names: mixins are declared in `neoforge.mods.toml`, and the release jar intentionally has no refmap
  or `MixinConfigs` manifest attribute.
- In 1.21, `WorldDimensions.dimensions()` is a map rather than a registry. Do not use an ordinary
  weak set for its marker: record/map equality can make two distinct worlds compare equal. Use the
  weak identity bridge through the baked registry and the saving constructor.
- The workspace sync script accepts standalone worktrees: use
  `tools/sync-mod-files.ps1 -Check -Mod <this-worktree>` for every synced CI/world/wrapper file.
- DankRecipeTransfer and Roundwood's finished NeoForge 1.21.1 ports supply the scaffold conventions.
- Forge 1.20.1 only: ForgeGradle registers run tasks late. Use `tasks.configureEach` for the proof dependency;
  eagerly resolving `tasks.named('runServer')` fails during configuration.
- Forge 1.20.1 only: Gradle `--offline` does not disable Forge's own runtime update check. Stage
  `config/fml.toml` with `versionCheck=false` before a proof launch; the
  `forge.disableVersionCheck` system property does not disable it on this Forge version.
  Proof runs also direct Java HTTP/HTTPS through localhost port 9 (no listener), preventing
  authlib's automatic metadata refreshes from accessing the internet. This is test-only.
