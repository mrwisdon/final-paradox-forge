execute at @s run kill @e[type=snowball,tag=meteor,distance=..2]

execute at @s run particle explosion ~ ~0.5 ~ 0.3 0.3 0.3 1 1 normal
execute at @s run particle block ice ~ ~1 ~ 1 0.5 1 0.2 150 normal
execute at @s run particle dust 0.737 0.863 1 1 ~ ~1 ~ 1.2 0.5 1.2 0.01 400 normal
execute at @s run playsound entity.generic.explode master @a[distance=..22] ~ ~ ~ 0.15 1.1 0.15
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..22] ~ ~ ~ 0.2 0.5 0.2
execute at @s run playsound block.glass.break master @a[distance=..22] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound entity.glow_squid.death master @a[distance=..22] ~ ~ ~ 0.5 1.7 0.5
execute at @s run playsound entity.generic.explode master @a[distance=..9] ~ ~ ~ 0.1 1.1 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..9] ~ ~ ~ 0.1 0.5 0.1
execute at @s run playsound block.glass.break master @a[distance=..9] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound entity.glow_squid.death master @a[distance=..9] ~ ~ ~ 0.5 1.7 0.5

execute at @s positioned ~ ~1 ~ run execute as @e[tag=r4r_ability_target,distance=..4.5] run function skills:sword/ice_meteor_hit

#execute at @s run fill ~-2 ~0.5 ~-1 ~2 ~0.5 ~1 snow[layers=1] replace #minecraft:nonsolid_nosnow
#execute at @s run fill ~-1 ~0.5 ~-2 ~1 ~0.5 ~2 snow[layers=1] replace #minecraft:nonsolid_nosnow

execute at @s positioned ~ ~1.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~1.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~1.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~1.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~1.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~1.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~1.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~1.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~1.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~1.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~1.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~1.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~1.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~1.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~1.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~1.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~1.4 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~1.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~1.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~1.4 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~1.4 ~2 run function skills:sword/ice_meteor_snow

execute at @s positioned ~ ~0.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~0.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~0.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~0.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~0.4 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~0.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~0.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~0.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~0.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~0.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~0.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~0.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~0.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~0.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~0.4 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~0.4 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~0.4 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~0.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~0.4 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~0.4 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~0.4 ~2 run function skills:sword/ice_meteor_snow

execute at @s positioned ~ ~-0.6 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~-0.6 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~-0.6 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~-0.6 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~-0.6 ~ run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~-0.6 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~-0.6 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~-0.6 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~-0.6 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-2 ~-0.6 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~-0.6 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~-0.6 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~2 ~-0.6 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~-0.6 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~-0.6 ~-1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~-0.6 ~1 run function skills:sword/ice_meteor_snow
execute at @s positioned ~ ~-0.6 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~-0.6 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~-0.6 ~-2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~1 ~-0.6 ~2 run function skills:sword/ice_meteor_snow
execute at @s positioned ~-1 ~-0.6 ~2 run function skills:sword/ice_meteor_snow

tag @a remove ice_meteor_attack
kill @s