execute at @s[predicate=skills:spell_1] run function skills:spells/spell_1_cast
execute at @s[predicate=skills:spell_2] run function skills:spells/spell_2_cast
execute at @s[predicate=skills:spell_3] run function skills:spells/spell_3_cast
execute at @s[predicate=skills:spell_4] run function skills:spells/spell_4_cast
execute at @s[predicate=skills:spell_5] run function skills:spells/spell_5_cast
execute at @s[predicate=skills:spell_6] run function skills:spells/spell_6_cast
execute at @s[predicate=skills:spell_7] run function skills:spells/spell_7_cast
execute at @s[predicate=skills:spell_8] run function skills:spells/spell_8_cast
execute at @s[predicate=skills:spell_9] run function skills:spells/spell_9_cast
execute at @s[predicate=skills:spell_10] run function skills:spells/spell_10_cast
execute at @s[predicate=skills:spell_11] run function skills:spells/spell_11_cast
execute at @s[predicate=skills:spell_12] run function skills:spells/spell_12_cast
execute at @s[predicate=skills:spell_13] run function skills:spells/spell_13_cast
execute at @s[predicate=skills:spell_14] run function skills:spells/spell_14_cast

scoreboard players set @s[predicate=skills:spell_echo] spell_echo_cd 0
scoreboard players set @s arcane_suprem 1

execute at @s[predicate=skills:call_void] run function skills:offhand/call_void_spell
effect give @s[predicate=skills:spell_shield] absorption 12 0 true
effect give @s[predicate=skills:spell_shield] regeneration 12 0 true

execute as @s run function skills:spells/spell_empty