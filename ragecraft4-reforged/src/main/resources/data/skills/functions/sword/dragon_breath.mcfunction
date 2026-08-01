scoreboard players add @s chal_mana_spent 20

scoreboard players set @s delayed_att 40

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

tag @s add dragon_breath_att

execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..20] ~ ~ ~ 0.6 0.7 0.6
execute at @s run playsound entity.ender_dragon.growl master @a[distance=..20] ~ ~ ~ 0.1 1.2 0.1

execute at @s run kill @e[type=marker,tag=dragonb_2,distance=..4]

kill @e[type=marker,tag=dragonb_1]

#execute at @s run summon marker ^ ^1.8 ^ {NoGravity:1b,Tags:["dragonb_1"]}
execute at @s run summon marker ~ ~ ~ {NoGravity:1b,Tags:["dragonb_1"]}
execute at @s run summon marker ^ ^1.8 ^2 {NoGravity:1b,Tags:["marker_tick","dragonb_2"]}

execute at @s run execute as @e[type=marker,tag=dragonb_2,distance=..4] run function skills:sword/dragon_breath_2

tag @s remove dragon_breath_att