execute at @s run particle electric_spark ~ ~0.3 ~ 0.3 0.3 0.3 0.05 15 force
execute at @s run particle end_rod ~ ~0.3 ~ 0.1 0.1 0.1 0.05 8 force
execute at @s run particle dust 0.949 0.973 0.827 1 ~ ~0.3 ~ 0.3 0.3 0.3 0.1 10 force

execute at @s run execute as @e[tag=r4r_ability_target,tag=!charged_arrow_hit,distance=..5] at @s run function skills:bow/charged_arrow_hit