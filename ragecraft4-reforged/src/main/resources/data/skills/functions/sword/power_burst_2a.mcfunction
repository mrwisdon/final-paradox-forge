scoreboard players add @s chal_mana_spent 20

tag @s add power_burst_attack

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

scoreboard players set @s power_burst_buffs 0
scoreboard players add @s[nbt={ActiveEffects:[{Id:1}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:3}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:5}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:8}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:10}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:11}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:12}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:13}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:14}]}] power_burst_buffs 1
scoreboard players add @s[nbt={ActiveEffects:[{Id:22}]}] power_burst_buffs 1

execute at @s positioned ^ ^ ^3 run execute at @e[tag=r4r_ability_target,nbt={HurtTime:10s},limit=1,sort=nearest] run function skills:sword/power_burst_3a

tag @s remove power_burst_attack
tag @s add power_burst_attack_2