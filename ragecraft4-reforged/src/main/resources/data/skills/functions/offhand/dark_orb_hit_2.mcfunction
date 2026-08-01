tag @s add mob_tick
tag @s add dark_orb_damaged
scoreboard players set @s dark_orb_duration 25

damage @s 9 magic by @p[scores={dark_orb_duration=1..}]
tag @s add hurt

execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..12] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound block.frogspawn.place master @a[distance=..12] ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 0.6 0.8 0.6
execute at @s run particle reverse_portal ~ ~1 ~ 0.4 0.6 0.4 0.01 50 normal
execute at @s run particle dust 0.431 0.247 0.486 1 ~ ~1 ~ 0.4 0.6 0.4 0.1 80 normal