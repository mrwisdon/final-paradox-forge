execute at @s run particle dust 0.82 0.875 0.988 1 ~ ~1 ~ 0.4 0.6 0.4 0.5 8 normal
execute at @s run particle item_snowball ~ ~1 ~ 0.4 0.6 0.4 0.5 5 normal

execute store result score @s helper_health run data get entity @s Health 100
execute if entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..600}] run execute at @s run damage @s 6 magic by @p[scores={spell_cd=..4,last_spell=10}]
execute unless entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..600}] run execute at @s run damage @s 6 magic

tag @s add hurt

execute at @s[tag=!elite] unless entity @s[tag=frozen] run function skills:axe/cold_snap_3
effect give @s[tag=elite] slowness 10 4 false
execute at @s if score #rc4tick tick20 matches 5 run tag @p[predicate=skills:spell_10,scores={frost_ray_cd=1..}] add frost_ray_mana
tag @s remove spell_10_hit1
tag @s remove spell_10_hit2

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[predicate=skills:spell_10,scores={last_spell=10}] chal_hit_spell 1
tag @s add hit_by_spell