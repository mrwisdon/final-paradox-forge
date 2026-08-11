# Paradoxical Tome / 典籍-悖谬 reconstruction evidence

## Authoritative inputs

- World/datapack: `Final_Paradox_v1.1.15/datapacks/luisb1202-functions`.
- Resource pack: `Final_Paradox_v1.1.15/resources.zip`.
- Chinese translation: `_tmp_fp_lang/assets/vm/lang/zh_cn.json`.
- Runtime behavior is taken from the command/function chain, not inferred from lore.

## Item stack and levels

`items/tomo/regresion/item_lvl3.mcfunction` creates a `minecraft:book` with:

- `CustomModelData:77`
- `CanPlaceOn:["minecraft:structure_void"]`
- an empty enchantment entry (foil/glint)
- `HideFlags:16`
- tags `lanzable:1`, `tomo:1`, `tomo_paradojico:1`, `tomo_lvl:3`
- translated name `item.book.3.name.1` and lore `item.book.3.lore.*`

`item_lvl2.mcfunction` uses the same model and ability tags with `tomo_lvl:2`; its cooldown lore is
35 seconds (`item.book.6.lore.8.1`) instead of Lvl III's 25 seconds
(`item.book.3.lore.8.1`). `items/tomo/regresion/ini.mcfunction` sets cooldown scores to 700 ticks
for Lvl II and 500 ticks for Lvl III. It contains a 1000-tick Lvl I branch, but this datapack version
has no matching paradoxical-tome Lvl I item creation function.

## Activation and inventory behavior

`items/tomo/index.mcfunction` detects a dropped item carrying `tomo_paradojico:1` and routes it to
`items/tomo/regresion/check.mcfunction`. The check function recreates the matching Lvl II/III stack,
schedules the shared tome cooldown ticker, rejects use while `tomo_cd` is positive, and otherwise
calls `items/tomo/regresion/ini.mcfunction`.

The Forge implementation intercepts `ItemTossEvent`, transfers the exact stack (including arbitrary
NBT) back to the selected slot or inventory, discards the physical item entity, and then checks the
cooldown. It first uses `Inventory.add`, which cannot automatically toss a remainder. In the defensive
full-inventory/occupied-selected-slot case, the tome takes back its selected slot and the exact
displaced stack is tossed under a synchronous bypass flag, so another tome cannot recursively activate.
It does not bind the item to any existing arena Koros echo; the original Sanctuary-specific
acquisition entry remains intentionally unimplemented until that area exists in the mod.

## Timeline and double-Shift state

`items/regresion/ini.mcfunction` stores the player's health and creates an anchor at the activation
position. `items/regresion/run.mcfunction` advances the anchor to 400 ticks. The active state is
therefore 20 seconds. `run_instance.mcfunction` plus `check_tp.mcfunction` implement two distinct
sneak press edges: the first press opens a 5-tick scoreboard window, the key must be released, and a
second press in the window triggers regression. A held key cannot trigger repeatedly.

The Forge state is stored per player in persistent NBT. It records dimension ID, precise XYZ,
initial health, remaining ticks, prior sneak state, and the double-tap window. The timeline is cleared
on successful regression, expiry, death, logout, or dimension mismatch. Cross-dimension regression is
never attempted. Cooldown is separate, persists across reconnects, is copied to replacement player
entities on respawn or other Forge clone flows, and
restores the vanilla cooldown overlay without restarting it every tick.

## Regression, healing, effects, and feedback

`items/regresion/tp_instance.mcfunction` teleports to the anchor without forcing a stored yaw/pitch,
so the Forge implementation preserves the player's current orientation. It applies approximately one
second of Speed II and Resistance 100. The activation function also gives two seconds of Speed II.

The original healing checks use strict comparisons against the health captured before any healing:

- initial health greater than current health + 3: Instant Health I (4 health)
- initial health greater than current health + 7: additionally Instant Health II (8 health)
- initial health greater than current health + 1: Regeneration III for one second

The Forge rule applies the equivalent 0/4/12 immediate health, never lowers current health, clamps to
maximum health, and applies the one-second regeneration branch independently.

Original feedback evidence includes `reverse_portal`, `explosion`, `portal`, `large_smoke`, purple
dust, witch and smoke particles plus beacon ambient/power-select, ender-eye death, enchantment-table,
fire-extinguish, note-block and UI-button sounds. The persistent anchor visuals are deliberately
throttled in Forge (no entities or world scans; bounded particles every 10/20 ticks), while activation,
regression bursts, the capped teleport trail, countdown, expiry, and ready feedback remain server
authoritative.

`items/regresion/msg.mcfunction` displays 5, 4, 3, 2, and 1 seconds at scores 300, 320, 340, 360,
and 380. `end_instance.mcfunction` reports that the effect vanished on natural expiry. The shared tome
cooldown functions report remaining seconds and completion.

## Visual resource

The exact source entry is:

`resources.zip/assets/minecraft/textures/customitems/tomo_paradojico.png`

It is copied byte-for-byte to:

`assets/finalparadox/textures/item/paradoxical_tome.png`

SHA-256: `8CED9B65F5AC97A08F626FDB85AF9BAC0EFB21193D33BEDDE7DAE5BC1E1A230F`.
The authoritative PNG is 18×16 pixels (despite an earlier 16×16 expectation); it is not resized or
redrawn. A standard generated item model replaces the map's book `CustomModelData:77` override.

## Validation boundary

Unit tests cover constants, input edges and timeout, dimension matching, strict healing tiers, health
clamping, and final-tick expiry. Compilation, tests, and the reobfuscated build validate code and
resource packaging only. Inventory/held appearance, toss return behavior with a full inventory,
server sneak-edge behavior, teleport safety, particle composition, audio balance, multiplayer
isolation, reconnect/respawn cooldown display, and parity beside the original map still require an
in-game pass.
