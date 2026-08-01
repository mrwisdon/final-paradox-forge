scoreboard players add @s cd 1

execute at @s[scores={cd=16..}] positioned ^ ^-0.25 ^ unless block ~ ~0.2 ~ #minecraft:nonsolid run function skills:axe/snowstorm_ground
execute at @s[scores={cd=16..}] positioned ^ ^-0.25 ^ if block ~ ~0.2 ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s[scores={cd=..15}] positioned ^ ^-0.25 ^ run tp @s ^ ^ ^

execute at @s run particle snowflake ~ ~0.5 ~ 0.15 0.15 0.15 0.01 8 force
execute at @s run particle end_rod ~ ~0.5 ~ 0.1 0.1 0.1 0.01 2 force

kill @s[scores={cd=50..}]