kill @e[type=armor_stand,tag=power_crystal]
execute at @s run summon armor_stand ~ ~-0.5 ~ {Silent:1b,Invulnerable:1b,ShowArms:0b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","power_crystal"],ArmorItems:[{},{},{},{id:"minecraft:purple_concrete",Count:1b}]}

execute at @s run playsound block.end_portal_frame.fill master @p ~ ~ ~ 0.5 1 0.1

execute at @s run particle witch ~ ~0.2 ~ 0.6 0.4 0.6 0.01 100 normal
execute at @s run particle dust 0.62 0.243 0.6 1 ~ ~0.2 ~ 0.8 0.6 0.8 0.1 120 normal

kill @s