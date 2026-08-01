scoreboard players add @s cd 1

execute at @s[scores={cd=1}] run particle flame ~ ~1.3 ~ 0.2 1 0.2 0.01 15 normal
execute at @s[scores={cd=1}] run particle smoke ~ ~1.3 ~ 0.2 1 0.2 0.01 30 normal

execute positioned ~ ~-0.25 ~ run tp @s ~ ~ ~

execute at @s run particle flame ~ ~0.2 ~ 0.2 0.2 0.2 0.01 3 normal
execute at @s run particle smoke ~ ~0.2 ~ 0.2 0.2 0.2 0.01 15 normal
execute at @s run particle falling_lava ~ ~0.2 ~ 0.2 0.2 0.2 0.1 5 normal
execute at @s run particle dust 0.659 0.333 0.118 1 ~ ~0.1 ~ 0.2 0.2 0.2 0.2 15 normal

execute at @s[scores={cd=50..}] run function skills:spells/spell_9_trigger1
execute at @s unless entity @e[type=snowball,tag=meteor,distance=..2] run execute at @s run function skills:spells/spell_9_trigger1
execute at @s[scores={cd=15..}] unless block ~ ~0.5 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_9_trigger1