execute at @s run playsound block.medium_amethyst_bud.break master @a[distance=..25] ~ ~ ~ 0.5 1 0.5
execute at @s run playsound block.medium_amethyst_bud.break master @a[distance=..12] ~ ~ ~ 0.3 1 0.3
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..25] ~ ~ ~ 0.4 1.2 0.4
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..12] ~ ~ ~ 0.2 1.2 0.2

execute at @s run particle explosion ~ ~1 ~ 0.1 0.1 0.1 0.1 1 force
execute at @s run particle dust 0.878 0.651 0.878 1 ~ ~1 ~ 0.8 0.8 0.8 0.5 200 force

kill @s