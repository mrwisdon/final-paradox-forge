scoreboard players add @s chal_mana_spent 20

tag @s add slash_attack

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run particle sweep_attack ^ ^1.5 ^2.5 1.7 1 1.7 0.8 25 normal
execute at @s run particle block nether_wart_block ^ ^1.5 ^3 2 1 2 1 150 normal
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..10] ~ ~ ~ 0.6 0.7 0.6
execute at @s run playsound entity.zombie.infect master @a[distance=..10] ~ ~ ~ 0.5 1.5 0.5
execute at @s positioned ^ ^0.5 ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4.2] run function skills:sword/slash_hit

tag @s remove slash_attack