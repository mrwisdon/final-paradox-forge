execute at @s run summon marker ~ ~ ~ {Silent:1b,Invulnerable:1b,Tags:["marker_tick","witchcraft_cloud"]}

execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 1 1.2 1
execute at @s run playsound entity.dolphin.splash master @a[distance=..18] ~ ~ ~ 0.4 0.9 0.4

execute at @s run particle dust 0.196 0.533 0.22 1 ~ ~1 ~ 0.5 0.7 0.5 0.1 200 normal
execute at @s run particle totem_of_undying ~ ~1 ~ 0.4 0.6 0.4 0.1 60 normal
execute at @s run particle splash ~ ~1 ~ 0.6 0.8 0.6 0.1 200 normal

tag @s add witchcraft_done

kill @s