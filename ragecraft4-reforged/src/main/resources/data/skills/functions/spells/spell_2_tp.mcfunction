execute at @s run tag @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] add hit_spell_2

execute at @s run particle flash ~ ~ ~ 0.4 0.4 0.4 0.5 3 force
execute at @s run particle enchanted_hit ~ ~1 ~ 1.5 1.5 1.5 0.1 50 force
execute at @s run particle dust 0.898 0.490 1.000 1 ~ ~1 ~ 1.5 1.5 1.5 0.3 60 force

execute as @e[tag=hit_spell_2] run function skills:spells/spell_2_hit