scoreboard players add @s cd 1

execute at @s run particle dust 0.349 0.173 0.4 1 ^ ^ ^0.9 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:misc/nullification_anim
kill @s[scores={cd=120..}]