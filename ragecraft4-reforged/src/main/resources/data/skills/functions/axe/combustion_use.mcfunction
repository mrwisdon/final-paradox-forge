data modify entity @s Fire set value 0
damage @s[tag=!no_target] 18 player_attack by @p[tag=combustion_attack]
tag @s add hurt

execute at @s run particle explosion ~ ~0.5 ~ 0.1 0.1 0.1 0.3 1 normal
execute at @s run particle lava ~ ~0.5 ~ 0.3 0.5 0.3 0.1 15 normal
execute at @s run particle flame ~ ~0.5 ~ 0.3 0.5 0.3 0.1 80 normal
execute at @s run particle dust 0.902 0.486 0.278 1 ~ ~0.5 ~ 0.8 0.8 0.8 0.01 100 normal

execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.15 1.3 0.15
execute at @s run playsound minecraft:entity.wither.shoot master @a[distance=..20] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.25 0.8 0.25
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound minecraft:entity.wither.shoot master @a[distance=..10] ~ ~ ~ 0.05 0.7 0.05
