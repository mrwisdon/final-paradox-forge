execute as @s[predicate=skills:xin_blessing] run function skills:chest/xin_blessing
execute as @s[predicate=skills:leap_slam,scores={jump_time=18..}] run function skills:boots/leap_slam
effect clear @s[predicate=skills:xin_blessing] strength
scoreboard players set @s jump 0
scoreboard players set @s jump_time 0
