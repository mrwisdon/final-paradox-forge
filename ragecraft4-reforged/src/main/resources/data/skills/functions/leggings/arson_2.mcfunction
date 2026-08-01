tag @s add arson_attack

execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..20] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound minecraft:entity.shulker.shoot master @a[distance=..20] ~ ~ ~ 0.5 0.8 0.5

execute positioned ^ ^1.5 ^3 run function skills:leggings/arson_2a
execute positioned ^ ^1.5 ^5 run function skills:leggings/arson_2b
execute positioned ^ ^1.5 ^7 run function skills:leggings/arson_2c

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,distance=..4] run function skills:leggings/arson_hit

tag @s remove arson_attack