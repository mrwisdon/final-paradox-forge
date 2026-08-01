scoreboard players set @s power_burst_buffs 0
execute at @s if entity @p[scores={bladestorm_time=1..},nbt={ActiveEffects:[{Id:3,Amplifier:0b}]}] run scoreboard players set @s power_burst_buffs 1
execute at @s if entity @p[scores={bladestorm_time=1..},nbt={ActiveEffects:[{Id:3,Amplifier:1b}]}] run scoreboard players set @s power_burst_buffs 2
execute at @s if entity @p[scores={bladestorm_time=1..},nbt={ActiveEffects:[{Id:3,Amplifier:2b}]}] run scoreboard players set @s power_burst_buffs 3
execute at @s[scores={power_burst_buffs=0}] positioned ^ ^ ^1 run execute as @e[tag=r4r_ability_target,tag=!invulnerable,distance=..4] run function skills:sword/bladestorm_hit_2h0
execute at @s[scores={power_burst_buffs=1}] positioned ^ ^ ^1 run execute as @e[tag=r4r_ability_target,tag=!invulnerable,distance=..4] run function skills:sword/bladestorm_hit_2h1
execute at @s[scores={power_burst_buffs=2}] positioned ^ ^ ^1 run execute as @e[tag=r4r_ability_target,tag=!invulnerable,distance=..4] run function skills:sword/bladestorm_hit_2h2
execute at @s[scores={power_burst_buffs=3}] positioned ^ ^ ^1 run execute as @e[tag=r4r_ability_target,tag=!invulnerable,distance=..4] run function skills:sword/bladestorm_hit_2h3
scoreboard players set @s power_burst_buffs 0

execute at @s if entity @p[predicate=skills:eternal_flame,distance=..3] positioned ^ ^ ^1 run execute as @e[tag=r4r_ability_target,distance=..4] run function skills:misc/setonfire_5s

execute at @s unless entity @p[scores={bladestorm_time=1..},distance=..25] run kill @s