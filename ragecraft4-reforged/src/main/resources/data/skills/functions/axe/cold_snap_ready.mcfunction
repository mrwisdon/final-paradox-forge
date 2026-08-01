scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20

effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

scoreboard players set @s[predicate=skills:cold_snap] cold_snap_ready 200
playsound entity.zombie.infect master @s ~ ~ ~ 0.5 1 0.5
execute at @s run particle electric_spark ^ ^1 ^2 0.3 0.3 0.3 0.2 12 normal