scoreboard players add @s cd 1
execute at @s unless entity @p[distance=..4,predicate=skills:spell_10,scores={frost_ray_cd=1..}] run kill @s

execute at @s run data modify entity @s Rotation set from entity @p[predicate=skills:spell_10,scores={frost_ray_cd=1..}] Rotation
execute at @s run data modify entity @s Pose.RightArm[0] set from entity @p[predicate=skills:spell_10,scores={frost_ray_cd=1..}] Rotation[1]

execute as @s[scores={cd=6}] run function skills:spells/spell_10_texture