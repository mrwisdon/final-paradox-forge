tag @s add void_blast_base

execute at @s run playsound entity.squid.squirt master @p ~ ~ ~ 0.3 0.7 0.3

summon arrow ^ ^ ^ {crit:1b,pickup:1b,Tags:["arrow_done","void_blast"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","void_blast"]}]}
execute as @e[type=arrow,tag=void_blast,tag=!arrow_copied] run function skills:bow/void_blast_3

kill @s