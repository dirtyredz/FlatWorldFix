# Gotchas

- A flat preset alone loses to Terralith's dimension resource in `WorldDimensions.bake`.
  Disabling structures does not change this precedence.
- `bake` also runs on save reload. A blanket generator-type fix would change existing worlds;
  creation must opt in, and the opt-in must survive serialization.
- The selected preset's generator settings and biome references still come from the active
  datapacks. In particular, selecting single biome does not restore vanilla noise settings.
- Unit tests do not supply Forge's transformation environment. Exercise real Minecraft
  registries and serialization in the dedicated-server proof source set.
- Minecraft-targeting mixins require an SRG refmap in the release jar. Dev launches remap that
  refmap back to official names. Inspect both config and refmap after reobfuscation.
  ForgeGradle's createMcpToSrg emits output.tsrg (TSRG2); createMixinMappings converts it
  to traditional SRG with ForgeGradle's existing srgutils dependency for Mixin's processor.
- The workspace sync script accepts standalone worktrees: use
  `tools/sync-mod-files.ps1 -Check -Mod <this-worktree>` for every synced CI/world/wrapper file.
- DankRecipeTransfer currently has no CLAUDE.md; its build.gradle and the workspace CLAUDE.md
  supplied the scaffold conventions.
- ForgeGradle registers run tasks late. Use `tasks.configureEach` for the proof dependency;
  eagerly resolving `tasks.named('runServer')` fails during configuration.
- Gradle `--offline` does not disable Forge's own runtime update check. Stage
  `config/fml.toml` with `versionCheck=false` before a proof launch; the
  `forge.disableVersionCheck` system property does not disable it on this Forge version.
  Proof runs also direct Java HTTP/HTTPS through localhost port 9 (no listener), preventing
  authlib's automatic metadata refreshes from accessing the internet. This is test-only.
