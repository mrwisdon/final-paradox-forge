# B5 boss reconstruction baseline: Koyomi x Gariheuz (dual fight)

> 2026-08-01 correction: the older rewrite notes below contain superseded values
> (notably Gari 1000 HP / 680 / 370) and outdated remaining-difference claims.
> Use `b5-parity-audit-2026-08-01.md` as the current implementation audit.

## Authoritative references

- World archive: `Final_Paradox_v1.1.15` (same unpacked copy as `boss-baseline.md`).
- Datapack: `data/luisb1202/functions/bossfight/b5/**`.
- Chinese text: VM translation V3 keys `luisb1202.functions.bossfight.b5.*`.

## Arena

- Exported interior: `src/main/resources/data/finalparadox/structures/arenas/b5/`
  (8 tiles, 85 x 62 x 57; X -1171..-1087, Y 42..103, Z 1398..1454).
- Verified 300,390 blocks against the v1.1.15 region source: exact, no gaps/duplicates.
- Includes the enclosed combat well (platform, moat, perimeter walls, open shaft) and
  the polished-andesite/glowstone ceiling; the entrance hall and palace floor are excluded.
- Anchor: player spawn `-1107 49 1426`; bosses `-1148 49 1428` (Koyomi) and
  `-1148 49 1424` (Gari). Forceload `-1165 1447 -1093 1405`.
- Test helper: `function finalparadox:test/place_b5_arena`.

## Boss entities

| | Koyomi (koyo_boss) | Gariheuz (gari_boss) |
|---|---|---|
| Base mob | zombie | pillager |
| Health | 920 | 800 |
| Speed | 0.24 | 0.29 |
| Armor | 20 | 7 |
| Attack | - | 15 |
| Hand | trident (Unbreakable, RepairCost 999999) | crossbow |
| Armor | iron boots; leather leggings #5283427 prot2; leather chestplate #3038778 prot3 | - |
| Head | player_head (Koyomi skin texture) | - |
| Loot | default | empty (DeathLootTable) |
| Rotation | [-90, 0] | [-90, 0] |
| Tags | koyo_boss, dual_boss, boss, hostile | gari_boss, dual_boss, boss, hostile |
| Name key | `...b5.summon_koyo.1` | `entity.pillager.6.name.1` |

Both: PersistenceRequired, CustomNameVisible. Koyomi starts with tag `b5_h1_shield`.

## Encounter flow

- `ini`: forceload; `fase=3`; disable escapes; minikoros end; countdown; `tp_dentro`;
  spawnpoint `-1107 49 1426`; `barriers`; resistance 6 101 + instant health;
  mosquito/fiebre resets; `summon_iddle`; check player count; dialogue ini16.
- `summon`: despawn old; summon both bosses; growl sound; `fase/1/ini`; shield Koyomi;
  `h1/switch`; health bars (`vida/ini` + `setvida`); schedule `run` every 20 ticks.
- `run` (20-tick loop): revive dead bosses unless fase 2/4; phase dispatch on score `fase`
  (1/2/3/4/5/6 -> fase 1/inter1/2/inter2/3/4); `tp_dentro`; shield effects
  (resistance 10, slowness 4); `confianza/run_slow` on unshielded; `separar` when both
  bosses are within 2 blocks; victory when no boss remains; water check at y=43;
  spectator handling; boat cleanup; levitation/jump-boost clear; fleccy/water fall checks.

## Phases

| Phase | Entry condition | Content |
|---|---|---|
| 1 | start | Shielded Koyomi increments `h5`; h5>=35 -> h5. Shield switch at `boss_vida<=68` & `b5_shield_change=1`; inter1 at `boss_vida2<=68`. |
| inter1 (fase 2) | above | Title; shield back to Koyomi; Koyomi Invulnerable+NoAI, tp `-1088 64 1426`; dialogue ini7; poison pools at 5s; h3 loco at 17.5/24.5/31.5/38.5s; h4 at 46.5s. |
| 2 | inter1 end | Shielded Koyomi increments h5/h3; shielded Gari increments h7; triggers h5>=35, h3>=35, h7>=14; shield switch at `boss_vida2<=37`; inter2 at `boss_vida<=37`. |
| inter2 (fase 4) | above | Like inter1 (dialogue ini8); h4 at 15s; h3 loco at 20s. |
| 3 | inter2 end | Same counters as 2; shield switch at `boss_vida2<=6`; fase 4 at `boss_vida<=6`. |
| 4 (fase 6) | above | Reset all attacks; bosses Invulnerable + NoAI 0 + slowness/weakness infinite; Gari hand cleared; shield removed; cinematic tp_end -> dia_end -> `gari_irse` (reward + despawn Gari) -> `open_fleccy` -> victory. |

