execute at @s run execute as @e[tag=r4r_ability_target,distance=..5] run function skills:bow/exp_trap_hit

execute at @s run particle campfire_cosy_smoke ~ ~1 ~ 1.2 0.8 1.2 0.2 30 force
execute at @s run particle explosion ~ ~1 ~ 1 0.8 1 0.5 5 force
execute at @s run particle lava ~ ~1 ~ 1 0.8 1 0.5 50 force
execute at @s run particle flame ~ ~1 ~ 1 0.8 1 0.5 300 force
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..30] ~ ~ ~ 0.45 1.2 0.45
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..18] ~ ~ ~ 0.25 1.2 0.25
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..10] ~ ~ ~ 0.2 1.2 0.2

kill @s