particle flame ~ ~ ~ 2 1 2 0.03 150 normal
particle smoke ~ ~ ~ 1 0.5 1 0.1 100 normal
particle lava ~ ~ ~ 2 1 2 0.03 40 normal
particle falling_lava ~ ~ ~ 1.3 0.7 1.3 0.03 200 normal

execute as @e[tag=r4r_ability_target,tag=!arson_hit,distance=..5] run function skills:leggings/arson_hit

tag @e[tag=r4r_ability_target,tag=arson_hit] remove arson_hit

execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^7 run function area:green_crystal/melt_3x2