## Shield / trust (h1)

- Shielded boss gets resistance 10 + slowness 4 every run tick; particle ring at +0.4 y.
- `h1/switch` moves the shield to the other boss, increments `b5_shield_change`,
  schedules h1 particle loop, resets h2, and picks the next attack:
  Gari shielded -> `h2/ini` at 5s + h5 reset; else h2 reset.
- `confianza`: the unshielded boss is "confiado" (purple name) when both bosses are within
  15 blocks of each other, otherwise "desconfiado" (gray name); crit particles while trusted.

## Attacks

### h5 Trident volley (Koyomi)
- Trigger: `h5>=35` while Koyomi shielded.
- Gari fires a homing projectile per volley: spawn at Gari +1.6 y, targets a random
  non-spectator player (`b5_h5_id`), crossbow + drown sounds, totem/explosion particles.
- Movement: 0.16/tick toward the target within 12 blocks, 0.1/tick outside; extra 0.04 at
  danom 260/360 and 0.15 at 460; homing on the player's eyes; purple+green dust trail.
- Hit: within 1.4 of a player -> 24 crit-particle fan; kills when target leaves to spectator.

### h3 Trident barrage (Koyomi)
- Trigger: `h3>=35` while Koyomi shielded (phase 2+).
- 12 scheduled throws at 15-tick intervals (15..180t); rotating armor-stand tridents
  (+7 deg/tick); impact at danom 35 -> explosion + cloud + campfire smoke +
  dark-prismarine/andesite item debris fan.
- "Loco" mode during intermissions: trident stands placed along the arena Z axis
  (starting `-1095 49 1445.75`, every 5.5 blocks).

### h2 Water bomb (Koyomi)
- Trigger: Gari shielded -> `h2/ini` at 5s after a shield switch.
- Koyomi advances to `-1129 49 1426`, slams (golpear), a bomb drops on the selected player
  (random non-spectator, announced in actionbar: "XX is the target!").
- Bomb falls from 6 blocks above the target; on water/ground -> boom: weakness 10 to all +
  escalating instant damage (amplifier 1..5 by `b5_h2_dano`); strength to the selected
  player, speed to all.

### h7 Shot + circular teleport (Gari)
- Trigger: `h7>=14` while Gari shielded (phase 2+).
- Gari rotates +22.5 deg/tick and fires; alternates with h6 circular repositioning
  (24 points on a radius-14 ring at Y49, teleporting 0.9/tick toward the target, ends
  within 1.5 of it).

### h4 Illusion phase
- Scheduled at 46.5s (inter1) / 15s (inter2); evoker cast sound; Koyomi theme music.
- Poison pools: green (`0.533 1 0`) and purple (`0.757 0.243 0.859`) dust zones;
  players tagged `b5_h4_veneno_verde/morado`; real vs illusion Gari
  (`gari_boss_real` / `gari_boss_ilusion`).
- Projectiles: purple dust + end-rod pillar, move 0.23/tick, hit within 1 -> resistance,
  instant damage, weakness 5, poison 10.

## Health bars

- Bossbar `luisb1202:boss2` (red, max 100) = combined/Gari segment, refreshed at
  100/75/50/25; `luisb1202:boss` = Koyomi health. Names via `vida.ini.1` / `setvida.1`.

## Death / revive / defeat

- `run` revives dead bosses unless fase 2/4: kill + resummon at spawn with 0.02 HP,
  NoAI 0, Invulnerable, infinite slowness/weakness, Gari hand cleared; Koyomi re-shielded.
- `morir`: spectators tp `-1108 55 1426`; defeat when no players remain.
- `derrota`: defeat titles, wither-death sound, respawn in 5s, dialogue ini15.

## Victory & reward

- `victoria`: victory titles, level-up sound, spawnpoint `1127 117 -55`, spectators tp
  `1519 109 1398`, survival restore, `ciane_boss=4`, celebration.
