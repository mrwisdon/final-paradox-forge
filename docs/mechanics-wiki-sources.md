# RC4 Mechanics Compendium sources

The in-game mechanics compendium follows the original Ragecraft IV English text and the selected Simplified Chinese
translation. Core crafting, potion, enchantment, and debuff pages reference the original translation keys directly,
preserving punctuation, spacing, and localized terminology. Runtime behavior in Reforged remains authoritative where
the original map UI or progression differs.

## Source mapping

| Compendium section | Original translation keys |
|---|---|
| Runeforge introduction | `npc.functions.shade_nexus.cra_1.3` through `npc.functions.shade_nexus.cra_6.3` |
| Base items, placement, reusable runes, crafting order, costs | `block.command_block.3.command.1` through `.13` |
| Upgrade discovery | `general.functions.dis_upgrade.4` |
| Wands | `item.carrot_on_a_stick.195.*`, `.241.*`, `.287.*`, plus the shared spell cooldown text |
| Accessories | `item.iron_horse_armor.310.*` and `.610.*` |
| Infinite potions and elixirs | `item.lingering_potion.1.*`, `item.potion.2.*`, `.35.*`, `.70.*`, `.107.*` |
| Enchantments and debuffs | `general.functions.encyclopedia.dis_*2.1` and `.2` |

## Intentional Reforged adaptations

- The original physical armor-stand and item-frame placement text is expressed as the equivalent GUI slots.
- Reforged accepts compatible equipment by item class/tag, including compatible modded equipment, rather than only
  the original map's listed vanilla material tiers.
- The original map introduced upgrade runes as discovered reward items. Reforged distributes all 24 upgrade runes
  through tiered natural structure-chest pools based on their Rune Power requirement.
- Crafting costs are shown inside the Reforged Runeforge interface rather than physically in front of the forge.
- Reforged documents its added random crafting recipe for infinite potions alongside the original item text.
- Reforged documents its tiered directed wand recipes, each centered on one Unenchanted Wand, alongside the original
  wand descriptions.
- Reforged adds simple shapeless recipes for all eight reusable suffix-school sigils while retaining their monster
  drop source.
- The original Bonus Monument granted Rune Power. Reforged instead spends one emerald block through the Runeforge
  unlock button, so the compendium labels that behavior explicitly.
- Reforged exposes the original Runeforge repair instructions and follows the executable repair script: the stored
  repair cost is multiplied by ten experience points even though the prose calls the cost "levels." The original
  fixed equipment/material amounts are retained, with compatible modded equipment using a recognized repair
  ingredient when available.
- Area and creature encyclopedia entries are excluded.
