execute at @s run particle block blue_ice ~ ~1 ~ 0.8 1.2 0.8 0.4 50 force
execute at @s run particle snowflake ~ ~1 ~ 0.8 1.5 0.8 0.5 15 force
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.3 0.7 0.3

tag @s add ice_shield_broken