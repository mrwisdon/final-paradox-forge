execute store result score @s helper_health run data get entity @s Health 100
execute if entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1500}] run execute at @s run damage @s 16 magic by @p[tag=trinity_attack]
execute unless entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1500}] run execute at @s run damage @s 16 magic

execute as @s run function skills:misc/setonfire_7s