execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..12] ~ ~ ~ 0.2 0.6 0.2

execute at @s run summon marker ~ ~0.1 ~ {Tags:["an_curse_anim"]}
execute as @e[type=marker,tag=an_curse_anim] run function skills:offhand/an_curse_anim

execute at @s run particle dust 0.188 0.114 0.251 1 ~ ~0.7 ~ 0.4 0.7 0.4 0.1 60 normal
execute at @s run particle squid_ink ~ ~0.7 ~ 0.3 0.5 0.3 0.01 4 normal