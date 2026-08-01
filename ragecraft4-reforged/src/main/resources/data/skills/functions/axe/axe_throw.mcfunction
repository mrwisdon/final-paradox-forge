scoreboard players add @s chal_mana_spent 20

scoreboard players set @s delayed_att 55

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

tag @e[tag=r4r_ability_target] remove axe_throw
tag @e[tag=r4r_ability_target] remove axe_throw_done

kill @e[type=armor_stand,tag=axe_throw]
execute at @s[nbt=!{ActiveEffects:[{Id:5}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw"],Pose:{RightArm:[50f,0f,0f]}}
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:0b}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw","strength_1"],Pose:{RightArm:[50f,0f,0f]}}
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:1b}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw","strength_2"],Pose:{RightArm:[50f,0f,0f]}}
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:2b}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw","strength_3"],Pose:{RightArm:[50f,0f,0f]}}
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:3b}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw","strength_4"],Pose:{RightArm:[50f,0f,0f]}}
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:4b}]}] run summon armor_stand ^ ^0.4 ^0.5 {NoGravity:1b,Silent:1b,ShowArms:0b,Small:0b,Marker:0b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","axe_throw","strength_5"],Pose:{RightArm:[50f,0f,0f]}}

playsound item.trident.throw master @s ~ ~ ~ 0.5 0.8 0.2