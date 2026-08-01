scoreboard players set @s charged_arrow_time 6
effect give @s slowness 3 6 false
tag @s add charged_arrow_hit
execute at @s run particle electric_spark ~ ~1 ~ 0.3 0.6 0.3 0.05 60 force
execute at @s run particle dust 0.976 0.89 1 1 ~ ~0.3 ~ 0.3 0.6 0.3 0.1 30 force