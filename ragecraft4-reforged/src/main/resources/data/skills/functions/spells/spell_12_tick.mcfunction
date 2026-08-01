scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run data modify entity @s Rotation set from entity @p Rotation

execute at @s positioned ^ ^ ^-1 run particle squid_ink ~ ~0.2 ~ 0.1 0.1 0.1 0.01 1 force
execute at @s positioned ^ ^ ^-1 run particle warped_spore ~ ~0.2 ~ 0.2 0.2 0.2 0.01 8 force
execute at @s positioned ^ ^ ^-1 run particle dust 0.004 0 0.078 1 ~ ~0.1 ~ 0.4 0.4 0.4 0.2 15 force

execute at @s positioned ~ ~-1 ~ if entity @e[tag=r4r_ability_target,distance=..1.5] run function skills:spells/spell_12_trigger
execute at @s[scores={cd=60..}] run function skills:spells/spell_12_trigger

execute at @s positioned ^ ^ ^0.4 unless block ~ ~0.2 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_12_trigger
execute at @s positioned ^ ^ ^0.4 if block ~ ~0.2 ~ #minecraft:nonsolid run tp @s ^ ^ ^
