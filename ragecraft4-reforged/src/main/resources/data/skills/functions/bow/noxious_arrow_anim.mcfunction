scoreboard players add @s cd 1

execute at @s run particle dust 0.043 0.169 0.067 1 ^ ^ ^1.1 0.1 0.1 0.1 0.01 1 normal
execute at @s run particle totem_of_undying ^ ^ ^0.8 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:bow/noxious_arrow_anim
kill @s[scores={cd=120..}]