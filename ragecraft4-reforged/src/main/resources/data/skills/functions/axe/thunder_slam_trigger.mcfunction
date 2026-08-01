execute at @s positioned ^ ^0.5 ^1 run execute as @e[tag=r4r_ability_target,tag=!thunder_slam_hit,distance=..3.5] run function skills:axe/thunder_slam_hit

playsound entity.glow_squid.death master @a[distance=..20] ~ ~ ~ 0.2 1.4 0.2
playsound entity.puffer_fish.blow_out master @a[distance=..20] ~ ~ ~ 0.15 0.8 0.15
playsound entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.1 1.3 0.1
playsound entity.glow_squid.death master @a[distance=..10] ~ ~ ~ 0.1 1.4 0.1
playsound entity.puffer_fish.blow_out master @a[distance=..10] ~ ~ ~ 0.1 0.8 0.1
playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.05 1.3 0.05

execute at @s positioned ^ ^0.8 ^ run particle campfire_cosy_smoke ~ ~0.5 ~ 0.9 0.4 0.9 0.01 15 normal
execute at @s positioned ^ ^0.8 ^0.2 run particle electric_spark ~ ~0.5 ~ 1 0.4 1 0.01 350 normal
execute at @s positioned ^ ^0.8 ^0.2 run particle flash ~ ~0.5 ~ 0.0 0.0 0.0 0.01 1 normal
execute at @s positioned ^ ^0.8 ^0.2 run particle explosion ~ ~0.5 ~ 0.3 0.3 0.3 0.2 1 normal