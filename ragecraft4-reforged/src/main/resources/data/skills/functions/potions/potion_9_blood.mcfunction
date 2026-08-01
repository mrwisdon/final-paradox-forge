execute at @s run particle dust 0.600 0.012 0.012 1 ~ ~ ~ 0.1 0.1 0.1 0.01 2 normal

scoreboard players add @s cd 1
kill @s[scores={cd=80..}]