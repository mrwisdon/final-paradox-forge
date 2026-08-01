execute as @s run function ragecraft4reforged:damage/damage2

execute at @s run particle block blue_ice ~ ~1 ~ 0.6 0.4 0.6 0.4 8 normal
execute at @s run particle snowflake ~ ~1 ~ 0.5 0.3 0.5 0.01 15 normal
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.1 0.7 0.1