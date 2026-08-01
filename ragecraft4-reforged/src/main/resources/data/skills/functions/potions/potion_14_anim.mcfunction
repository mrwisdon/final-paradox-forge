scoreboard players add @s cd 1

execute at @s run particle dust 0.878 0.933 0.992 1 ^ ^0.2 ^2.7 0 0 0 0.01 3 normal
execute at @s run particle wax_off ^ ^0.2 ^1.3 0 0 0 1 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:potions/potion_14_anim
kill @s[scores={cd=120..}]