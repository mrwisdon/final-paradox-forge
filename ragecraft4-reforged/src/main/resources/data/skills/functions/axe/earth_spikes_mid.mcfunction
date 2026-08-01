execute if block ~ ~1 ~ #minecraft:nonsolid run fill ~ ~ ~ ~ ~1 ~ pointed_dripstone replace #minecraft:nonsolid

execute at @s run execute as @e[tag=r4r_ability_target,tag=!earth_spikes_hit,distance=..3] at @s run function skills:axe/earth_spikes_hit