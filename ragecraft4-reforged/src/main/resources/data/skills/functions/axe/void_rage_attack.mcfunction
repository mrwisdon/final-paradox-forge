scoreboard players add @s chal_mana_spent 3

scoreboard players remove @s mana 3

playsound minecraft:entity.lightning_bolt.impact master @s ~ ~ ~ 0.2 1.2 0.2
playsound minecraft:entity.shulker_bullet.hit master @s ~ ~ ~ 0.6 0.8 0.6
playsound minecraft:entity.wither.hurt master @s ~ ~ ~ 0.2 0.8 0.2

execute at @s run particle dust 0.267 0.176 0.310 1 ^ ^1 ^3 0.8 0.5 0.8 0.1 200 normal
execute at @s run particle squid_ink ^ ^1 ^3 0.8 0.5 0.8 0.03 20 normal
execute at @s run particle squid_ink ^ ^1 ^2.5 0.3 0.3 0.3 0.03 15 normal

tag @s add void_rage_attack
execute at @s positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:axe/void_rage_hit
tag @s remove void_rage_attack