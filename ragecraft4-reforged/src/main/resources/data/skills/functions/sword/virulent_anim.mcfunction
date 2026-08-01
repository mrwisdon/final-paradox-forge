scoreboard players add @s cd 1

execute at @s run particle dust 0.055 0.741 0.008 1 ^ ^ ^0.8 0 0 0 0.1 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:sword/virulent_anim
kill @s[scores={cd=120..}]