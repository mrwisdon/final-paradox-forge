scoreboard players add @s cd 1

execute at @s run execute as @e[tag=r4r_ability_target,tag=!trinity_wind_hit,distance=..4] run function skills:sword/trinity_wind_hit

execute at @s positioned ^ ^ ^0.5 unless block ~ ~0.8 ~ #minecraft:nonsolid run tag @s add trinity_done
execute at @s positioned ^ ^ ^0.5 if block ~ ~0.8 ~ #minecraft:nonsolid run tp @s ^ ^ ^

execute at @s[tag=!trinity_wind_done] run particle electric_spark ^ ^ ^ 1.6 1 1.6 0.01 25 normal
execute at @s[tag=!trinity_wind_done] run particle sweep_attack ^ ^ ^ 1.6 1 1.6 0.05 6 normal
execute at @s[tag=!trinity_wind_done] run particle cloud ^ ^ ^ 1.2 0.8 1.2 0.03 12 normal

execute if entity @s[scores={cd=20..}] run function skills:sword/trinity_wind_end