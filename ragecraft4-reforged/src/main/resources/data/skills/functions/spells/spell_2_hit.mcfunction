execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
execute as @p[distance=..6,tag=cast_spell_2] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @p[distance=..6,tag=cast_spell_2] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
scoreboard players operation @s[tag=!no_knockback] x -= @p[distance=..5,tag=cast_spell_2] x
scoreboard players add @s[tag=!no_knockback] y 18
scoreboard players operation @s[tag=!no_knockback] z -= @p[distance=..5,tag=cast_spell_2] z
execute if entity @p[distance=..6,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[0] double 0.01 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..6,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[1] double 0.01 run scoreboard players get @s[tag=!no_knockback] y
execute if entity @p[distance=..6,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[2] double 0.01 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..4,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..4,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..3,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..3,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..2,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[0] double 0.08 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..2,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[2] double 0.08 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..1,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[0] double 0.2 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..1,tag=cast_spell_2] store result entity @s[tag=!no_knockback] Motion[2] double 0.2 run scoreboard players get @s[tag=!no_knockback] z

tag @s remove hit_spell_2

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={last_spell=2}] chal_hit_spell 1
tag @s add hit_by_spell