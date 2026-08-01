execute store result score @s[tag=!invulnerable] helper_health run data get entity @s[tag=!invulnerable] Health 100
execute at @s[tag=!invulnerable,scores={helper_health=..149}] run execute as @p[scores={meleehit=1..,distance=..6}] at @s run function skills:misc/meleekill
kill @s[tag=!invulnerable,scores={helper_health=..149}]
execute unless entity @s[tag=!invulnerable,scores={helper_health=..149}] store result entity @s[tag=!invulnerable] Health float 0.01 run scoreboard players remove @s[tag=!invulnerable] helper_health 150
tag @s add hurt