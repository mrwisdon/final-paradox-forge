scoreboard players add @s cd 1

execute as @s[scores={cd=1}] run function skills:axe/axe_throw_texture_1
execute as @s[scores={cd=6}] run function skills:axe/axe_throw_texture_2

execute as @s[scores={cd=6..}] store result score @s mob_animation run data get entity @s Pose.RightArm[0] 100
execute as @s[scores={cd=6..}] store result entity @s Pose.RightArm[0] float 0.01 run scoreboard players add @s mob_animation 5000
execute at @s[scores={cd=5..}] run tp ^ ^ ^0.4
execute at @s[scores={cd=10..}] run particle campfire_cosy_smoke ^-0.3 ^1.3 ^-0.8 0.2 0.2 0.2 0.01 1 force
execute at @s[scores={cd=10..}] run particle dust 0.655 0 0 1 ^-0.3 ^1.3 ^-0.8 0.3 0.3 0.3 0.5 3 force
execute at @s[scores={cd=10..}] run particle portal ^-0.3 ^1.1 ^-0.8 0.2 0.2 0.2 0.01 8 force

execute at @s[scores={cd=5..}] positioned ~ ~-0.5 ~ run tag @e[tag=r4r_ability_target,tag=!vt_axe_throw_done,distance=..1.5] add vt_axe_throw

execute as @s[scores={cd=50..}] run function skills:axe/axe_throw_end
execute at @s[scores={cd=5..}] unless block ~ ~1 ~ #minecraft:nonsolid run function skills:axe/vt_axe_throw_hitblock

execute as @e[tag=r4r_ability_target,tag=vt_axe_throw,tag=!vt_axe_throw_done] run function skills:axe/vt_axe_throw_hit

execute at @s run data modify entity @s[scores={cd=1..2}] Rotation set from entity @p Rotation