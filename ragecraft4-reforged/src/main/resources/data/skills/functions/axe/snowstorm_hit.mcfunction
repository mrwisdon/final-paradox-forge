execute store result score @s helper_health run data get entity @s Health 100
execute if entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1800}] run execute at @s run damage @s 18 player_attack by @p[scores={delayed_att=1..}]
execute unless entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..1800}] run execute at @s run damage @s 18 player_attack

tag @s add hurt

tag @s add snowstorm_hit