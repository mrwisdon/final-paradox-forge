effect give @s[type=#minecraft:undead] instant_health 1 1 true
effect give @s[type=#minecraft:non_undead] instant_damage 1 1 true
tag @s remove charged_arrow_hit
execute at @s run particle electric_spark ~ ~1 ~ 0.4 0.7 0.4 0.05 100 force
execute at @s run particle dust 0.925 0.945 0.82 1 ~ ~0.3 ~ 0.4 0.7 0.4 0.1 120 force
execute at @s run particle crit ~ ~1 ~ 0.3 0.6 0.3 0.05 80 force

execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..25] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound block.fire.extinguish master @a[distance=..25] ~ ~ ~ 0.4 1.2 0.4
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..15] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound block.fire.extinguish master @a[distance=..15] ~ ~ ~ 0.3 1.2 0.3