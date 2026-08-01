effect give @s absorption 10 1 true

scoreboard players set @s guardian_cd 0

playsound block.beacon.power_select master @s ~ ~ ~ 1 1.5 1
playsound minecraft:entity.shulker.shoot master @s ~ ~ ~ 0.7 0.8 0.7

execute at @s run particle dust 1.000 0.980 0.710 1 ~ ~1 ~ 1.2 0.7 1.2 0.6 100 force
execute at @s run particle flash ~ ~1 ~ 0.5 0.5 0.5 0.6 1 force
execute at @s run particle heart ~ ~1 ~ 1 0.7 1 0.6 40 force
execute at @s run particle end_rod ~ ~1 ~ 1 0.7 1 0.4 50 force

execute at @s run execute as @e[tag=r4r_ability_target,distance=..6] run function skills:chest/guardian_angel_hit