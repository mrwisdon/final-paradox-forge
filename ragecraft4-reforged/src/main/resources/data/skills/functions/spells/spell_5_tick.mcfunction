scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run function skills:spells/spell_5_start

execute at @s[scores={cd=2..}] positioned ^ ^ ^-0.5 run particle dust 0.341 0.522 0.22 1 ~ ~0.6 ~ 0.0 0.0 0.0 0.1 1 normal

execute at @s if entity @e[tag=r4r_ability_target,distance=..1] run function skills:spells/spell_5_trigger
execute at @s[scores={cd=50..}] run function skills:spells/spell_5_trigger

execute at @s positioned ^ ^ ^0.5 unless block ~ ~0.2 ~ #minecraft:nonsolid run execute at @s run function skills:spells/spell_5_trigger

execute at @s[tag=xx1] run function skills:spells/spell_5_tick_1
execute at @s[tag=xx2] run function skills:spells/spell_5_tick_2
execute at @s[tag=xx3] run function skills:spells/spell_5_tick_3
execute at @s[tag=xx4] run function skills:spells/spell_5_tick_4
execute at @s[tag=xx5] run function skills:spells/spell_5_tick_5
execute at @s[tag=xx6] run function skills:spells/spell_5_tick_6
execute at @s[tag=xx7] run function skills:spells/spell_5_tick_7