# Void arena sandboxes

## Approved design

The approved replacement for generic arena travel is one shared, empty arena
dimension containing deterministic, isolated slots. Five placeable sandboxes
select the complete encounters: B1 Apiglo, B2 Thar Kroo, B5 Koyomi/Gariheuz,
B8 Zombie Supermatrix, and Maraw'Thar. The Arena Compass is return-only; it
never selects an initial arena, reads a mutable arena-respawn tag for entry, or
falls back to a dimension shared spawn.

The active key is `finalparadox:arena_void`. It uses a flat generator with one
air layer (codec-safe but still no solid terrain), no structure overrides,
fixed noon, skylight, and ambient light. The old
`finalparadox:arena_dimension` remains registered and unchanged so old region
data is neither deleted nor overwritten. Players in either dimension may use
the compass to return to the overworld, but all new sandbox deployment,
encounter state, fight respawns, and fall handling use `arena_void`.

## Fixed slots and entry evidence

All floor anchors are at Y=128 and consecutive X anchors are 4096 blocks
apart. Export volumes are inclusive. Their X ranges do not approach one
another, and every Y range lies inside the dimension's -64..319 build range.

| Arena | Sandbox registry ID | Floor anchor | Export min..max | Entry offset / absolute | Yaw | Evidence |
|---|---|---:|---|---|---:|---|
| B1 Apiglo | `apiglo_arena_sandbox` | `0 128 0` | `-44 126 -55` .. `126 189 65` | `0 2 0` / `0 130 0` | -90 | Source floor anchor `1331 64 1526`; `0tp_boss1` is `1331 66 1526`. Facing east looks toward the waiting Apiglo, which moves about 16.5 blocks east. |
| B2 Thar Kroo | `thar_kroo_arena_sandbox` | `4096 128 0` | `4043 123 -52` .. `4136 139 47` | `0 1 -32` / `4096 129 -32` | 0 | Export min `-1558 48 2253` plus inverse source offset gives floor anchor `-1505 53 2305`; `0tp_boss2` is `-1505 54 2273`. |
| B5 Koyomi/Gariheuz | `koyomi_gariheuz_arena_sandbox` | `8192 128 0` | `8128 122 -28` .. `8212 183 28` | `9 1 0` / `8201 129 0` | 90 | Export min `-1171 42 1398` plus inverse source offset gives floor anchor `-1107 48 1426`; `0tp_boss5` is `-1098 49 1426`. |
| B8 Zombie Supermatrix | `zombie_supermatrix_arena_sandbox` | `12288 128 0` | `12265 119 -24` .. `12312 132 24` | `14 1 0` / `12302 129 0` | 90 | The source has no `0tp_boss8`. This deliberately reuses the reconstructed, source-derived B8 respawn offset/yaw, safely west-facing toward the core and near Koros. |
| Maraw'Thar | `marawthar_arena_sandbox` | `16384 128 0` | `16320 123 -64` .. `16448 178 64` | `-10 1 0` / `16374 129 0` | -90 | Export min `-6447 45 1684` plus inverse source offset gives floor anchor `-6383 50 1748`; `0tp_boss9` is `-6393 51 1748`. |

The pure `ArenaSlots` mapping owns arena ID, sandbox ID, anchor, entry offset,
and yaw. It is also the only initial-entry source used at runtime.

## Deployment and multiplayer state

Sandbox activation is server-authoritative and evaluates the selected arena's
independent `ArenaDeploymentData`:

1. `IDLE`: validate the fixed volume, begin tiled template deployment at the
   fixed anchor, and store this player's pending arena and origin dimension.
2. `DEPLOYING`: join the same pending request without beginning another copy.
3. `READY`: only accept an exact arena ID and exact fixed anchor, load the
   entry chunk, and teleport to the fixed entry.
4. `ERROR`: clear this player's pending request and report the recorded error.
5. A wrong arena ID, missing anchor, or non-fixed anchor is stale/incompatible
   state. It is rejected instead of silently routing the player elsewhere.

Each player owns pending/current state in persistent player NBT. Several
players may wait for the same deployment independently. Pending requests are
cleared after success, failure, logout, or a dimension change. Current arena
survives a death clone while the player remains in the active dimension and is
cleared on exit. A relog does not duplicate deployment or waiting entities;
the player simply activates the sandbox again after reconnecting.

The existing staged deployment manager remains the sole placer and reconciler,
so repeated clicks cannot create a second template deployment, waiting boss,
Koros/Eothar echo, or encounter controller.

## Compass and fight compatibility

In `arena_void`, the existing aggregate boss-fight lock still blocks compass
return. In the legacy arena dimension the compass always provides recovery so
an old player cannot be stranded. Outside both arena dimensions it displays a
short instruction to activate a boss sandbox and performs no teleport or
cooldown.

A sandbox carried into `arena_void` is also refused while a fight is active,
so placing a second table cannot bypass the same escape lock. Players outside
the active dimension may still request an already available arena slot.

`ArenaCompassDestination` still writes fight-respawn NBT to old compass stacks
for compatibility, but `ArenaCompassItem` no longer reads that NBT. Stale tags
therefore cannot choose a sandbox, anchor, or initial visitor entry. The
existing `ArenaPlayerRespawn` snapshot/restore path remains authoritative for
fight respawns and overworld restoration.

## Void fall policy

The active dimension alone checks players below Y=32:

- A roster member in an active fight receives normal `fellOutOfWorld` lethal
  damage. Existing death events remain the single authority for defeat counts,
  forced respawn/spectator behavior, and no-drop inventory preservation.
- A non-fighting player with an exact READY current-slot record is rescued to
  that slot's fixed initial entry.
- A non-fighting player without a valid current slot is safely returned through
  the normal overworld restoration service.

A short persistent tick guard makes rescue/death requests idempotent. Ordinary
void behavior in every other dimension is untouched.

## B10 reservation

`ArenaSlots.B10_TIME_NIGHTMARE` reserves floor anchor `20480 128 0`. The source
footprint is recorded as approximately 44x2x43 blocks (X -298..-255, Y
121..122, Z 2..44). No B10 definition, templates, boss, sandbox block, encounter
logic, recipe, or automatic deployment exists in this implementation.

## Old-save limitations and remaining in-game checks

- Existing `arena_dimension` chunks stay flat/world-generated and are not
  migrated into the void dimension. Existing deployment SavedData in that
  legacy dimension is not copied. The compass is the supported recovery path.
- If an operator has already manually written non-fixed deployment data into
  the new dimension, a sandbox rejects it as stale; this implementation does
  not erase structures or SavedData automatically.
- Build/tests validate code, mappings, JSON, deterministic bounds, and policy,
  but do not prove client visuals or live teleport behavior.
- In game, verify all five table models from inventory and every side, breaking
  drops the matching table, first/repeated/concurrent clicks, completion and
  error feedback, logout during construction, all five exact entries, active
  fight compass lock, fight void death/no-drop/defeat, non-fight rescue,
  invalid-state overworld return, and recovery from the legacy dimension.
