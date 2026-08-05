# Maraw'Thar arena trigger and Eo'Thar pre-battle flow

## Goal

Give the deployed Maraw'Thar (B9) arena the original map's pre-fight flow:
player proximity starts the Eo'Thar echo, the source pre-battle dialogue and
clickable lore/advice menu run, and confirming the challenge hands off to the
existing `MarawTharBossEntity`. The original spectator camera cinematic is
intentionally omitted; a short portal/particle possession effect is used
instead.

## Source evidence

- Trigger: `bossfight/b9/eothar/carga_zona` runs when a non-spectator player
  is within 18 blocks of `-6383 51 1748`.
- Eo'Thar: `bossfight/b10/eothar/gen_eothar` creates the echo marker, the
  right-click prompt, and a villager hitbox.
- Pre-battle text: `cld_lore_116..118`, `b9_dialogo_1..13`, and the Eo'Thar
  menu keys `b9_info_eothar_0..26`.
- The source possession cinematic is `eothar/cinematica_ini/posesion`; this
  recreation keeps its visual feel (portal, dust, explosion) without the
  spectator/camera choreography.

## Implementation

- `EotharEchoEntity` is a registered server entity with a client renderer
  (`EotharEchoRenderer`) showing the original light-blue echo name and the
  "right-click to talk" prompt.
- `ArenaDeploymentManager.tick` spawns the echo when the Maraw'Thar arena is
  READY, not triggered, has no living boss, and a player enters the 18-block
  range. The echo despawns if nobody stays within 30 blocks.
- `ArenaDeploymentEvents.onChunkLoad` adopts a naturally generated
  `finalparadox:marawthar_arena` start as READY, so the same echo and
  boss flow runs for world-generated arenas, not only manual deploy.
- `ArenaDeploymentData` persists `eotharUuid` and `marawTharTriggered`; both
  reset on `begin()` so `deploy marawthar` / `reset marawthar` can retest.
- `/finalparadox eothar_menu <uuid> <action>` handles the clickable menu.
- The challenge confirm plays a 140-tick possession sequence. Two armor stands
  reproduce `gen_conquistador_arrodillado` (kneeling corpse and red boots), the
  head lowers/rises and the right arm raises while the sword appears, then
  `MarawTharArenaStaging.spawnBoss` records the boss UUID, marks the encounter
  triggered, and removes the echo and corpse. The corpse visual is raised one
  block above the original source anchor height so it is not clipped by the
  arena floor.
- `/finalparadox arena start marawthar` remains a debug shortcut; it also
  removes any waiting echo and marks the encounter triggered.
- Player death during the fight switches the victim to spectator. When every
  online player is a spectator, the encounter plays the original defeat title,
  sound and two defeat dialogue lines at 2s/4s, then restarts after 5s: players
  return to the arena entrance in adventure with resistance/heal, the old boss
  and its visuals are discarded, and a fresh Maraw'Thar is staged.

## Verification

Passed:

- `gradlew.bat build` succeeds.
- Dedicated server smoke test summons `finalparadox:eothar_echo` and ticks it
  without errors.
- Dedicated server force-loaded a naturally generated `finalparadox:marawthar_arena`
  start; `/finalparadox arena status` became `marawthar: ready, tiles=18/18`
  with `marawTharTriggered=false`.

Still requires in-game observation:

- Entering the arena should spawn the echo once, play the pre-battle dialogue,
  open the menu on right-click, and after confirmation show the kneeling
  Conqueror corpse animation (head/arm/pose, sword) before Maraw'Thar appears.
- Reload during the dialogue/menu should resume without a second echo.
- All players dying should show the defeat sequence and restart the fight after
  5s without duplicate bosses or stray arena entities.
