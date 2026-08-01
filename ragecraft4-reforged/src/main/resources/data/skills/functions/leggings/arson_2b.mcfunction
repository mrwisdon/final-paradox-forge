particle flame ~ ~ ~ 1.5 1 1.5 0.03 100 normal
particle smoke ~ ~ ~ 0.8 0.5 0.8 0.1 70 normal
particle lava ~ ~ ~ 1.5 1 1.5 0.03 20 normal
particle falling_lava ~ ~ ~ 1 0.7 1 0.03 120 normal

execute as @e[tag=r4r_ability_target,tag=!arson_hit,distance=..4] run function skills:leggings/arson_hit

execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^5 run function area:green_crystal/melt_2x1