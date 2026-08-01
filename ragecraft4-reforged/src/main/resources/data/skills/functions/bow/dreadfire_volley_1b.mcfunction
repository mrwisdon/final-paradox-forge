execute at @s run summon arrow ^ ^ ^ {crit:1b,Tags:["arrow_done","dreadfire","volley","volley_1b"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","dreadfire"]}]}
execute at @s run execute as @e[type=arrow,tag=dreadfire,limit=1,sort=nearest] run data modify entity @s {} merge value {Color:4849684}

kill @s