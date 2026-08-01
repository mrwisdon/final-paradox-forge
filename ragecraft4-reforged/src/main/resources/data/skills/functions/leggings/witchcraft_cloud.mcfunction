scoreboard players add @s cd 1

execute at @s run particle dust 0.282 0.702 0.314 1 ~ ~ ~ 2 0.4 2 0.1 7 normal
execute at @s run particle totem_of_undying ~ ~ ~ 1.5 0.4 1.5 0.1 5 normal

execute at @s if score #rc4tick tick20 matches 5 run function skills:leggings/witchcraft_debuff

kill @s[scores={cd=400..}]