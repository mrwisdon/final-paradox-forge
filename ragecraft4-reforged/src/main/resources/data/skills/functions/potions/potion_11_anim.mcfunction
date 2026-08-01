scoreboard players add @s cd 1

execute at @s run particle end_rod ^ ^ ^3.5 0.0 0.0 0.0 0.01 1 normal
execute at @s positioned ~ ~1 ~ run particle end_rod ^ ^ ^-3.5 0.0 0.0 0.0 0.01 1 normal

execute at @s run tp @s ~ ~0.07 ~ ~8 ~

kill @s[scores={cd=120..}]