execute at @s run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] at @s run function skills:bow/void_blast_hit

execute at @s run particle ash ~ ~ ~ 1 1 1 0.2 600 force
execute at @s run particle squid_ink ~ ~ ~ 0.9 0.6 0.9 0.2 50 force
execute at @s run particle explosion ~ ~ ~ 0.0 0.0 0.1 0.3 1 force
execute at @s run particle dust 0.016 0 0.094 1 ~ ~ ~ 1 0.7 1 0.3 450 force
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..40] ~ ~ ~ 0.6 0.6 0.6
execute at @s run playsound minecraft:entity.ender_dragon.hurt master @a[distance=..40] ~ ~ ~ 0.2 1.5 0.2
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..25] ~ ~ ~ 0.3 0.6 0.3
execute at @s run playsound minecraft:entity.ender_dragon.hurt master @a[distance=..25] ~ ~ ~ 0.2 1.5 0.2
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 0.3 0.6 0.3
execute at @s run playsound minecraft:entity.ender_dragon.hurt master @a[distance=..12] ~ ~ ~ 0.1 1.5 0.1

kill @s