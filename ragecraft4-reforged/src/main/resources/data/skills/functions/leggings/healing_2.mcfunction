execute at @s positioned ^ ^1 ^3.5 run summon armor_stand ~ ~2 ~ {Silent:1b,Invulnerable:1b,ShowArms:0b,Small:1b,Marker:1b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","healing_orb"],ArmorItems:[{},{},{},{id:"minecraft:nether_wart_block",Count:1b}]}
 
execute at @s positioned ^ ^1.5 ^3.5 run particle dust 0.545 0.075 0.255 1 ~ ~1 ~ 0.5 0.7 0.5 0.1 120 normal
execute at @s positioned ^ ^1.5 ^3.5 run particle heart ~ ~1 ~ 0.4 0.6 0.4 0.1 5 normal

execute at @s run playsound block.end_portal_frame.fill master @a[distance=..15] ~ ~ ~ 0.4 1 0.4
execute at @s run playsound block.beehive.enter master @a[distance=..15] ~ ~ ~ 0.6 1 0.6