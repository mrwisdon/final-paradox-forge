scoreboard players add @s cd 1

item replace entity @s[scores={cd=6}] weapon.mainhand with diamond_sword{CustomModelData:30} 1
execute as @s[scores={cd=1}] run function skills:sword/astral_texture_1
execute as @s[scores={cd=6}] run function skills:sword/astral_texture_2

execute as @s[scores={cd=6..}] store result score @s mob_animation run data get entity @s Pose.RightArm[0] 100
execute as @s[scores={cd=6..}] store result entity @s Pose.RightArm[0] float 0.01 run scoreboard players add @s mob_animation 5000
execute at @s[scores={cd=5..}] run tp ^ ^ ^0.5
execute at @s[scores={cd=7..}] run particle end_rod ^-0.8 ^1 ^-0.8 0.0 0.0 0.0 0.01 3 force
execute at @s[scores={cd=7..}] run particle dust 0.878 0.651 0.878 1 ^-0.8 ^1 ^-0.8 0.2 0.2 0.2 0.5 8 force

execute at @s[scores={cd=5..}] positioned ~ ~-0.5 ~ run tag @e[tag=r4r_ability_target,tag=!astral_done,distance=..2] add astral_blades

execute as @s[scores={cd=38..}] run function skills:sword/astral_end
execute at @s[scores={cd=5..}] unless block ~ ~1 ~ #minecraft:nonsolid run function skills:sword/astral_hitblock

execute as @e[tag=r4r_ability_target,tag=astral_blades,tag=!astral_done] run function skills:sword/astral_hit

execute at @s run data modify entity @s[scores={cd=1}] Rotation set from entity @p Rotation
execute as @s[scores={cd=1}] at @s run tp ~ ~0.5 ~
execute at @s run tp @s[tag=astral_1,scores={cd=1}] ~ ~ ~ ~20 ~
execute at @s run tp @s[tag=astral_3,scores={cd=1}] ~ ~ ~ ~-20 ~