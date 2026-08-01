effect give @s ragecraft4reforged:vulnerability 5 0 false
effect give @s slowness 5 3 false
effect give @s weakness 5 3 false
execute at @s run particle flash ~ ~1 ~ 0.5 0.5 0.5 0.5 1 force
execute at @s run particle smoke ~ ~1 ~ 0.5 0.5 0.5 0.1 15 force
execute at @s run particle dust 0.094 0 0.286 1 ~ ~1 ~ 0.5 0.5 0.5 0.5 60 force
tag @s remove shadow_target

execute at @s run summon marker ~ ~-0.5 ~ {Tags:["shadow_grasp_anim"]}
execute as @e[type=marker,tag=shadow_grasp_anim] run function skills:axe/shadow_grasp_anim
