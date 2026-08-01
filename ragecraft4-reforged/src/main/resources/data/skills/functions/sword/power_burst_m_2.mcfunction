execute at @s facing entity @e[tag=r4r_ability_target,tag=!power_burst_target,distance=..7,limit=1,sort=nearest] feet run tp ^ ^ ^

scoreboard players remove @s power_burst_buffs 1

tag @s remove mob_found
scoreboard players set @s ray_range 0
execute at @s run function skills:sword/power_burst_m_ray