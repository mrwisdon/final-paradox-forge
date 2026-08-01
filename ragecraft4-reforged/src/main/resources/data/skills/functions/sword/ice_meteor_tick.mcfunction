scoreboard players add @s cd 1

execute at @s run particle snowflake ~ ~0.5 ~ 0.2 0.2 0.2 0.1 5 normal
execute at @s run particle dust 0.745 0.855 1 1 ~ ~0.5 ~ 0.2 0.2 0.2 0.2 10 normal

execute positioned ~ ~-0.3 ~ run tp @s ~ ~ ~

execute at @s[scores={cd=25..}] run function skills:spells/spell_9_trigger1
execute at @s unless entity @e[type=snowball,tag=ice_meteor,distance=..2] run execute at @s run function skills:sword/ice_meteor_trigger
execute at @s[scores={cd=8..}] unless block ~ ~0.5 ~ #minecraft:nonsolid_nosnow run execute at @s run function skills:sword/ice_meteor_trigger