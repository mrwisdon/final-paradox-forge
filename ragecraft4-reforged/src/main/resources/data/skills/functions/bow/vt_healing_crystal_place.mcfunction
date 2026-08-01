kill @e[type=armor_stand,tag=vt_healing_crystal]
execute at @s run summon armor_stand ~ ~-0.5 ~ {Silent:1b,Invulnerable:1b,ShowArms:0b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","vt_healing_crystal"],ArmorItems:[{},{},{},{id:"minecraft:pink_concrete",Count:1b}]}

execute at @s run playsound block.end_portal_frame.fill master @a[distance=..15] ~ ~ ~ 0.4 1 0.4

execute at @s run particle crimson_spore ~ ~0.2 ~ 0.6 0.4 0.6 0.02 100 normal
execute at @s run particle portal ~ ~0.2 ~ 0.6 0.4 0.6 0.02 200 normal
execute at @s run particle dust 1.000 0.549 0.745 1 ~ ~0.2 ~ 0.8 0.6 0.8 0.1 120 normal

kill @s