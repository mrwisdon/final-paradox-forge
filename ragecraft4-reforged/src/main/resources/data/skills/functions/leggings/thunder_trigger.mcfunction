execute at @s run execute as @e[tag=r4r_ability_target,distance=..5] run function skills:leggings/thunder_hit

playsound entity.glow_squid.death master @a[distance=..20] ~ ~ ~ 0.5 1.4 0.5
playsound entity.puffer_fish.blow_out master @a[distance=..20] ~ ~ ~ 0.3 0.8 0.3
playsound entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.5 1.3 0.5
playsound entity.glow_squid.death master @a[distance=..10] ~ ~ ~ 0.3 1.4 0.3
playsound entity.puffer_fish.blow_out master @a[distance=..10] ~ ~ ~ 0.2 0.8 0.2
playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.3 1.3 0.3

particle end_rod ~ ~2 ~ 0.0 2.5 0.0 0.01 120 normal
particle dust 0.969 0.820 1.000 1 ~ ~2 ~ 0.0 2.5 0.0 0.01 250 normal
particle electric_spark ~ ~2 ~ 0.4 2.5 0.4 0.01 350 normal
particle electric_spark ~ ~ ~ 2 0.6 2 0.01 600 normal
particle dust 0.969 0.820 1.000 1 ~ ~ ~ 1.5 0.5 1.5 0.1 350 normal
particle cloud ~ ~ ~ 1.6 0.5 1.6 0.01 300 normal
particle flash ~ ~ ~ 0.0 0.0 0.0 0.01 1 normal
particle campfire_cosy_smoke ~ ~ ~ 0.9 0.4 0.9 0.01 60 normal

tag @s add thunder_done

kill @s