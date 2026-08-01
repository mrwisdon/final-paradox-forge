execute at @s[tag=!spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..3] run function skills:spells/spell_5_hit
execute at @s[tag=spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..3] run function skills:spells/spell_5_hit2

execute at @s run scoreboard players add @p[scores={last_spell=5,mana=..19}] mana 1