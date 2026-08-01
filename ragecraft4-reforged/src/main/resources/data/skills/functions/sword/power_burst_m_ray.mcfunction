scoreboard players add @s ray_range 1

execute at @s run particle dust 0.796 0.671 0.941 1 ~ ~0.9 ~ 0.05 0.05 0.05 0.1 10 normal
execute at @s run particle wax_off ~ ~0.9 ~ 0.05 0.05 0.05 0.01 10 normal

execute at @s[scores={ray_range=..40},tag=!mob_found] positioned ^ ^ ^0.2 run tp @s ^ ^ ^
execute at @s[scores={ray_range=..40}] positioned ^ ^ ^0.2 if entity @e[tag=r4r_ability_target,tag=!power_burst_target,distance=..0.3] run tag @s add mob_found
execute at @s[tag=mob_found] run function skills:sword/power_burst_m_3
execute as @s[scores={ray_range=..39},tag=!mob_found] run function skills:sword/power_burst_m_ray