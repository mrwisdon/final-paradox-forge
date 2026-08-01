scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run data modify entity @s Rotation set from entity @p Rotation

execute at @s positioned ^ ^ ^-2 run particle end_rod ~ ~0.2 ~ 0.1 0.1 0.1 0.05 6 force
execute at @s positioned ^ ^ ^-2 run particle dust 1 0.941 0.6 1 ~ ~0.1 ~ 0.3 0.3 0.3 0.2 50 force

execute at @s if entity @e[tag=r4r_ability_target,distance=..1.5] run function skills:spells/spell_11_trigger
execute at @s[scores={cd=40..}] run function skills:spells/spell_11_trigger

execute at @s positioned ^ ^ ^1 unless block ~ ~0.2 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_11_trigger
execute at @s positioned ^ ^ ^1 if block ~ ~0.2 ~ #minecraft:nonsolid run tp @s ^ ^ ^
