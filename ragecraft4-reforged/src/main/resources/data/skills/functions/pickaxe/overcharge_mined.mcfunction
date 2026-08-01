scoreboard players set @s overcharge_time 8

execute at @s run playsound entity.puffer_fish.blow_out master @s ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound block.fire.extinguish master @s ~ ~ ~ 0.6 1.2 0.6

execute at @s run particle flash ^ ^1 ^2.5 0.1 0.1 0.1 0.5 1 normal
execute at @s run particle electric_spark ^ ^1 ^2.5 0.7 0.7 0.7 0.03 80 normal