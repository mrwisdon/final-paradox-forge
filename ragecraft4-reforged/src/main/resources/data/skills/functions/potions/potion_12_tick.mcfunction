execute at @s run tp @s ~ ~ ~ facing entity @p feet
execute at @s positioned ^ ^ ^0.4 run tp ^ ^ ^

execute at @s run particle dust 0.522 0.42 0.655 1 ~ ~0.8 ~ 0.05 0.05 0.05 0.5 10 normal
execute at @s run particle wax_off ~ ~0.8 ~ 0 0 0 0.05 2 normal

execute at @s if entity @p[distance=..1] run kill @s