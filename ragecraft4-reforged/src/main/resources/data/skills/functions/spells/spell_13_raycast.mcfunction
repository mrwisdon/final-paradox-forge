execute at @s[tag=!spell_power] run function skills:spells/spell_13_damage
execute at @s[tag=spell_power] run function skills:spells/spell_13_damage_2

execute at @s run particle electric_spark ~ ~ ~ 0.3 0.3 0.3 0.1 12 normal
execute at @s run particle end_rod ~ ~0.7 ~ 0.15 0.15 0.15 0.01 5 normal
execute at @s run particle dust 0.804 0.588 0.859 1 ~ ~ ~ 0.15 0.15 0.15 0.3 20 normal

scoreboard players add @s ray_range 1
execute at @s positioned ^ ^ ^0.5 if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s positioned ^ ^ ^0.5 unless block ~ ~ ~ #minecraft:nonsolid run tag @s add tp_position
tag @s[scores={ray_range=24..}] add tp_position

execute at @s[scores={ray_range=24..},tag=tp_position] run tp @p[tag=cast_spell_13] ~ ~ ~

execute at @s[scores={ray_range=24..},tag=tp_position] run function skills:spells/spell_13_tp

execute as @s[scores={ray_range=..23}] run function skills:spells/spell_13_raycast

tag @s remove cast_spell_13
kill @s[tag=tp_position]