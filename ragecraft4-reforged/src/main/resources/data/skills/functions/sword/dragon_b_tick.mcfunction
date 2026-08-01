scoreboard players add @s cd 1

execute at @s run execute as @e[tag=r4r_ability_target,tag=!dragon_b_hit,distance=..4] run function skills:sword/dragon_b_hit

execute at @s positioned ^ ^ ^0.5 unless block ~ ~0.8 ~ #minecraft:nonsolid run tag @s add dragon_b_done
execute at @s positioned ^ ^ ^0.5 if block ~ ~0.8 ~ #minecraft:nonsolid run tp @s ^ ^ ^

execute at @s[tag=!dragon_b_done] run particle flame ^ ^ ^ 1 1 1 0.1 10 normal
execute at @s[tag=!dragon_b_done] run particle smoke ^ ^ ^ 0.5 0.5 0.5 0.1 30 normal
execute at @s[tag=!dragon_b_done] run particle lava ^ ^ ^ 1 1 1 0.1 3 normal
execute at @s[tag=!dragon_b_done] run particle sweep_attack ^ ^ ^ 1 1 1 0.01 5 normal

execute at @s[predicate=general:biome_snowy_taiga] positioned ~ ~-0.5 ~ run function area:green_crystal/melt_3x2

execute if entity @s[scores={cd=20..}] run function skills:sword/dragon_b_end