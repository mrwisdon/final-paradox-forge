execute store result score @s helper_health run data get entity @s Health 100
execute if entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1000}] run execute at @s run damage @s 10 magic by @p[scores={spell_cd=..10,last_spell=9}]
execute unless entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1000}] run execute at @s run damage @s 10 magic

execute as @s run function skills:misc/setonfire_7s

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={last_spell=9}] chal_hit_spell 1
tag @s add hit_by_spell