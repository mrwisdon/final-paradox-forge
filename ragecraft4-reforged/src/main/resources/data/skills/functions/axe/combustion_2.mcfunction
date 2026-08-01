scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

tag @s add combustion_attack
execute as @e[tag=r4r_ability_target,predicate=mobs:is_on_fire,tag=!no_target,distance=..6] run function skills:axe/combustion_use
tag @s remove combustion_attack