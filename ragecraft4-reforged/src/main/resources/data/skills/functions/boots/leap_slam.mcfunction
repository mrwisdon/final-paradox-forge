tag @s add leap_slam
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..12] ~ ~ ~ 0.2 1.4 0.1
execute at @s run particle campfire_cosy_smoke ~ ~0.5 ~ 0.8 0.3 0.8 0.01 10 normal
execute at @s run particle crit ~ ~0.5 ~ 1 0.5 1 0.2 150 normal
execute at @s run particle explosion ~ ~0.5 ~ 0.1 0.1 0.1 0.2 1 normal
execute as @e[tag=r4r_ability_target,distance=..4] run function skills:boots/leap_slam_hit
tag @s remove leap_slam