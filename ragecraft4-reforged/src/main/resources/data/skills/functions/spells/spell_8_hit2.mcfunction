effect give @s wither 5 2 false
tag @s add r4r_corrosion_dot_apply

execute store result score @s helper_health run data get entity @s Health 100
execute if entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..500}] run execute at @s run damage @s 5 magic by @p[scores={acid_spray_cd=1..}]
execute unless entity @s[tag=!invulnerable,nbt=!{ActiveEffects:[{Id:11}]},scores={helper_health=..500}] run execute at @s run function ragecraft4reforged:damage/damage3

execute at @s run tag @p[scores={acid_spray_cd=1..}] add acid_spray_mana
tag @s remove spell_8_hit1
tag @s remove spell_8_hit2

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={last_spell=8}] chal_hit_spell 1
tag @s add hit_by_spell
