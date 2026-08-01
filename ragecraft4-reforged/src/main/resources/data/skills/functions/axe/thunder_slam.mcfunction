scoreboard players add @s chal_mana_spent 10

scoreboard players remove @s mana 10

execute at @s run summon armor_stand ^-1.3 ^-0.3 ^2 {NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","thunder_slam_1","thunder_slam"],Pose:{RightArm:[220f,0f,0f]}}
execute at @s run summon armor_stand ^ ^-0.3 ^2 {NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","thunder_slam_2","thunder_slam"],Pose:{RightArm:[220f,0f,0f]}}
execute at @s run summon armor_stand ^1.3 ^-0.3 ^2 {NoGravity:1b,Silent:1b,Invulnerable:1b,Invisible:1b,Tags:["marker_tick","thunder_slam_3","thunder_slam"],Pose:{RightArm:[220f,0f,0f]}}

playsound item.trident.throw master @s ~ ~ ~ 0.15 0.6 0.15
playsound minecraft:entity.illusioner.mirror_move master @s ~ ~ ~ 0.7 0.6 0.7

tag @s add thunder_slam_attack