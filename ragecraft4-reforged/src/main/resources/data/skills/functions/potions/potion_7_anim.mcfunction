scoreboard players add @s cd 1

execute at @s run particle dust 0.282 0.129 0.353 1 ^ ^0.3 ^2.8 0 0 0 0.01 3 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:potions/potion_7_anim
kill @s[scores={cd=120..}]