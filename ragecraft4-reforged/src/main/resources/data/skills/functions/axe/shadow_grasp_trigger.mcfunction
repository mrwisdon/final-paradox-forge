execute positioned ^ ^1 ^3 run tp @e[tag=r4r_ability_target,tag=shadow_target,tag=!no_shadow_grasp] ~ ~ ~
execute as @e[tag=r4r_ability_target,tag=shadow_target] run function skills:axe/shadow_grasp_hit

execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..15] ~ ~ ~ 0.4 0.8 0.2