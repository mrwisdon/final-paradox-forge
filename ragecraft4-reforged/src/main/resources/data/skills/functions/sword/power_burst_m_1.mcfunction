execute at @s unless entity @e[tag=r4r_ability_target,tag=!power_burst_target,distance=..7] run tag @s add power_burst_end
execute at @s if entity @e[tag=r4r_ability_target,tag=!power_burst_target,distance=..7] run function skills:sword/power_burst_m_2

scoreboard players set @s cd 0

execute at @s[tag=power_burst_end] run function skills:sword/power_burst_end