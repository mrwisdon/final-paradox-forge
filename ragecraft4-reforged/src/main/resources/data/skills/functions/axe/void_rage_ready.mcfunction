scoreboard players set @s void_rage_time 200
playsound minecraft:entity.phantom.bite master @s ~ ~ ~ 0.2 0.7 0.2
execute at @s run particle sculk_soul ~ ~ ~ 0.5 0.5 0.5 0.2 70 force
effect give @s instant_damage 1 1 true