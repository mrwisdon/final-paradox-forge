effect give @s[predicate=skills:arcane_celerity,scores={mana=20..}] speed 1 0 true
effect give @s[predicate=skills:acrobatics] jump_boost 1 2 true
execute as @s[predicate=skills:ghost_form] run function skills:chest/ghost_form_tick
execute as @s[scores={sapping_potion_buff=1..}] run function skills:potions/potion_12_buff
execute as @s[predicate=skills:duality] run function skills:boots/duality
execute as @s[predicate=skills:assassination] run function skills:helmet/assassination
execute as @s[predicate=skills:blood_pact] run function skills:helmet/blood_pact_tick
scoreboard players add @s[scores={spell_echo_cd=..10}] spell_echo_cd 1
execute at @s[scores={spell_echo_cd=8}] run function skills:helmet/spell_echo_trigger
execute at @s[predicate=skills:headhunter] run function skills:helmet/headhunter
