scoreboard players add @s cd 1

execute at @s if block ~ ~-0.1 ~ #minecraft:nonsolid run tp ~ ~-0.1 ~
execute at @s run particle flame ~ ~0.1 ~ 0.7 0.1 0.7 0.01 1 normal

execute at @s run execute as @e[type=#minecraft:can_be_on_fire,predicate=!mobs:is_on_fire,tag=!invulnerable,distance=..1.5] run function skills:boots/flamewalker_hit

kill @s[scores={cd=80..}]