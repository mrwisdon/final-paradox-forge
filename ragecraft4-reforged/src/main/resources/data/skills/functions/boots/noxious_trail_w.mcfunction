scoreboard players add @s cd 1

execute at @s if block ~ ~-0.1 ~ #minecraft:nonsolid run tp ~ ~-0.1 ~
execute at @s run particle totem_of_undying ~ ~0.1 ~ 0.7 0.1 0.7 0.01 1 normal
execute at @s run effect give @e[tag=r4r_ability_target,distance=..1.5] wither 2 1 false

kill @s[scores={cd=90..}]