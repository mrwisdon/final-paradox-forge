kill @e[type=armor_stand,tag=explosive_trap]
execute at @s run summon armor_stand ~ ~-0.7 ~ {Silent:1b,Invulnerable:1b,ShowArms:0b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","explosive_trap"],ArmorItems:[{},{},{},{id:"minecraft:tnt",Count:1b}]}

execute at @s run playsound block.end_portal_frame.fill master @p[distance=..15] ~ ~ ~ 0.3 1 0.1

kill @s