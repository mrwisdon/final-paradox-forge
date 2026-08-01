execute at @s run playsound minecraft:entity.wither.hurt master @a[distance=..12] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound entity.zombie.infect master @a[distance=..12] ~ ~ ~ 1 1 1
execute at @s run particle lava ~ ~1 ~ 0.4 0.6 0.4 0.03 12 normal
execute at @s run particle flame ~ ~1 ~ 0.4 0.6 0.4 0.03 30 normal

execute as @s run function skills:misc/setonfire_7s

execute at @s run summon marker ~ ~ ~ {Tags:["flameborn_anim"]}
execute as @e[type=marker,tag=flameborn_anim] run function skills:axe/flameborn_anim

execute at @s run particle falling_lava ~ ~1 ~ 0.3 0.3 0.3 1 8 normal