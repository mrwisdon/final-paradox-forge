tag @s add sharpshot
tag @s add sharpshot_2
tag @s add arrow_tick
tag @s add sharpshot_done

execute as @s store result score @s arrow_damage run data get entity @s damage 1
execute as @s store result entity @s damage double 1 run scoreboard players add @s arrow_damage 2