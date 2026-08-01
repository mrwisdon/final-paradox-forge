tag @s add arcane_suprem
tag @s add arrow_tick
tag @s add arrow_done

execute as @s store result score @s arrow_damage run data get entity @s damage 1
execute as @s store result entity @s damage double 1 run scoreboard players add @s arrow_damage 5

execute at @s run playsound entity.firework_rocket.launch master @p ~ ~ ~ 0.2 0.8 0.2

data modify entity @s pickup set value 0b