scoreboard players add @s cd 1

execute at @s run particle flame ^ ^0.1 ^3 0 0 0 0.01 2 normal
execute at @s run particle smoke ^ ^0.1 ^2.3 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:potions/potion_10_anim
kill @s[scores={cd=120..}]