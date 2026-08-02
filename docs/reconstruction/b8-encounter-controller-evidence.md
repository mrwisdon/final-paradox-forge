# B8 encounter controller and persistence (M1)

Updated: 2026-08-02 (M2..M6 added)

## Scope

M1 implements the server-side B8 state machine, durable state, bossbar,
250-point health state, invulnerable/vulnerable switching, arena containment,
spectator handling, countdown-to-summon flow, and idempotent reset/recovery.
M2 adds mount spawning and the ride-to-battle entry. M3 wires rover bullets
to the matrix hit zone. M4-H2 implements the falling gold-module shield break;
M4-H1 and M4-H4 implement the straight-line warning beam and the rotating-area
explosions with their rover damage. M4-H3 spawns four vanilla-based add types
and the sniper bullet system (the hostile acechador is deferred). M5 (phase
timelines), M6 (death/defeat), M7 (victory/reward/cleanup) and M8 (dialogue/
music) remain hooks or stubs.

## Source chain mirrored

| Source | Implementation |
|---|---|
| `b8/ini` | `prepare()`: reset state, snapshot/apply gamerules, spawnpoints, forceload, countdown timers |
| `b8/cuenta_atras/ini..1`, `summon` | timers `countdown_3/2/1` (+60/+80/+100t), `summon` (+120t) |
| `b8/summon` | `summonEncounter()`: spawn matrix at `anchor+(0,7,0)` compact, state PHASE_1 |
| `b8/setvida` + `vida/ini` | `setupBossBar()`: red bossbar, max 100, value = health*100/250 |
| `b8/run` | `runLoop()` every 20t: containment, spectator TP/actionbar, resistance 101, hostile cleanup, bossbar refresh |
| `b8/matriz/hit` + `check_vida` | `setHealth()`/`checkHealth()`: thresholds 200/150/100/50 enter phases 2-5, <=0 defeat stub |
| `b8/matriz/hacer_invulnerable/vulnerable` | `setVulnerable()` drives the model entity |
| `b8/reset` | `endEncounter()`: kill matrix, clear timers/bossbar, unforce chunks, restore gamerules, clear state |

## M2: mounts and fight entry

Source `fase/1/ini` -> `ini_monturas/**` -> `el_montura`:

- At summon, `startPhase1()` shows the phase-1 title/subtitle (`b1.ini_f1.1`,
  `b4.fase.1.ini.1`) and pling, then `spawnMounts()` summons one B8
  `TerrastalkerRoverEntity` per online non-spectator player at the source
  positions: anchor + 14 blocks in yaw `300 + 20*index`, y + 1. Each rover's
  cabin-recovery position is the source fallback translated to the anchor:
  `anchor + (13,1,0)`.
- Mounting is open to the nearest unoccupied rover (source `subirse/ini`); no
  per-rover ownership gate. The controller records rider->rover UUIDs for
  cleanup/recovery and re-spawns only missing rovers on reload.
- After a forced dismount the rover will not auto-mount the same player again
  until that player releases Shift (the double-sneak window is 10 ticks), so
  holding Shift cannot instantly remount and replay the controls guide
  (fixed 2026-08-02). The rider is also placed on the ground behind the rover
  on dismount instead of being left standing inside the cabin.
