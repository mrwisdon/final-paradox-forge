execute at @s run particle block blue_ice ~ ~1.5 ~ 0.8 0.8 0.8 0.4 50 normal
execute at @s run particle explosion ~ ~1.5 ~ 0.2 0.2 0.2 0.5 1 normal
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.3 0.7 0.2
execute at @s run playsound entity.glow_squid.hurt master @a[distance=..15] ~ ~ ~ 0.3 1 0.3
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..15] ~ ~ ~ 1 0.8 1

kill @s[type=armor_stand]