scoreboard players remove @s acid_spray_cd 1

execute at @s run playsound block.soul_sand.fall master @a[distance=..15] ~ ~ ~ 0.5 0.6 0.5

execute at @s[predicate=!skills:spell_power] positioned ^ ^0.5 ^2 run function skills:spells/spell_8_near
execute at @s[predicate=!skills:spell_power] positioned ^ ^0.5 ^4 run function skills:spells/spell_8_near
execute at @s[predicate=!skills:spell_power] positioned ^ ^0.5 ^6 run function skills:spells/spell_8_far

execute at @s[predicate=skills:spell_power] positioned ^ ^0.5 ^2 run function skills:spells/spell_8_near_2
execute at @s[predicate=skills:spell_power] positioned ^ ^0.5 ^4 run function skills:spells/spell_8_near_2
execute at @s[predicate=skills:spell_power] positioned ^ ^0.5 ^6 run function skills:spells/spell_8_far_2

scoreboard players add @s[tag=acid_spray_mana,scores={mana=..19}] mana 1
tag @s remove acid_spray_mana

execute as @s[predicate=!skills:spell_8] run function skills:spells/spell_8_end
execute as @s[scores={acid_spray_cd=0}] run function skills:spells/spell_8_end