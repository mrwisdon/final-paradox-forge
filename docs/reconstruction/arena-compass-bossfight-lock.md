# Arena Compass boss-fight lock

## Goal

While any real boss fight is running in the arena dimension, the Arena Compass
must refuse to teleport the player back to the overworld. Entering the arena is
never blocked.

## Five-fight state mapping

`ArenaBossFightState.isAnyActive(ServerLevel)` returns the OR of five
authoritative signals. The aggregation is intentionally read-only: it never
force-loads chunks, never rewrites SavedData, and never treats a stale UUID as
an active fight.

| Fight | Signal | Active when |
|---|---|---|
| B1 Apiglo | `ArenaDeploymentData(b1).activeBossUuid()` resolved to a living `ApigloBossEntity` | resolved and `!isWaiting()` |
| B2 Thar Kroo | `ArenaDeploymentData(b2).activeBossUuid()` resolved to a living `TharKrooBossEntity` | resolved and `!isWaiting()` |
| B5 Koyomi/Gariheuz | `B5EncounterManager.isActive(level)` | encounter data active |
| B8 Zombie Supermatrix | `B8EncounterManager.isActive(level)` | encounter data active (includes the countdown; `inCombat` is intentionally not used) |
| Maraw'Thar (B9) | `ArenaDeploymentData(marawthar).activeBossUuid()` resolved to a living `MarawTharBossEntity` | resolved boss is alive |

Design notes:

- A saved UUID counts only while it resolves to a currently loaded living boss
  (`ServerLevel#getEntity` never forces chunk loads). Missing or unresolved
  UUIDs are simply inactive.
- B1/B2 exclude waiting bosses, so pre-battle placement does not lock the
  compass. Maraw'Thar has no waiting gate; the persistent one-shot
  `marawTharTriggered` flag is deliberately not used because it stays true after
  the fight ends.
- B5 and B8 delegate to their encounter managers so countdown, combat, and
  defeat/victory wrap-up remain locked for exactly as long as the encounter
  data is active.

## Item behavior

In `ArenaCompassItem.use()` the check runs only when `returning` is true and
before any target-dimension lookup, teleport, sound, cooldown, or stack
modification. When the fight lock triggers the player sees the actionbar
message `message.finalparadox.arena_compass.bossfight_active` and receives
`InteractionResultHolder.fail(stack)`.

Translations:

- `en_us.json`: "A boss fight is in progress. You cannot return to the overworld."
- `zh_cn.json`: "Boss 战正在进行，无法返回主世界。"

## Automated verification

- Both language JSON files parse with valid JSON.
- Java 17 `compileJava` succeeds.
- `ArenaBossFightStateTest` covers none / each / any OR combinations plus the
  living-non-waiting (B1/B2) and living-boss (Maraw'Thar) rules.
- Existing `ArenaEntitySelectionTest` still passes.
- `git diff --check` succeeds.

## Required in-game verification

- Use the compass while waiting bosses are staged in B1/B2: teleport must
  still work.
- Start each fight (B1, B2, B5, B8, Maraw'Thar), then use the compass from
  inside the arena: no teleport, no sound, no cooldown, actionbar lock message.
- During a B8 countdown, confirm the lock is already active.
- After winning each fight (and during Maraw'Thar's post-victory wrap-up while
  the boss entity is still alive), confirm the compass unlocks exactly when the
  signal goes inactive.
- Enter the arena from the overworld while a fight is active: the entry
  teleport must not be blocked.
- Check the stack keeps its NBT and no cooldown icon appears on the failed use.
