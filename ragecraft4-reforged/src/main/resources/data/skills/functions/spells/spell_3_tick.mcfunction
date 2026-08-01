scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run data modify entity @s Rotation set from entity @p Rotation

execute at @s positioned ^ ^ ^-2 run particle block frosted_ice ~ ~0.2 ~ 0.2 0.2 0.2 0.05 5 force
execute at @s positioned ^ ^ ^-2 run particle snowflake ~ ~0.2 ~ 0.2 0.2 0.2 0.01 5 force
execute at @s positioned ^ ^ ^-2 run particle dust 0.702 0.929 1 1 ~ ~0.1 ~ 0.2 0.2 0.2 0.2 15 force

execute at @s if entity @e[tag=r4r_ability_target,distance=..1.5] run function skills:spells/spell_3_trigger
execute at @s[scores={cd=40..}] run function skills:spells/spell_3_trigger

execute at @s positioned ^ ^ ^1 unless block ~ ~0.2 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_3_trigger
execute at @s positioned ^ ^ ^1 if block ~ ~0.2 ~ #minecraft:nonsolid run tp @s ^ ^ ^
