tag @s add mob_tick
tag @s add blight_orb_damaged
scoreboard players set @s blight_orb_duration 25

damage @s 5 magic by @p[scores={blight_orb_duration=1..}]
tag @s add hurt

execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..12] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound block.frogspawn.place master @a[distance=..12] ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..12] ~ ~ ~ 0.6 0.8 0.6
execute at @s run particle sneeze ~ ~1 ~ 0.4 0.6 0.4 0.01 30 normal
execute at @s run particle dust 0.337 0.459 0.255 1 ~ ~1 ~ 0.4 0.6 0.4 0.1 80 normal