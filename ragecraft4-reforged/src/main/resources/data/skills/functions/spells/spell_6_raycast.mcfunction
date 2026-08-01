scoreboard players add @s ray_range 1

execute at @s run particle crit ~ ~ ~ 0.6 0.4 0.6 0.1 40 normal
execute at @s run particle cloud ~ ~ ~ 0.7 0.5 0.7 0.1 20 normal
execute at @s run particle flash ~ ~ ~ 0.4 0.4 0.4 0.5 1 normal
execute at @s run particle enchanted_hit ~ ~ ~ 0.7 0.4 0.7 0.5 30 normal
execute at @s run particle sweep_attack ~ ~ ~ 0.9 0.6 0.9 0.05 5 normal

execute at @s positioned ^ ^ ^1 if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s[tag=!spell_power] run tag @e[tag=r4r_ability_target,distance=..3] add spell_6_target
execute at @s[tag=spell_power] run tag @e[tag=r4r_ability_target,distance=..3] add spell_6_target2
execute as @s[scores={ray_range=..4}] run function skills:spells/spell_6_raycast

kill @s[scores={ray_range=7..}]