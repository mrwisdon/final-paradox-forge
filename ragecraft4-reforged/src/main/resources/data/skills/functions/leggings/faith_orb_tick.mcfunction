execute at @s run tp @s ~ ~ ~ ~10 ~

scoreboard players add @s cd 1

execute at @s positioned ~ ~-0.2 ~ if block ~ ~0.6 ~ #minecraft:nonsolid run tp @s ~ ~ ~
execute at @s if entity @p[distance=..1] run function skills:leggings/faith_orb_pickup

execute at @s run particle dust 1 0.973 0.612 1 ~ ~1 ~ 0.3 0.3 0.3 0.1 2 normal

kill @s[scores={cd=2400..}]