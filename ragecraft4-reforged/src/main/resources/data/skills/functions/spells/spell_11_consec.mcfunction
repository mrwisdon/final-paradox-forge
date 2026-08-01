scoreboard players add @s cd 1

execute at @s run particle end_rod ~ ~ ~ 5 4 5 0.05 7 force

execute at @s run effect give @a[distance=..8] resistance 1 1 true

kill @s[scores={cd=400..}]