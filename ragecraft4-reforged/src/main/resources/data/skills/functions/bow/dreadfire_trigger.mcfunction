execute at @s run execute as @e[tag=r4r_ability_target,distance=..4.5] at @s run function skills:bow/dreadfire_hit

execute at @s run particle dust 0.133 0 0.039 1 ~ ~ ~ 1.2 0.7 1.2 0.3 120 force
execute at @s run particle lava ~ ~ ~ 1 0.5 1 0.1 15 force
execute at @s run particle falling_lava ~ ~-0.5 ~ 1.2 0.5 1.2 1 100 force
execute at @s run playsound block.redstone_torch.burnout master @a[distance=..15] ~ ~ ~ 0.4 0.7 0.4

kill @s