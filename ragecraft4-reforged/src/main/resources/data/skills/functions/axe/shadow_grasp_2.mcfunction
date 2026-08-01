tag @e[tag=r4r_ability_target,tag=!boss,distance=..2,limit=1,sort=nearest] add shadow_target
execute if entity @e[tag=r4r_ability_target,tag=!boss,tag=shadow_target] run tag @s add shadow_found
execute positioned ~ ~ ~ unless block ~ ~ ~ #minecraft:nonsolid run tag @a[tag=shadow_cast] add shadow_wall
execute positioned ~ ~ ~ run particle smoke ~ ~ ~ 0.2 0.2 0.2 0.1 6 force
execute positioned ~ ~ ~ run particle dust 0.094 0 0.286 1 ~ ~ ~ 0.2 0.2 0.2 0.5 10 force