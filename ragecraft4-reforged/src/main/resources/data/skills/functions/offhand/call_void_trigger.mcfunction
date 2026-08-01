execute at @s run particle dust 0.02 0.004 0.086 1 ~ ~1.2 ~ 0.7 0.7 0.7 0.01 200 force
execute at @s run particle squid_ink ~ ~1.2 ~ 0.2 0.2 0.2 0.1 20 force
execute at @s run playsound minecraft:item.totem.use master @a[distance=..30] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 0.1 0.5 0.1
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..30] ~ ~ ~ 0.1 1.4 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..20] ~ ~ ~ 0.05 0.8 0.05
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.1 0.5 0.1
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..20] ~ ~ ~ 0.05 1.4 0.05
execute at @s run playsound minecraft:item.totem.use master @a[distance=..10] ~ ~ ~ 0.05 0.8 0.05
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.1 0.5 0.1
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..10] ~ ~ ~ 0.05 1.4 0.05

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..3] run function skills:offhand/call_void_hit

kill @s