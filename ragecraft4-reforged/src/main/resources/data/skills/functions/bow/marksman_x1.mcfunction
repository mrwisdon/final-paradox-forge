execute as @s store result score @s arrow_damage run data get entity @s damage 10
execute as @s store result entity @s damage double 0.1 run scoreboard players add @s arrow_damage 5