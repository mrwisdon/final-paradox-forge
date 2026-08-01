scoreboard players set @s frostburn_debuff 0
effect clear @s slowness
effect give @s wither 1 1 true
execute as @s run function ragecraft4reforged:damage/damage10

execute at @s run particle block packed_ice ~ ~1 ~ 0.3 0.5 0.3 0.1 25 normal
execute at @s run particle white_ash ~ ~1 ~ 0.3 0.5 0.3 0.5 150 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..16] ~ ~ ~ 1 1.2 0.5
execute at @s run playsound entity.squid.squirt master @a[distance=..16] ~ ~ ~ 0.6 1.5 0.2
