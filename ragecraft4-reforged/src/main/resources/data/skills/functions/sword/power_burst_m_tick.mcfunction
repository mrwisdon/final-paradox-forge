scoreboard players add @s cd 1

execute as @s[scores={power_burst_buffs=..0}] run function skills:sword/power_burst_end

execute at @s[scores={cd=3}] run function skills:sword/power_burst_m_1