- `runLoop()` phase 1, ronda 0: when every online non-spectator player rides a
  living B8 rover (`fase/1/run`'s `14_id2>=1` equivalent), it schedules the
  ride countdown (ronda -> 1): 1s/2s/3s titles + bells, `comenzar` at +4s.
- `comenzar()` shows the `b8.fase.1.comenzar.1` title and plays the
  ender-dragon growl; the M4 `h2/ini +1s` and M8 music hooks land here.
- The controller-spawned matrix carries the `boss` tag (source `matriz/gen`),
  which makes `TerrastalkerRoverEntity` reject dismount while the fight runs.
  Outside the encounter the improved mount's double-sneak dismount still works.
- `endEncounter()` kills every spawned rover via the cleanup set.

## M3: bullet hits and interception

Source `matriz/run_vulnerable` + `hit` and `matriz/run_invulnerable` + `block`,
sampled at the matrix core +0.2 Y:

- Hit center: the matrix entity position plus 0.2 (source hitbox ends at
  core-1.3 and is sampled at +1.5). Same center in both states.
- Vulnerable: any rover bullet within 1.5 blocks of the hit center triggers
  `hit`: gold-block/cloud/explosion particles, two gilded-blackstone break
  sounds, health -1, and every bullet inside the 1.5 zone is removed. Damage
  is applied at most once per tick (source runs `hit` once per tick).
- Invulnerable: bullets within 3.0 blocks are intercepted (`block`): anvil
  sound, cloud + crit particles, bullet removed, no health change.
- `TerrastalkerRoverEntity.tickBullets` evaluates each bullet from its current
  server position (`next`) after movement, so the firing point can never go
  stale, and consumes bullets outside the iteration (no concurrent
  modification). A bullet is removed on hit/block/lifetime, so it can never
  hit twice.
- Bullet direction uses the rider's full look angle (pitch included), matching
  the source cannon aim, so players shoot up at the floating matrix.
- The 200/150/100/50 phase thresholds and 0-health defeat stub were already in
  place from M1 (`checkHealth`), and every phase entry closes the matrix and
  makes it invulnerable; opening it again is the M4 H2 loop.

## M4-H2: falling gold modules and shield break

Source `h2/{ini,pos,gen,run,romper,boom,end,reset}`. The controller schedules
`h2_ini` at +1s from `comenzar` (source `fase/1/comenzar`).

- Candidate selection (`pos`): 32 directions (11.25 deg, each with a 50%
  +/-5.625 deg jitter) x 10 rings at radius 7..25 (step 2) x 5 random picks
  per ring = up to 50 candidates at marker y = anchor + 10. Keep 20 random,
  then 12 more per online non-spectator player (source `b8_h2_pre_pos_safe`).
- One module spawns per tick from the remaining pool (`run` -> `gen`), with
  the source Y offset roll 0/2/4/5/6 (1/5, 4/15, 4/15, 2/15, 2/15), plus the
  ender-eye launch/death sounds.
- `B8H2ModuleEntity` remains one lightweight entity per module rather than a
  networked armor stand. It keeps the server hit position at the selected sky
  point while its renderer reproduces the source armor stand's three-client-
  tick teleport interpolation from anchor + `(0, 6.5, 0)`, creating the fast
  outward throw. It then falls 0.036 blocks/tick, rotates 3 deg/tick with
  partial-tick smoothing, emits the golden dust at +2, and carries the source
  yellow glowing outline.
- The renderer now matches the vanilla full-size armor-stand head item: the
  gold block is centered at +1.6875, scaled to 0.625, and rotated around its
  own center. This replaces the previous full-block, off-center transform
  which made every module orbit/wobble while spinning.
- Landing: stand Y <= anchor Y - 1.5 triggers `boom` at +1.7: squid-ink ring,
  explosion, generic-explode sound, then 5 damage to every B8 rover
  (source runs `danar_montura` per `14_montura_core`).
- Bullet break (`romper`): a rover bullet within 1.5 of module +1.4 breaks the
  module (explosion/cloud/gold-block-item particles, ender-eye-death sound)
  and is consumed.
- After the last module is gone and no spawns are pending, the matrix opens
  vulnerable after the source 2-second delay. The end condition is stricter
  than the source (also waits for pending spawns) so reload or re-entry can
  never open the matrix early; `enterPhase` clears any active H2 cycle before
  closing the matrix.
- Phase 2..5 entries now show the source titles/subtitles and pling, and the
  translations for phases 2-4 were copied from the VM pack / original English
  (`b8.fase.2/3/4.ini.1`; phase 5 reuses the existing `b3.fase.5.ini.1`).
- `endEncounter`/`onDefeat`/`prepare` all clear H2 modules and state.

Known H2 difference still to verify in-game: the squid-ink landing ring is
rendered as 60 ring positions instead of the source's 64 exact offsets. The
corrected launch interpolation, yellow outline, head-item scale/placement and
centered rotation require a visual client pass before parity can be claimed;
see `b8-handoff-to-gpt.md`.

Visual test command: `/finalparadox b8 h2` while a B8 encounter is active.

## Anchor policy

All runtime coordinates derive from the player-deployed floor anchor stored in
`ArenaDeploymentData` (see `b8-arena-evidence.md`). No absolute source
coordinates are hard-coded in the controller.

## Persisted state (`B8EncounterData`, `finalparadox_b8_encounter`)

- active, anchor, state (IDLE/COUNTDOWN/PHASE_1..5/VICTORY/DEFEAT/CLEANUP), fase, ronda
- health/healthTotal (250), vulnerable, addCount, matrixUuid
- participants, spectators, mountOwnership (M2), pending timers (type + due game time), cleanup entity set
- gamerule snapshot (doImmediateRespawn, mobGriefing, doMobSpawning, keepInventory,
  doFireTick, naturalRegeneration, randomTickSpeed) for restore on reset

## Reload recovery

`B8EncounterManager.tick` -> `requireController` recreates the controller from
saved data: bossbar re-added, forceload re-applied, matrix re-found by UUID (or
respawned if missing), timers remain valid because due times are stored in
absolute game time. Double tick is impossible: one controller per dimension.

## Commands

- `/finalparadox arena deploy b8 [anchor]`, `arena start b8` (official entry)
- `/finalparadox b8 status` - state/fase/ronda/health/vulnerable/addCount/matrix/timers
- `/finalparadox b8 health <0..250>` - boundary smoke test (201/200, 151/150, 101/100, 51/50, 1/0)
- `/finalparadox b8 phase <1..5>` - force phase (dev test)
- `/finalparadox b8 vulnerable|invulnerable` - state switch smoke test
- `/finalparadox b8 reset` - idempotent teardown
- model test commands (`/finalparadox supermatrix *`) refuse while the encounter is active

## Deliberate M1 boundaries

- Phase-1 countdown after mounting and the H2 opening wait for M2/M4; phase 1
  currently keeps the arena rules running with the matrix compact/invulnerable.
- `gamemode adventure @a` from `boss_gamerules` is not forced yet (M2/M6 flow).
- Victory at 0 health is a stub (M7): matrix dies, bossbar removed, state VICTORY.
- `onBulletHit` is an empty hook for M3.
- English bossbar/actionbar text follows the original resource pack
  (`Zombie Megamatrix`); Chinese follows the VM translation pack.

## M4-H1: straight-line warning and advancing explosion

Source `h1/{ini,ini3,gen,gen_random,run,run2,gen_warn,warn_path,warn_path2,
rayo/explosion,reset}`:

- `startH1(beams=1)` aims one beam at a random non-spectator player; mode 3
  (source `h1/ini3`) adds two random-yaw beams. All start at the arena center.
- The beam locks the player's position once at generation and keeps that
  heading (no continuous tracking), standing still for the first 100 ticks.
  Every 10 ticks a warn marker spawns at the beam (`gen_warn`) inheriting the
  beam's yaw, then travels 4x0.4 per tick along that heading with the end-rod
  side/center particle pattern, and is killed beyond 23 blocks from center.
- From tick 101 the beam advances 2 blocks/tick toward its aim and explodes
  every two ticks (`danom2` resets after each explosion), until killed at 114.
- Explosion (`rayo/explosion`): 24-position 3.0 cloud ring, 24 end-rod ring
  (0.1 speed), explosion/flash, gray-concrete debris (100), large smoke, the
  center line of end rods from `anchor+(0,8.5,-0.5)` to the head, generic
  explode + firework-launch sounds, then 5 damage to every rover within 4
  blocks - once per round per rover (`b8_h1_damaged` equivalent).

## M4-H4: rotating area explosions

Source `h4/{ini,ini2,ini3,gen,giro,run,run2,explosion,reset}`:

- Variants spawn 1 (ini), 2 (ini2, second at +0.5s), or 3 (ini3, seconds at
  +0.5s and +4.5s) scan sources, each taking a distinct radius from
  {7, 14, 20} (source as3/as2/as1).
- For the first 19 ticks each source rotates 20 deg/tick (two 10-deg giros)
  and drops a warn marker at its radius (y+1) unless one exists within 5.
- Warn markers rotate 7 deg/tick, emit the two end-rod preview particles, and
  explode at tick 100.
- Explosion shares the H1 visuals but with a 3.6 cloud ring, 0.12 rod speed,
  and 5 damage to every rover within 5.5 blocks (no per-round tag).
- Sources die at tick 119; when no sources/warns remain the hazard turns off.

Test commands: `/finalparadox b8 h1 [1|3]`, `/finalparadox b8 h4 <1..3>`.
The M5 phase timelines will call these entries with the source schedules.
All hazard state persists (beams/warns/sources/damaged set), and
`prepare`/`endEncounter`/`onDefeat` clear H1/H4 so reloads cannot leave ghost
hazards.

## M4-H3: adds

Source `h3/{pos,run_ini,reset}` + `zombie_robot`, `sniper`, `golem`, `tnt`,
`acechador`. Spawn points: 64 markers on a 25-radius circle at the walkable
floor level (anchor+1; the source's anchor-4 spawn was shifted up per user
feedback 2026-08-02 so adds no longer appear below the platform), 5.625-deg
steps starting at 5.625 deg matching the source list order; markers within 10
of any non-spectator player are removed. (H2's 32 marker directions likewise
start at 11.25 deg; both rotations were corrected 2026-08-02 after a reported
relative-coordinate offset.)
Per-wave counts (source `*_pos`): zombie 8 + 4xplayers, sniper 2 + players,
golem 1 + 2xplayers, TNT 6 + 2x(min 3, players).

- Zombie robot: invisible vanilla zombie, 15 HP, follow 60, speed 0.21, dyed
  leather armor + one of three player-head textures (or bare), name
  `h3.zombie_robot.gen.1`.
- Sniper: NoAI/NoGravity skeleton hovering 3 blocks above the walkable floor
  (tunable `H3_SNIPER_HOVER_OFFSET`; raised from platform level per user
  feedback 2026-08-02), 30 HP, speed 0, leather armor + skull head, fires
  every 9 ticks toward the nearest rover.
- Mad Golem: iron golem, 150 HP, speed 0.25, `afijo_aplastante`.
- TNT skeleton: skeleton with a TNT head, 15 HP, speed 0.18, `afijo_detonante`.
- All adds carry `hostile`/`b8_add`/type tags, zero drop chances, and run a
  20-tick setup that advances them 0.4/tick toward the matrix while jumping
  up 10 blocks (first 10 ticks) and back down 10 (last 10 ticks), landing on
  the floor; the peak was raised from floor+5 to floor+10 per user feedback
  (2026-08-02). Each setup tick also zeroes the add's motion, so the teleport
  arc cannot accumulate fall velocity and tunnel through the floor on landing.
  The golem/TNT `b8_h3_arrow` falling snowball fires at tick 20. Golems get
  `AngryAt` set to the nearest non-spectator player each tick.
- Burst (`hit`): when a zombie robot or TNT skeleton takes damage, 1/2 chance
  to strip helmet+chestplate, reset health to 15, play lava/totem/terracotta
  particles + explosion sound, and tag `b8_h3_reventado`.
- Sniper bullet: `B8SniperBulletEntity` (conduit renderer), 0.4/tick along the
  direction locked at spawn toward the nearest rover, smoke/flame particles,
  killed beyond 30 from anchor+(0,6,0). Explodes on solid block at +1.7, a
  rover bullet within 1, or a rover within 2; rover damage 2, explosion/lava
  particles, 16-flame ring, netherite-step (and blaze-hurt) sounds.
- Add count (`fase/recount_adds`): recounted from living `b8_add` every run
  loop into `addCount`; `h3Cleanup` removes all adds/bullets on prepare,
  defeat and reset.

Test command: `/finalparadox b8 add <zombie|sniper|golem|tnt>`.

## M5: five-phase round timelines

Source `fase/1..5/{ini,run,index,ronda*}`. `runLoop` (every 20t) recounts adds
and dispatches `runPhase`, which advances `ronda` per the source conditions:

- Fase 2/3: rounds 1-2 advance when adds <= 3, round 3 when adds == 0.
- Fase 4: rounds 1-2 when adds == 0, round 3 when adds <= 3, round 4 when
  adds == 0.
- Fase 5: rounds 1-2 when adds == 0; rounds 3-6 when no `14_acechador_core`
  exists (with the acechador deferred this cascades through to round 7).

Round contents are copied exactly from the source `ronda*` files, including
per-round add waves (zombie/sniper/golem/TNT with the source counts) and the
final-round hazard timelines converted to exact ticks (1t, 6s=120, 12s=240,
13s=260, 18s=360, 21s=420, 24s=480, 28s=560, 30s=600, 31s=620, 34s=680,
35s=700, 38s=760, 40s=800, 42s=840, 46s=920, 49s=980). Every schedule entry is
one persisted absolute-game-time timer, so reloads cannot duplicate rounds or
wave dispatches.

- Fase 1 is unchanged: ride countdown -> comenzar -> H2 at +1s.
- Fase 2 ronda 4, fase 3 ronda 4, fase 4 ronda 5 and fase 5 ronda 7 each end
  with an `h2/ini` that opens the matrix, letting bullets drop it to the next
  phase threshold; `enterPhase` closes the matrix and clears the H2 cycle.
- Fase 5 rounds 3-6 (acechador) are a stub: the source evoker sound and
  tellraw translations play, but the hostile acechador itself is deferred.

## M6: vehicle damage, player death and defeat

Source `b8/{morir,derrota,respawn}` and `danar_montura*`:

- Encounter-mode rover energy damage announces the original tellraw
  `<!> -<amount>% 能量` in red (source `b8/danar_montura.1..3`) instead of the
  generic mod message; each hazard still damages only the ridden rover.
- Vehicle destruction keeps the existing rover meltdown (kills the rider at
  zero energy), which now feeds into the death flow below.
- `onPlayerDeath`: the dead player becomes spectator, is teleported to
  `anchor + (0,13,0)`, and when every online player is a spectator the defeat
  flow fires (`b8/morir`).
- `defeat`: state DEFEAT, title/subtitle `§4☠` / `§4§l战败！` (`b1.derrota`),
  wither-death sound, and a persisted +100t respawn timer.
- `respawn`: full encounter cleanup, spectators restored to survival and
  teleported to `anchor + (14,1,0)` yaw 90, wither cleared, resistance 101
  applied, ready to re-challenge (`b8/respawn`; the source's adventure-mode
  restore is replaced by survival because the mod never forces adventure).

### Acechador (hostile Terrastalker) - deferred

`h3/acechador/pos` -> `carga_lanas/14_verde/el_acechador/**` is a separate
89-file subsystem (22 visible parts, autonomous pathfinding and attack
behaviors) that must not be folded into the player mount. It is intentionally
NOT implemented in this pass and is recorded here as a pending H3 item for a
dedicated follow-up; the arena-relative spawn circle (16 positions at radius
10 plus center) is documented in `h3/acechador/pos.mcfunction`.

### H1 fix (2026-08-02, user report)

Reported: H1 only showed particles on one fixed line instead of following the
locked player. Root cause: the source's `gen_warn` teleports the warn marker
without copying the beam's rotation, so it travels with its own yaw (0, +Z),
while the beam aims at a player. Deliberate deviation from the literal chain:
warn markers now inherit the beam's yaw and travel toward the player position
locked at generation. The beam locks once at creation and does NOT re-aim
continuously (confirmed by user on 2026-08-02); the two random beams of
`h1/ini3` keep a fixed random yaw (`gen_random` behavior).
