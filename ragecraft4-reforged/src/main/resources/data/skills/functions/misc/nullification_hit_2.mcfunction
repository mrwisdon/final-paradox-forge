execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..25] ~ ~ ~ 0.2 0.6 0.2

execute at @s run summon marker ~ ~0.1 ~ {Tags:["nullification_anim"]}
execute as @e[type=marker,tag=nullification_anim] run function skills:misc/nullification_anim

execute at @s run particle dust 0.275 0.09 0.427 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 60 normal
execute at @s run particle squid_ink ~ ~0.7 ~ 0.3 0.5 0.3 0.01 4 normal