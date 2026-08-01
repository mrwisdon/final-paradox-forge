scoreboard players add @p[predicate=skills:charged_arrow,distance=..5] chal_mana_spent 20

scoreboard players remove @p[predicate=skills:charged_arrow,distance=..5] mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

tag @s add charged_arrow
tag @s add arrow_tick
tag @s add arrow_done

execute at @s run playsound entity.squid.squirt master @p ~ ~ ~ 0.2 1.8 0.2

data modify entity @s pickup set value 0b