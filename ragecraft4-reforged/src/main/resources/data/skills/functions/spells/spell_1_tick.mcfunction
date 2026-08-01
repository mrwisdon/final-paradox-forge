scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run data modify entity @s Rotation set from entity @p Rotation

execute at @s positioned ^ ^ ^-2 run particle flame ~ ~0.2 ~ 0.2 0.2 0.2 0.01 5 force
execute at @s positioned ^ ^ ^-2 run particle smoke ~ ~0.2 ~ 0.1 0.1 0.1 0.01 5 force
execute at @s positioned ^ ^ ^-2 run particle falling_lava ~ ~0.2 ~ 0.2 0.2 0.2 0.1 5 force
execute at @s positioned ^ ^ ^-2 run particle dust 0.659 0.333 0.118 1 ~ ~0.1 ~ 0.2 0.2 0.2 0.2 15 force

execute at @s if entity @e[tag=r4r_ability_target,distance=..1.5] run function skills:spells/spell_1_trigger
execute at @s[scores={cd=40..}] run function skills:spells/spell_1_trigger

execute at @s positioned ^ ^ ^1 unless block ~ ~0.2 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_1_trigger
execute at @s positioned ^ ^ ^1 if block ~ ~0.2 ~ #minecraft:nonsolid run tp @s ^ ^ ^
