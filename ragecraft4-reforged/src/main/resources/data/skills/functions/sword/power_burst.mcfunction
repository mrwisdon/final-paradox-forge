execute at @s positioned ^ ^ ^3 if entity @e[tag=r4r_ability_target,nbt={HurtTime:10s},distance=..4] run tag @s add hurt_x
execute at @s positioned ^ ^ ^3 unless entity @e[tag=r4r_ability_target,nbt={HurtTime:10s},distance=..4] run function skills:sword/power_burst_2b
execute at @s[tag=hurt_x] positioned ^ ^ ^3 run function skills:sword/power_burst_2a
tag @s remove hurt_x