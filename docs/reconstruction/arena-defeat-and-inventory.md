# Arena-scoped defeat and death inventory

## Source evidence

The original map's shared `bossfight/morir_index.mcfunction` changes a dead
player to spectator before dispatching to the active boss-specific `morir`
function. Each implemented encounter then declares defeat when no
non-spectator player remains. B1 schedules its defeat dialogue after 3 seconds
and `b1/respawn` after 6 seconds; B2, B5, B8, and B9 use the corresponding
spectator/reset flow.

The original `bossfight/boss_gamerules.mcfunction` also sets
`keepInventory true` and `doImmediateRespawn true`. Before this correction,
only the Forge B8 controller temporarily applied those global rules. B1, B2,
B5, and Maraw'Thar could therefore drop or lose a player's inventory.

## Forge adaptation

The source used global `@a` selectors because its party shared one boss world.
The mod can host several deployed arenas in one dimension, so
`ArenaFightParticipants` persists a separate roster for each arena. A roster
is captured from non-spectator players inside the arena structure bounds when
the fight starts. Death, logout, or leaving the arena dimension eliminates
only a recorded participant; unrelated players cannot trigger or block another
arena's defeat. Rejoining an unfinished fight restores the eliminated player
to spectator mode.

B1 now follows the source defeat path instead of relying on a 200-tick empty
proximity fallback: death changes the participant to spectator, all defeated
participants start the title/sound sequence, the original dialogue plays at
3 seconds, and the waiting encounter is rebuilt at 6 seconds. The existing B2,
B5, B8, and Maraw'Thar paths use the same roster policy while retaining their
own reset timings and game-mode recovery.

`ArenaDeathInventory` snapshots the exact vanilla inventory and selected
hotbar slot during `LivingDeathEvent`, cancels the resulting player drops, and
loads the snapshot into the death clone. This is scoped to recorded arena
participants and avoids changing the whole dimension's `keepInventory`
gamerule. B8's existing game-rule snapshot remains compatible; loading the
same inventory over the keep-inventory clone is idempotent.

## Required in-game verification

- Start each implemented fight with two players, kill one participant, and
  confirm only that player becomes a spectator while the other continues.
- Kill the final participant and verify the defeat title, dialogue, timing,
  cleanup, game-mode restoration, and waiting encounter recreation.
- Run two different arena fights in parallel and verify a death in one does
  not affect or block the other.
- Log out or use an administrative dimension teleport during combat; the
  player should count as eliminated and return as spectator if the attempt is
  still active.
- Die with items in the hotbar, main inventory, armor, and offhand. Confirm no
  item entities spawn and every slot plus the selected hotbar slot is restored.
- Repeat the inventory check in B8, where the encounter also temporarily uses
  the vanilla keep-inventory game rule.
