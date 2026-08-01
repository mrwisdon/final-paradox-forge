execute at @s run particle block nether_wart_block ~ ~0.3 ~ 0.3 0.4 0.3 0.05 8 normal
execute at @s run particle damage_indicator ~ ~0.3 ~ 0.3 0.4 0.3 0.05 2 normal

scoreboard players remove @s slice_1 1

damage @s[tag=!no_target] 0.1 magic
execute as @s run function ragecraft4reforged:damage/damage1