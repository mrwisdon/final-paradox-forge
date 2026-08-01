scoreboard players set @s[predicate=skills:spell_echo] spell_echo_cd 0
scoreboard players set @s arcane_suprem 1
execute at @s[predicate=skills:call_void] run function skills:offhand/call_void_spell
effect give @s[predicate=skills:spell_shield] absorption 12 0 true
effect give @s[predicate=skills:spell_shield] regeneration 12 0 true
