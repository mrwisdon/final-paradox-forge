particle flame ~ ~ ~ 1 1 1 0.03 50 normal
particle smoke ~ ~ ~ 0.5 0.5 0.5 0.1 30 normal
particle lava ~ ~ ~ 1 1 1 0.03 10 normal
particle falling_lava ~ ~ ~ 0.7 0.7 0.7 0.03 80 normal

execute as @e[tag=r4r_ability_target,tag=!arson_hit,distance=..3] run function skills:leggings/arson_hit

execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^3 run function area:green_crystal/melt_2x1