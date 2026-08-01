tag @s add spec_archer_done

execute as @s store result score @s arrow_damage run data get entity @s damage 2
execute as @s store result entity @s damage double 0.5 run scoreboard players add @s arrow_damage 1