execute at @s run particle electric_spark ~ ~1 ~ 0.3 0.5 0.3 0.1 60 force
execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..18] ~ ~ ~ 0.5 0.8 0.5
execute at @s run playsound block.fire.extinguish master @a[distance=..18] ~ ~ ~ 0.4 1.2 0.4
tag @a remove unstable_charge