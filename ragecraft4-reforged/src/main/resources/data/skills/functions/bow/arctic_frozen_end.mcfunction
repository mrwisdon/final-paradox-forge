execute at @s run particle block blue_ice ~ ~1.5 ~ 0.8 0.8 0.8 0.4 50 force
execute at @s run particle explosion ~ ~1.5 ~ 0.2 0.2 0.2 0.5 1 force
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.3 0.7 0.2

kill @s[type=armor_stand]