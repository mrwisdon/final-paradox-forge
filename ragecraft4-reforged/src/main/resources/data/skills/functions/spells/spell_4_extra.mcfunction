execute at @s run tp @s ~ ~ ~ facing entity @p feet
execute at @s positioned ^ ^ ^0.4 run tp ^ ^ ^

execute at @s run particle dust 0.475 0.686 0.51 1 ~ ~0.8 ~ 0.05 0.05 0.05 0.5 6 force
execute at @s run particle happy_villager ~ ~0.8 ~ 0 0 0 0.05 1 force

execute at @s if entity @p[distance=..1] run kill @s