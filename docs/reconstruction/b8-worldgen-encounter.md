# B8 worldgen arena and one-time Koros dialogue

## Goal

1. Generate a B8 arena natively as a rare Overworld structure while the
   world generates, using the same `arenas/b8/tile_*` templates as the manual
   `/finalparadox arena deploy b8` command.
2. Make the B8 pre-fight Koros dialogue trigger only once per deployment.
   After the encounter has begun, the manager must not respawn Koros and the
   intro dialogue must not replay after a server reload.

## Structure generation

- `ModWorldgen` registers `finalparadox:b8_arena` as both a
  `StructureType<B8ArenaStructure>` and a `StructurePieceType` via
  `B8ArenaPiece`.
- `B8ArenaStructure` copies the MarawThar arena placement pattern: a 9-point
  surface check across the arena footprint (radius 24), max relief 8, max
  water/surface depth 3, and a floor anchor at the center surface height.
- The two B8 tiles (48x14x48 at tile offset 0,0,0 and 48x14x1 at 0,0,48) are
  placed at `floorAnchor + sourceMinOffset(-23,-9,-24)`.
- Datapack resources:
  - `worldgen/structure/b8_arena.json`
  - `worldgen/structure_set/b8_arena.json` (random_spread, spacing 160,
    separation 80)
  - `tags/worldgen/biome/has_structure/b8_arena.json`

`worldgen/structure/b8.json` registers `finalparadox:b8` as an alias entry
using the same `finalparadox:b8_arena` structure type and biome tag. Both
`/locate structure finalparadox:b8` and
`/locate structure finalparadox:b8_arena` resolve to a B8 placement.

## Chunk-load adoption

`ArenaDeploymentEvents.onChunkLoad` scans `ChunkAccess.getAllStarts()` on the
server. When a valid B8 structure start is found and the deployment record is
still IDLE, it derives the floor anchor from the structure start bounding box:

```text
floorAnchor = (box.minX + 23, box.minY + 9, box.minZ + 24)
```

The reverse formula is valid because every B8 piece uses no rotation/mirror
and `StructureTemplate.getBoundingBox` spans the full template size, so the
start bounding box min corner is exactly `floorAnchor + (-23, -9, -24)`.

`ArenaDeploymentData.adoptWorldgen` records the arena as immediately READY
with `nextTile = tileCount`, without running tile placement on top of the
already-generated structure. A manual deployment or reset always wins over
worldgen adoption: adoption only happens when the record is IDLE.

## One-time dialogue

- `ArenaDeploymentData` gains a persisted `b8Triggered` boolean
  (`B8Triggered` NBT key). `begin(...)` resets it to false so
  `deploy b8` / `reset b8` can be used to retest the dialogue.
- `B8EncounterManager.begin` sets `b8Triggered = true` when the encounter
  starts, covering both the Koros menu path and `/finalparadox arena start b8`.
- `ArenaDeploymentManager.tick` only auto-spawns B8 Koros when
  `b8Triggered` is false, so after victory Koros is not recreated and the
  dialogue cannot repeat. After defeat, `respawn` clears `b8Triggered` via
  `resetB8ForRetry()` and the next throttled reconcile recreates Koros so
  the arena can be challenged again.
- `KorosEchoEntity` persists `b8IntroStarted`, `b8DialogueStart`, and
  `b8DialogueStep`, and refuses to start the intro when `b8Triggered` is true.
  This also protects old saves that still have a Koros alive after a trigger.
- `/finalparadox arena status` shows `b8Triggered` for the B8 record.

## Verification

Passed:

- `gradlew.bat build` succeeds (compileJava, processResources, reobfJar).
- A test server resolved both `/locate structure finalparadox:b8` and
  `/locate structure finalparadox:b8_arena` to a B8 placement.
- All new worldgen JSON resources parse and are picked up by `processResources`.

Still requires in-game observation:

- A fresh world must actually contain a B8 arena in a supported biome.
- Chunk-load adoption must set the arena to READY and spawn Koros at
  `floorAnchor + (8, 2, -5)`.
- The intro dialogue must play once, survive a server reload before starting,
  and not replay after the encounter ends.

## Known limitations

- Vanilla structure generation only affects chunks generated after the mod is
  installed; existing chunks do not receive a B8 arena.
- `ArenaDeploymentData` is a single record per dimension, so only the first
  discovered/generated B8 arena is tracked.
