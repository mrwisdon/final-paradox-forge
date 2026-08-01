scoreboard players add @s ray_range 1
execute at @s positioned ^ ^ ^1 if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s[tag=!spell_power] run tag @e[tag=r4r_ability_target,distance=..4] add spell_4_target
execute at @s[tag=spell_power] run tag @e[tag=r4r_ability_target,distance=..4] add spell_4_target2
execute as @s[scores={ray_range=..11}] run function skills:spells/spell_4_raycast