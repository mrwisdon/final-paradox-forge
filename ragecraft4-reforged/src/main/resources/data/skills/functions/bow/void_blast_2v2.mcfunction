tag @s add void_blast_base

execute at @s run playsound entity.squid.squirt master @p ~ ~ ~ 0.3 0.7 0.3

execute at @s run summon arrow ^ ^ ^ {pickup:1b,Tags:["arrow_done","void_blast","volley"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","void_blast"]}]}
execute at @s run scoreboard players set @e[type=minecraft:arrow,tag=!volley_done,tag=void_blast,nbt={inGround:0b},distance=..3] volley_2_delay 1
execute at @s if entity @p[predicate=skills:trueshot,distance=..4] run tag @e[type=minecraft:arrow,tag=!volley_done,tag=void_blast,nbt={inGround:0b},distance=..3] add trueshot
execute as @e[type=arrow,tag=void_blast] at @s run function skills:bow/void_blast_3

kill @s