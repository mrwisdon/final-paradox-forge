execute at @s run execute as @e[tag=r4r_ability_target,tag=!snowstorm_hit,distance=..3] run function skills:axe/snowstorm_hit

execute at @s run particle snowflake ~ ~ ~ 0.7 0.7 0.7 0.01 200 force
execute at @s run particle explosion ~ ~ ~ 0.1 0.1 0.1 0.01 1 force
execute at @s run particle block snow_block ~ ~ ~ 1 0.5 1 1 100 force
execute at @s run particle dust 0.710 0.816 1.000 1 ~ ~ ~ 0.5 0.5 0.5 0.01 200 force

execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..15] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..15] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 0.7 1.2 0.7

execute at @s run fill ~-2 ~0.5 ~-1 ~2 ~0.5 ~1 snow[layers=1] replace #minecraft:nonsolid_nosnow
execute at @s run fill ~-1 ~0.5 ~-2 ~1 ~0.5 ~2 snow[layers=1] replace #minecraft:nonsolid_nosnow
execute at @s run fill ~-2 ~-0.5 ~-1 ~2 ~-0.5 ~1 snow[layers=1] replace #minecraft:nonsolid_nosnow
execute at @s run fill ~-1 ~-0.5 ~-2 ~1 ~-0.5 ~2 snow[layers=1] replace #minecraft:nonsolid_nosnow

kill @s