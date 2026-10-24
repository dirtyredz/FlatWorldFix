# Working on FlatWorldFix

Forge 1.20.1 / 47.4.10, Java 17. Version lives only in gradle.properties.
Read STRUCTURE.md and docs/DECISIONS.md before changing selection or persistence.

- Fast gate: `./gradlew --offline test build` (the workspace gate with downloads disabled).
- Named headless proof: see docs/VERIFY.md. Never run a visible client.
- Game/server checks run one at a time, BelowNormal; stop every started process.
- Java: src/main/java/io/github/dirtyredz/flatworldfix; tests stay in their own source sets.
- Shared wrapper/git files come byte-for-byte from the workspace tools/mod-template.
- No instance writes, downloads, publishing, or Docket status changes in this worktree.
