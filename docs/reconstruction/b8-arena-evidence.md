# B8 Zombie Supermatrix arena evidence

Updated: 2026-08-02

## Authoritative sources

- World archive: `Final_Paradox_v1.1.15.zip` (unpacked reference
  `.codex-tmp/fp-world-1.1.15/Final_Paradox_v1.1.15`, region `r.-8.2.mca`).
- Runtime arena boundary: `bossfight/b8/ini.mcfunction` forceload box
  `-3851 1436 -> -3804 1388`; player containment in `bossfight/b8/tp_dentro`
  is a 26-block radius around `-3828 78 1412`; hostile cleanup in `b8/run`
  starts at 29 blocks.
- Handoff coordinate set: player spawn `-3817 79 1413`, matrix core
  `-3828 85 1412`, spectator `-3828 91 1412`, lantern `-3828 86 1412`.

## Structure inside the boundary box

Export bounds (the forceload box): X `-3851..-3804` (48), Y `69..82` (14),
Z `1388..1436` (49) = 32,928 blocks.

- Y 69..77: light-blue-concrete/blue-glass ring wall with gray/black concrete
  banding, ~340 blocks per layer inside the box.
- Y 78: the combat floor. A self-contained diamond of 1,617 non-air cells,
  bbox X `-3850..-3806`, Z `1390..1434`, exactly centered on
  `(-3828, 78, 1412)`. Gray concrete field with quartz ring/line decoration,
  black-concrete accents, blue stained glass, and a quartz outer border.
- Y 79..82: the center pedestal at `(-3828, 1412)` - four gray-concrete
  corner pillars with quartz stairs, an observer (facing down) at Y 79, and a
  three-high blast-furnace column at Y 80..82, topped by quartz slabs/stairs.
- Nothing below Y 69 or above Y 82 exists inside the box; the floor at Y 78
  floats over the ring interior, matching the source world.

The player spawn, matrix core, spectator and lantern coordinates all fall
inside or directly above this box (the core, spectator and lantern are air
blocks in the static world - they are entity/runtime-block positions).

## Export and verification

- Tiles: `src/main/resources/data/finalparadox/structures/arenas/b8/`
  (`tile_0_0_0.nbt` 48x14x48, `tile_0_0_1.nbt` 48x14x1, `manifest.json`).
- Geometry-only policy identical to B1/B2: entities and block entities are
  omitted; command/structure/jigsaw/spawner blocks are replaced with air.
  No unsafe blocks were present in this export (sanitized count 0).
- Independent round-trip check: all 32,928 exported block entries (name and
  full block-state properties) compared against the source region - 0
  mismatches. Tile NBT sizes match the manifest.
- Manifest SHA-256 of the source `level.dat`: `717F3D6E30DD618C4F8AA9960668D7A7C36357D1CABC339325E8BCAFA2F863A6`.

## Deployment

- `ArenaDefinitions.B8`: id `b8`, path `arenas/b8`, size 48x14x49,
  sourceMinOffset `(-23,-9,-24)`, tile grid 1x1x2, tile size 48,
  bossSpawnOffset `(0,7,0)` (matrix core above the floor anchor).
- Floor anchor: arena center `(-3828, 78, 1412)`; minimum corner maps to
  `(-3851, 69, 1388)`; boss spawn maps to `(-3828, 85, 1412)`.
- Deployment policy (user-confirmed 2026-08-02): the official B8 fight is
  **fixed** at the source anchor `(-3828, 78, 1412)`. `deploy b8` always uses
  that anchor and rejects any custom anchor, because the source datapack
  hard-codes absolute coordinates for the player spawn, containment teleport
  (`-3815 79 1412`), spectator platform (`-3828 91 1412`), and reward
  pedestal (`-3861 80 1412`). The M1 encounter controller must read the
  recorded floor anchor (which will always equal the source anchor) and may
  then use those absolute coordinates directly.
- Commands: `/finalparadox arena deploy b8 [anchor]`, `reset b8`,
  `start b8` (spawns the Zombie Supermatrix at the core; the official
  encounter controller is a later milestone).
- `ArenaDeploymentManager` recognizes `ZombieSupermatrixEntity` as the B8
  boss and extends the arena search box upward so the floating core is found.

## Official encounter anchor policy (confirmed 2026-08-02)

The official B8 fight is fixed to the anchor where the player deployed the
arena. The controller reads the recorded floor anchor from
`ArenaDeploymentData` and resolves every runtime coordinate from it; the
source datapack's absolute coordinates are evidence only. Source-absolute to
anchor-relative offsets (`anchor = (-3828, 78, 1412)`):

| Runtime use | Source absolute | Offset from anchor |
|---|---:|---:|
| Player spawnpoint | `-3817 79 1413` | `(11, 1, 1)` |
| Matrix core | `-3828 85 1412` | `(0, 7, 0)` |
| Arena/boundary center | `-3828 78 1412` | `(0, 0, 0)` |
| Containment teleport target | `-3815 79 1412` | `(13, 1, 0)` |
| Spectator teleport | `-3828 91 1412` | `(0, 13, 0)` |
| Lantern setblock | `-3828 86 1412` | `(0, 8, 0)` |
| Add spawn positions (golem/sniper/TNT/zombie robot) | `-3828 74 1412` | `(0, -4, 0)` |
| Sniper bullet start | `-3828 84 1412` | `(0, 6, 0)` |
| H1 path origin | `-3828 78 1412` | `(0, 0, 0)` |
| Skip/victory spectator teleport | `-3814 79 1412` | `(14, 1, 0)` |
| Reward drop (megamatriz_perneras) | `-3861 80 1412` | `(-33, 2, 0)` |
| Forceload box | `-3851..-3804 / 1388..1436` | X `-23..24`, Z `-24..24` |
| Hostile cleanup radius | 29 blocks from center | 29 blocks from anchor |

Containment check: non-spectator players farther than 26 blocks horizontally
from `anchor + (0, 0, 0)` are teleported to `anchor + (13, 1, 0)` (yaw 90).
The deployed structure box relative to the anchor is X `-23..24`, Y `-9..4`,
Z `-24..24`.

## Known boundaries (not silently hidden)

- The decorative light-blue/glass ring and the west plaza continue beyond the
  forceload box on all four sides; the export keeps the map maker's own
  forceload boundary, so deployed arena edges may show a clean cut through
  the outer ring rather than the full surrounding scenery.
- The reward pedestal at `-3861 80 1412` (megamatriz_perneras drop point) is
  outside the exported box on the west plaza. The M7 victory flow must either
  extend the arena export, add a small dedicated pedestal, or account for the
  drop point explicitly.
- The lantern at `-3828 86 1412` is a runtime `setblock` (Y 86), not part of
  the static geometry.