- Reward: `chapa_gariheuz` (orange dye, CanPlaceOn structure_void, name `recompensa.1`
  #987764, lore keys 1-4) with flash/explosion particles and pickup sound while falling.

## Key constants

- `fase` score: 1 start, 2 inter1, 3 phase 2, 4 inter2, 5 phase 3, 6 phase 4.
- Shield thresholds: boss_vida<=68 -> inter1; boss_vida2<=37 -> inter2; boss_vida2<=6 ->
  fase 4; combined `boss_vida2` drives the second bar.
- Separation: both bosses within 2 blocks -> Koyomi pushed toward `-1131 49 1426`.
- Water fall: y=43 -> tp `-1129 49 1426` + instant damage.

## 2026-08-01 controller rewrite (fidelity pass)

The first Java controller was a loose approximation. After a line-by-line
comparison against `bossfight/b5/**` (v1.1.15), the controller was rewritten:

### Architecture

- `B5EncounterData` (SavedData, per dimension) persists the durable fight state
  (fase, counters, shield bearer, anchor, boss UUIDs, intermission/timer state)
  and survives server reloads; transient visuals are rebuilt on load.
- `B5EncounterManager` drives the fight from the server tick event, so it no
  longer depends on a boss entity's `tick()`; boss deaths/reloads cannot stall
  the run loop.
- `B5EncounterController` ports the source command chains to Java.

### Fixed against the source

| Item | Source | Rewrite |
|---|---|---|
| Phase-1 shield | `summon` + `h1/switch` -> **Gari** shielded, `b5_shield_change=1`, `h5=25` | shield starts on Gari; h5 counter starts at 25 |
| Shield effects | `resistance 1 10` + `slowness 1 4` | amplifiers 10 / 4 |
| Trust effects | `resistance 1 3` + `speed 1 0` | amplifiers 3 / 0 |
| Gari HP | `Health:1000f` (MaxHealth 800) | MAX_HEALTH 1000 (1.20.1 clamps Health to max; effective durability identical) |
| Fase 2/3 reset | `Health 1000` then `*0.68` / `*0.37` | absolute 680 / 370 |
| h5 projectile | 0.16 (<=12) / **0.1** (>12), +0.04@260/360, +0.15@460 | same |
| Revive | kill + resummon at spawn, 0.02 HP, invulnerable, shield swap | same (remove + resummon via manager) |
| h2 | walk -> `golpear` -> vision marker arc (pitch = score) -> boom -> water redirect | full choreography + strength/speed + reselect target + `instant_damage` 1/2/2/3/4/5 |
| h3 | per-player markers, Koyomi trident (rises 7, dies at 4), ground trident (dies at 62), radius 3.8 + weakness, loco every tick, -90 frames at end | same |
| h4 | 11 ghost trails -> 1 real + 5 purple + 5 green illusions, 75 s sidebar timer, color-switching poison pools, projectiles only from illusions >16 blocks (2 per volley at 1/11/21 t), fake-kill explosion + `instant_damage` I/V | same |
| h6 | 24-point ring filtered by floor/box, target = point with fewest players within 12, 2 x 0.9 tps, Koyomi pushed toward `-1131` | same |
| h7 | 3 bursts (0.5/1.8/3.1 s) interleaved with h6 | same |
| Run loop | tp_dentro box, water fall (y=43 + fleecy pit), spectator teleport, levitation/jump clear, boat kill, `separar` (Koyomi 4 blocks toward -1131) | same |
| Finale/victory | reset + iddle bosses + `dia_end` chain -> Gari drops badge -> Koyomi leaves -> fleecy box opens -> victory | same; reward item carries full NBT (name/lore/glint/HideFlags 16/CanPlaceOn) |
| Defeat | derrota -> respawn 5 s later (reset + iddle bosses) | same |
| Countdown | 3/2/1 at 3/4/5 s, fight at 6 s + bell + Koyomi music | same |
| Music | koyomi_main_intro/loop, inter_intro/loop, inter_final, abatir_jefe (record source, exact delays) | registered + ogg files copied |
| Text | all titles/dialogues/bossbars/reward use original `luisb1202.*` keys | 530 keys copied verbatim from VM zh_cn + original en_us |

### Known remaining differences

- Poison-pool mechanic is single-player faithful (one green pool, color flip on
  step); the multiplayer `recu_ini_veneno` variant (per-player pool layout) is
  not ported - all players are tagged purple.
- Scoreboard team colors for green/purple players (name color, sidebar
  DeathCount) are not shown; the color tags and per-color dust still work.
- `ciane_boss` score, spawnpoint changes, wool clone and the celebration
  sequence are map-story integration and are skipped.
- Dialogue lines use the exact translation keys; the source's animated
  newline-blocking preamble is not reproduced.
- h4 pool/illusion colors render for all players (the source filters dust by
  team); gameplay assignment is unaffected.

### Build

- `compileJava` and full `build` pass; `finalparadox-0.1.0.jar`
  SHA-256 `3A41C00A3F24E3BE4810218AD6B3B7DBE84DC2E97CD772183261219BA356BD5A`.
- In-game observation still required: countdown, phase 1 shield polarity,
  h2 bomb arc, h4 trail/illusion fight, pool color switching, finale dialogue
  and reward pickup.
