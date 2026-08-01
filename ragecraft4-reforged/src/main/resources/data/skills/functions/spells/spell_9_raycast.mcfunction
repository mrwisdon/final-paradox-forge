scoreboard players add @s ray_range 1

execute at @s[tag=spell_9_position,tag=!done] run particle flame ~ ~ ~ 0.4 0.1 0.4 0.01 50 force
tag @s[tag=spell_9_position,tag=!done] add done

execute at @s[tag=!spell_9_position] positioned ^ ^ ^1 if block ~ ~ ~ #minecraft:nonsolid run tp @s ^ ^ ^
execute at @s[tag=!spell_9_position] positioned ^ ^ ^1 unless block ~ ~ ~ #minecraft:nonsolid run tag @s add spell_9_position
execute as @s[scores={ray_range=..30}] run function skills:spells/spell_9_raycast