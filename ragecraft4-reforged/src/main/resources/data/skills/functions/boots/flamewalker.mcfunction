execute at @s unless entity @e[type=marker,tag=flamewalker,distance=..2] run summon marker ~ ~ ~ {Tags:["marker_tick","flamewalker"]}
effect give @s[scores={mana=20..}] fire_resistance 1 0 true

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_small