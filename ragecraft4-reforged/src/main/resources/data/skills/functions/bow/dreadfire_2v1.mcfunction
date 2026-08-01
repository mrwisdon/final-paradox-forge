tag @s add dreadfire_base

execute at @s run playsound minecraft:entity.blaze.shoot master @p ~ ~ ~ 0.4 0.6 0.4

execute at @s run summon arrow ^ ^ ^ {crit:1b,pickup:1b,Tags:["arrow_done","dreadfire","volley"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","dreadfire"]}]}
execute at @s run scoreboard players set @e[type=minecraft:arrow,tag=!volley_done,tag=dreadfire,nbt={inGround:0b},distance=..3] volley_1_delay 1
execute at @s if entity @p[predicate=skills:trueshot,distance=..4] run tag @e[type=minecraft:arrow,tag=!volley_done,tag=dreadfire,nbt={inGround:0b},distance=..3] add trueshot
execute as @e[type=arrow,tag=dreadfire] at @s run function skills:bow/dreadfire_3

kill @s