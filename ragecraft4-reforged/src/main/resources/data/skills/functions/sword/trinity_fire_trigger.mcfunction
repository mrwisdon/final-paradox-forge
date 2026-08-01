execute at @s run kill @e[type=snowball,tag=trinity_fire,distance=..2]

execute at @s run particle explosion ~ ~0.7 ~ 0.2 0.2 0.2 1 1 normal
execute at @s run particle minecraft:lava ~ ~1 ~ 0.3 0.5 0.3 0.2 20 normal
execute at @s run particle dust 0.561 0.133 0.024 1 ~ ~1 ~ 0.8 0.5 0.8 0.01 100 normal
execute at @s run particle flame ~ ~0.5 ~ 0.3 0.6 0.3 0.1 50 normal
execute at @s run particle falling_lava ~ ~1 ~ 2 0.7 0.3 0.7 20 normal
execute at @s run playsound entity.generic.explode master @a[distance=..40] ~ ~ ~ 0.1 1.1 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..40] ~ ~ ~ 0.2 0.5 0.2
execute at @s run playsound entity.generic.explode master @a[distance=..16] ~ ~ ~ 0.05 1.1 0.05
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..16] ~ ~ ~ 0.1 0.5 0.1
execute at @s run playsound entity.generic.explode master @a[distance=..9] ~ ~ ~ 0.05 1.1 0.05
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..9] ~ ~ ~ 0.1 0.5 0.1

execute at @s run execute as @e[tag=r4r_ability_target,distance=..4] run function skills:sword/trinity_fire_hit

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_2x1

tag @a remove trinity_attack
kill @s