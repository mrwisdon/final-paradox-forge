execute at @s run summon arrow ^ ^ ^ {crit:1b,Tags:["arrow_done","void_blast","volley","volley_2c"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","void_blast"]}]}
execute at @s run execute as @e[type=arrow,tag=void_blast,limit=1,sort=nearest] run data modify entity @s {} merge value {Color:458780}
#execute at @s run execute as @e[type=arrow,tag=void_blast,limit=1,sort=nearest] at @s run function skills:bow/void_blast_4

kill @s