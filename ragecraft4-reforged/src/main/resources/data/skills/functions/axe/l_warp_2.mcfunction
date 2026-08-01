tag @e[tag=r4r_ability_target,distance=..2,limit=1,sort=nearest] add l_warp_target
execute if entity @e[tag=r4r_ability_target,tag=l_warp_target] run tag @s add l_warp_found
execute positioned ~ ~ ~ unless block ~ ~ ~ #minecraft:nonsolid run tag @a[tag=l_warp_cast] add l_warp_wall
execute positioned ~ ~0.5 ~ run tp @a[tag=l_warp_cast,tag=l_warp_found] ~ ~ ~
scoreboard players add @s l_warp_range 1