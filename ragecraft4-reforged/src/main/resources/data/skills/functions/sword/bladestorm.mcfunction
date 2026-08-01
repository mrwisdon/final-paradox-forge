scoreboard players add @s chal_mana_spent 20

scoreboard players set @a bladestorm_rot 0
scoreboard players set @a bladestorm_time 0
scoreboard players set @s bladestorm_time 160

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run playsound entity.warden.attack_impact master @a[distance=..15] ~ ~ ~ 0.7 0.6 0.7
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..15] ~ ~ ~ 0.7 0.5 0.7
execute at @s run playsound entity.ravager.stunned master @a[distance=..15] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound minecraft:item.trident.riptide_1 master @a[distance=..15] ~ ~ ~ 0.7 0.6 0.7

kill @e[type=armor_stand,tag=bladestorm]
execute at @s run summon armor_stand ~-1 ~ ~ {Marker:1b,Small:0b,NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","bladestorm","bladestorm_1"],Pose:{RightArm:[350f,0f,0f]},Rotation:[90F,0F],HandItems:[]}
execute at @s run summon armor_stand ~1 ~ ~ {Marker:1b,Small:0b,NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","bladestorm","bladestorm_2"],Pose:{RightArm:[350f,0f,0f]},Rotation:[-90F,0F],HandItems:[]}
execute at @s run summon armor_stand ~ ~ ~-1 {Marker:1b,Small:0b,NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","bladestorm","bladestorm_3"],Pose:{RightArm:[350f,0f,0f]},Rotation:[180F,0F],HandItems:[]}
execute at @s run summon armor_stand ~ ~ ~1 {Marker:1b,Small:0b,NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","bladestorm","bladestorm_4"],Pose:{RightArm:[350f,0f,0f]},Rotation:[0F,0F],HandItems:[]}
execute as @e[type=armor_stand,tag=bladestorm] run function skills:sword/bladestorm_texture