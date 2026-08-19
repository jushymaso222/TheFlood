# The Flood — Feature-First Package Refactor

This refactor reorganizes the Java source tree by gameplay feature. No gameplay behavior was intentionally changed.

## Major package moves

- `guide/` — guide models/content, with `guide/client/` and `guide/network/`
- `team/` — existing team server logic, plus `team/client/` and `team/network/`
- `progression/` — Heat/player progression, plus `client/`, `combat/`, `event/`, `network/`, and `scaling/`
- `spawning/` — `SpawnDirector`, mob spawn events, Flood Warden events
- `horde/` — `HordeDirector`
- `hud/` — shared HUD/settings screens and pause-menu settings controls
- `compat/mekanism/` — Mekanism-specific compatibility code
- `event/` — only remaining cross-feature lifecycle/time/server events
- `client/` — only the cross-feature `ClientScreenEvents` router remains

## Validation performed

- Checked every Java file's package declaration matches its filesystem path.
- Checked for stale imports/references to all moved package names.
- Checked internal `com.jushymaso222.theflood.*` imports resolve to a source class.
- Checked moved classes for likely missing imports caused by formerly sharing a package.

A full Gradle compile could not be completed in the refactor environment because the Gradle wrapper attempted to download Gradle 8.8 and outbound network access is unavailable.

## Recommended first action

On your development machine, run:

```bat
gradlew clean compileJava
```

If that succeeds, run your normal:

```bat
gradlew runClient
```

Because this was intentionally a structure-only refactor, any compile issue should be an import/access edge case rather than a behavioral rewrite.
