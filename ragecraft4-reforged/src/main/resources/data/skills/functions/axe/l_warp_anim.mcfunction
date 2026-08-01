scoreboard players add @s cd 1

execute at @s run particle dust 1 0.992 0.875 1 ^ ^ ^4 0.1 0.1 0.1 0.01 3 normal
execute at @s run particle wax_off ^ ^ ^3 0 0 0 0.01 2 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:axe/l_warp_anim
kill @s[scores={cd=120..}]