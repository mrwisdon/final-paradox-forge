scoreboard players add @s cd 1

execute at @s run particle dust 0.71 0.804 0.945 1 ^ ^ ^3 0.1 0.1 0.1 0.01 5 normal
execute at @s run particle wax_off ^ ^ ^2.3 0 0 0 0.5 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:axe/cold_snap_anim
kill @s[scores={cd=120..}]