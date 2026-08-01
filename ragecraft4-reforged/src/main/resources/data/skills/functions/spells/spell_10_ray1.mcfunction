particle block packed_ice ~ ~-0.6 ~ 0.6 0.6 0.6 0.01 1 normal
execute unless block ~ ~ ~ #minecraft:nonsolid run tag @s add frost_ray_end
tag @e[tag=r4r_ability_target,tag=!spell_immune,distance=..1.5] add spell_10_hit1
execute as @e[tag=r4r_ability_target,tag=spell_immune,distance=..1.5,limit=1,sort=nearest] if score #rc4tick tick10 matches 5 run function skills:spells/spell_10_immune