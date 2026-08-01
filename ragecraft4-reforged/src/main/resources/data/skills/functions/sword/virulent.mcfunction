scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run playsound entity.zombie.infect master @a[distance=..12] ~ ~ ~ 0.7 0.8 0.7
execute at @s run execute as @e[tag=r4r_ability_target,nbt={HurtTime:10s},distance=..6] at @s run execute as @e[tag=r4r_ability_target,distance=..4] run function skills:sword/virulent_hit