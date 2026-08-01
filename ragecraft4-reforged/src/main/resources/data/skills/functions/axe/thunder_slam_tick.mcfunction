scoreboard players add @s cd 1

execute at @s run data modify entity @s[scores={cd=1}] Rotation set from entity @p[predicate=skills:thunder_slam] Rotation
execute at @s run tp @s[tag=thunder_slam_1,scores={cd=1}] ~ ~ ~ ~30 ~
execute at @s run tp @s[tag=thunder_slam_3,scores={cd=1}] ~ ~ ~ ~-30 ~

execute as @s[scores={cd=6}] run function skills:axe/thunder_slam_texture

execute as @s[scores={cd=6..}] store result score @s mob_animation run data get entity @s Pose.RightArm[0] 100
execute as @s[scores={cd=6..}] store result entity @s Pose.RightArm[0] float 0.01 run scoreboard players add @s mob_animation 1600

execute at @s[scores={cd=14}] run function skills:axe/thunder_slam_trigger
execute at @s[scores={cd=15..}] run function skills:axe/thunder_slam_end