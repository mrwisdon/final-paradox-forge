scoreboard players add @s cd 1

execute at @s positioned ~ ~0.2 ~ run particle dust 0.086 0.043 0.153 1 ^ ^ ^0.9 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.9 ~ run particle dust 0.086 0.043 0.153 1 ^ ^ ^0.9 0 0 0 0.01 1 normal
execute at @s positioned ~ ~1.6 ~ run particle dust 0.086 0.043 0.153 1 ^ ^ ^0.9 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:bow/cursed_arrow_anim
kill @s[scores={cd=120..}]