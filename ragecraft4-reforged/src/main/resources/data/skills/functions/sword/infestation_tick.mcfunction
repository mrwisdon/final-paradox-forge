scoreboard players add @s cd 1

execute at @s[tag=fungus_placed] run particle totem_of_undying ~ ~ ~ 1.6 0.1 1.6 0.01 6 normal
execute at @s run particle crimson_spore ~ ~ ~ 0.3 0.1 0.3 0.01 5 normal
execute at @s run particle sneeze ~ ~ ~ 0.2 0.2 0.2 0.03 1 normal

execute at @s[tag=!fungus_placed] unless block ~ ~-0.1 ~ #minecraft:nonsolid run function skills:sword/infestation_place

execute at @s[tag=fungus_placed] if score #rc4tick tick10 matches 5 run function skills:sword/infestation_tick_10

execute at @s[scores={cd=400..}] run function skills:sword/infestation_boom
