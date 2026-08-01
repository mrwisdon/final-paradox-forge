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

tag @e[tag=r4r_ability_target] remove astral_throw
tag @e[tag=r4r_ability_target] remove astral_done
tag @e[tag=r4r_ability_target] remove astral_blades

kill @e[type=armor_stand,tag=astral]
execute at @s run summon armor_stand ^-0.7 ^0.3 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:1b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","astral","astral_1"],Pose:{RightArm:[80f,0f,0f]}}
execute at @s run summon armor_stand ^ ^0.3 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:1b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","astral","astral_2"],Pose:{RightArm:[80f,0f,0f]}}
execute at @s run summon armor_stand ^0.7 ^0.3 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:1b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","astral","astral_3"],Pose:{RightArm:[80f,0f,0f]}}

playsound minecraft:entity.illusioner.cast_spell master @s ~ ~ ~ 0.6 1.5 0.6
playsound item.trident.throw master @s ~ ~ ~ 0.4 0.8 0.2