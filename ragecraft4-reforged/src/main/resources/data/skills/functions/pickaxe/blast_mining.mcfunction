execute at @s run particle lava ^ ^1.5 ^2.5 1.2 0.7 1.2 0.1 50 force
execute at @s run particle flame ^ ^1.5 ^2.5 1.2 0.7 1.2 0.05 150 force
execute at @s run particle explosion ^ ^1.5 ^2.5 1 0.6 1 0.05 10 force
execute at @s run particle campfire_cosy_smoke ^ ^1.5 ^2.5 1 0.6 1 0.05 30 force
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.2 1.2 0.2

execute at @s positioned ^ ^1.5 ^3 run fill ~-3 ~-3 ~-3 ~3 ~3 ~3 air replace spawner

execute at @s positioned ^ ^0.5 ^2 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5] run function skills:pickaxe/blast_mining_hit
execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^3 run function area:green_crystal/melt_4x3