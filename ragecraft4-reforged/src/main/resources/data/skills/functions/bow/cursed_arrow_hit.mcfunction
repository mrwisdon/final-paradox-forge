effect give @s ragecraft4reforged:vulnerability 12 0 true
effect give @s ragecraft4reforged:decay 12 1 true
execute at @s run particle warped_spore ~ ~1 ~ 0.2 0.4 0.2 0.01 100 force
execute at @s run particle smoke ~ ~1 ~ 0.2 0.4 0.2 0.01 30 force

execute at @s run summon marker ~ ~ ~ {Tags:["cursed_arrow_anim"]}
execute as @e[type=marker,tag=cursed_arrow_anim] run function skills:bow/cursed_arrow_anim
