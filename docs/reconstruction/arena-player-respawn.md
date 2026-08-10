# Arena boss-fight respawn override

## Source evidence and defect

The original Final Paradox datapack uses `/spawnpoint @a ...` when its boss
encounters begin. That command accepts the arena's arbitrary coordinates as a
forced spawn point. In the Forge recreation, all five implemented encounters
already call `ArenaCompassDestination.setForArena(level, definition, anchor)`
after they successfully leave the waiting state, but that method previously
updated only Arena Compass NBT.

B8 was the sole exception: its controller called `setRespawnPosition` with
`forced=false`. The selected arena block is neither a bed nor a respawn anchor,
so vanilla can reject it and fall back to the overworld. B8 also replaced the
respawn with the arena dimension's shared spawn during victory and skip cleanup.

## Unified behavior

`ArenaPlayerRespawn.activateForArena` now resolves the existing relative point
and yaw from `ArenaDefinitions.respawnFor`, snapshots each player's original
respawn once, and assigns `anchor + offset` in the arena dimension with
`forced=true`. Because all five fight starts share `ArenaCompassDestination`,
B1, B2, B5, B8, and Maraw'Thar use the same path.

The snapshot contains the original dimension, optional position, angle, and
forced flag. Consecutive fights in one arena visit update the active arena
point without overwriting that original snapshot. The snapshot is saved in the
player's persistent NBT, explicitly copied on death clone, and retained across
logout/reconnect.

The original respawn is restored when the player leaves the arena dimension.
The Arena Compass restores it after its active-boss guard passes and before it
chooses the overworld return destination, so a saved overworld bed remains the
return point. Dimension-change and login events repair exits performed through
commands or other mechanisms. B8's three older non-forced/shared-spawn writes
were removed to keep this path authoritative.

## Required in-game verification

- Start each of B1, B2, B5, B8, and Maraw'Thar; die during the countdown or
  fight and confirm the player respawns at that arena's configured point.
- Confirm staged/waiting bosses do not change the player's respawn.
- Start two different fights without leaving the arena, then leave; the
  original overworld bed or spawn must be restored rather than the first arena.
- Die, reconnect, and die again during an encounter to verify the persistent
  snapshot and clone handling.
- Win, fail, or skip B8 and remain in the arena; death should still use the
  arena point until the dimension is left.
- While a fight is active, the compass must remain blocked and must not restore
  the overworld respawn. After the fight, use the compass and confirm it returns
  to the restored bed/spawn.
- Leave with an administrative dimension teleport and log in outside the arena
  with a residual snapshot; both paths must restore the original respawn.
