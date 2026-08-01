execute at @s run playsound block.end_gateway.spawn master @a[distance=..15] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound entity.goat.screaming.ram_impact master @a[distance=..15] ~ ~ ~ 1 0.9 1
execute at @s run particle campfire_cosy_smoke ~ ~0.5 ~ 0.2 0.5 0.2 0.05 15 normal

execute at @s run summon marker ~ ~0.1 ~ {Tags:["eruption_anim"]}
execute as @e[type=marker,tag=eruption_anim] run function skills:sword/eruption_anim