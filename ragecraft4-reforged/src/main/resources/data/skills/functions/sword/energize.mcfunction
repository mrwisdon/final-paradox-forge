scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15

execute at @s run particle electric_spark ^ ^1 ^3 0.7 0.7 0.7 0.01 110 normal
execute at @s run particle dust 0.071 0.753 1.000 1 ^ ^1 ^3 0.5 0.5 0.5 0.01 80 normal
execute at @s run particle flash ^ ^1 ^3 0.1 0.1 0.1 0.1 1 normal

effect give @s haste 60 1 false
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..15] ~ ~ ~ 0.7 0.8 0.7
execute at @s run playsound block.fire.extinguish master @a[distance=..15] ~ ~ ~ 0.4 1.2 0.4
execute at @s run execute as @e[tag=r4r_ability_target,tag=elite,nbt={HurtTime:10s},tag=elite,distance=..6] run function skills:sword/energize_hit