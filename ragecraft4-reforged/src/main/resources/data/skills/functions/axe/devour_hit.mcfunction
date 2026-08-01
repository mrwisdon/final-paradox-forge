particle block bone_block ~ ~1 ~ 0.5 0.8 0.5 0.2 60 normal
particle dust 0.345 0.039 0.039 1 ~ ~1 ~ 0.3 0.6 0.3 0.2 150 normal

execute store result score @s[tag=!invulnerable] helper_health run data get entity @s Health 100
execute at @s[tag=!invulnerable,scores={helper_health=..2250},predicate=skills:has_vulnerability] run execute as @p[tag=devour_attack,distance=..7] at @s run function skills:axe/devour_kill
execute at @s[tag=!invulnerable,scores={helper_health=..1500},predicate=!skills:has_vulnerability] run execute as @p[tag=devour_attack,distance=..7] at @s run function skills:axe/devour_kill

execute as @s run function ragecraft4reforged:damage/damage15
