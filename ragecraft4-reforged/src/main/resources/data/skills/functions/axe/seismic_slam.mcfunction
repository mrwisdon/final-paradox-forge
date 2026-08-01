scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200

execute at @s run particle explosion ^ ^1 ^3 2 0.5 2 0.02 10 normal
execute at @s run particle crit ^ ^1 ^3 2 0.8 2 0.1 600 normal
execute at @s run particle campfire_cosy_smoke ^ ^1 ^3 1.5 0.5 1.5 0.03 60 normal
execute at @s run particle minecraft:wax_off ^ ^1 ^3 2.2 0.7 2.2 0.8 120 normal
execute at @s run playsound entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.2 1 0.2
execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..20] ~ ~ ~ 0.3 0.6 0.3
execute at @s run playsound minecraft:item.totem.use master @a[distance=..20] ~ ~ ~ 0.2 0.6 0.2
execute at @s run playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.1 1 0.1
execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..10] ~ ~ ~ 0.15 0.6 0.15
execute at @s run playsound minecraft:item.totem.use master @a[distance=..10] ~ ~ ~ 0.1 0.6 0.1

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..6] run function skills:axe/seismic_slam_hit