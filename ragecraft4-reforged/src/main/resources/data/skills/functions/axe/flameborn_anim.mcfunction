scoreboard players add @s cd 1

execute at @s run particle flame ^ ^ ^0.8 0 0 0 0.01 1 normal
execute at @s run particle smoke ^ ^ ^0.8 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~0.015 ~ ~8 ~

execute at @s[scores={cd=..150}] run function skills:axe/flameborn_anim
kill @s[scores={cd=150..}]