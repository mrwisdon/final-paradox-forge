execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=7}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s[predicate=skills:has_vulnerability] weakness 20 1 false
effect give @s[predicate=skills:has_vulnerability] slowness 20 1 false
effect give @s[predicate=!skills:has_vulnerability] weakness 20 0 false
effect give @s ragecraft4reforged:vulnerability 20 0 true

execute at @s run scoreboard players add @p[scores={potion_selected=7,mana=..19}] mana 1
