execute at @s run particle dust 0.431 0.247 0.486 1 ~ ~1 ~ 0.2 0.2 0.2 0.01 8 normal
execute at @s run particle witch ~ ~1 ~ 0.1 0.1 0.1 0.01 1 normal
execute at @s run particle squid_ink ~ ~1 ~ 0 0 0 0.01 1 normal

execute at @s positioned ~ ~ ~ run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..3] run function skills:offhand/dark_orb_hit
execute at @s unless entity @p[predicate=skills:vt_dark_orb,distance=..12] run function skills:offhand/dark_orb_end_2