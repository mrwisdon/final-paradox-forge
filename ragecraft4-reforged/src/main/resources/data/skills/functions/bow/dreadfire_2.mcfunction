tag @s add dreadfire_base

execute at @s run playsound minecraft:entity.blaze.shoot master @p ~ ~ ~ 0.4 0.6 0.4

execute at @s run summon arrow ^ ^ ^ {Fire:300,pickup:1b,crit:1b,Tags:["arrow_done","dreadfire"],Passengers:[{id:"minecraft:armor_stand",Silent:1b,Marker:1b,Invisible:1b,Small:1b,Invulnerable:1b,Tags:["marker_tick","dreadfire"]}]}
execute at @s if entity @p[predicate=skills:trueshot,distance=..4] run data modify entity @e[type=minecraft:arrow,tag=!trueshot_done,tag=dreadfire,limit=1,sort=nearest] NoGravity set value 1b

execute as @e[type=arrow,tag=dreadfire] run function skills:bow/dreadfire_3

kill @s