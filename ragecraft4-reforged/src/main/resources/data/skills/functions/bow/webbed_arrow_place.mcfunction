execute at @s run particle spit ~ ~0.5 ~ 0.5 0.5 0.5 0.1 50 force

execute at @s run fill ~1 ~ ~ ~-1 ~ ~ cobweb keep
execute at @s run fill ~ ~1 ~ ~ ~-1 ~ cobweb keep
execute at @s run fill ~ ~ ~1 ~ ~ ~-1 cobweb keep

kill @s