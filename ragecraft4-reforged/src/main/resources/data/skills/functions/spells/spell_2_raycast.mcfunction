execute at @s run particle enchanted_hit ~ ~ ~ 0.3 0.3 0.3 0.1 12 normal
execute at @s run particle crit ~ ~ ~ 0.15 0.15 0.15 0.2 20 normal
execute at @s run particle dust 0.898 0.490 1.000 1 ~ ~ ~ 0.2 0.2 0.2 0.3 20 normal

scoreboard players add @s ray_range 1
execute at @s positioned ^ ^ ^0.5 if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s positioned ^ ^ ^0.5 unless block ~ ~ ~ #minecraft:nonsolid run tag @s add tp_position
tag @s[scores={ray_range=24..}] add tp_position

execute at @s[scores={ray_range=24..},tag=tp_position] run tp @p[tag=cast_spell_2] ~ ~ ~

execute at @s[scores={ray_range=24..},tag=tp_position] run function skills:spells/spell_2_tp

execute as @s[scores={ray_range=..23}] run function skills:spells/spell_2_raycast

tag @s remove cast_spell_2
kill @s[tag=tp_position]