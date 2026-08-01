execute at @s if block ~ ~ ~ #minecraft:nonsolid run tag @s add in_nonsolid
execute at @s unless block ~ ~ ~ #minecraft:nonsolid run tag @s add in_solid

tag @s add earth_spikes_pos