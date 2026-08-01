execute store result score @s[tag=!boss] helper_health run data get entity @s[tag=!boss] Health 100
execute store result score @s[tag=!boss] helper_health_2 run data get entity @s[tag=!boss] Health 20
execute as @s[tag=!boss] store result entity @s[tag=!boss] Health float 0.01 run scoreboard players operation @s[tag=!boss] helper_health -= @s[tag=!boss] helper_health_2