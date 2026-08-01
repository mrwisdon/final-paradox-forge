execute at @s run particle dust 0.345 0.486 0.247 1 ~ ~1 ~ 0.2 0.2 0.2 0.01 8 normal
execute at @s run particle totem_of_undying ~ ~1 ~ 0.1 0.1 0.1 0.01 2 normal

execute at @s positioned ~ ~ ~ run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..2.5] run function skills:offhand/blight_orb_hit
execute at @s unless entity @p[predicate=skills:blight_orb,distance=..12] run function skills:offhand/blight_orb_end_2