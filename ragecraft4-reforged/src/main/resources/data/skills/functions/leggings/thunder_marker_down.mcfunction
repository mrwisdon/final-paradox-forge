scoreboard players add @s cd 1

execute at @s positioned ~ ~-0.2 ~ if block ~ ~ ~ #minecraft:nonsolid run tp @s ~ ~ ~
execute at @s positioned ~ ~-0.2 ~ unless block ~ ~ ~ #minecraft:nonsolid run tag @s add on_ground

kill @s[scores={cd=50}]

execute at @s[tag=!on_ground] run function skills:leggings/thunder_marker_down
execute at @s[tag=on_ground,tag=!thunder_done] run function skills:leggings/thunder_trigger