execute at @s run damage @s 18 magic by @p[predicate=skills:spell_echo,tag=spell_echo_att]

#effect give @s[type=#minecraft:non_undead] instant_damage 1 1
#effect give @s[type=#minecraft:undead] instant_health 1 1
#execute as @s run function ragecraft4reforged:damage/damage6

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 4
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 4
execute as @p[predicate=skills:spell_echo,distance=..8] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 4
execute as @p[predicate=skills:spell_echo,distance=..8] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 4
scoreboard players operation @s[tag=!no_knockback] x -= @p[predicate=skills:spell_echo,distance=..8] x
scoreboard players add @s[tag=!no_knockback] y 20
scoreboard players operation @s[tag=!no_knockback] z -= @p[predicate=skills:spell_echo,distance=..8] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..9] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..9] store result entity @s[tag=!no_knockback] Motion[1] double 0.02 run scoreboard players get @s[tag=!no_knockback] y
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..9] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s[tag=!no_knockback] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..6] store result entity @s[tag=!no_knockback] Motion[0] double 0.03 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..6] store result entity @s[tag=!no_knockback] Motion[2] double 0.03 run scoreboard players get @s[tag=!no_knockback] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..5] store result entity @s[tag=!no_knockback] Motion[0] double 0.05 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..5] store result entity @s[tag=!no_knockback] Motion[2] double 0.05 run scoreboard players get @s[tag=!no_knockback] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..4] store result entity @s[tag=!no_knockback] Motion[0] double 0.08 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..4] store result entity @s[tag=!no_knockback] Motion[2] double 0.08 run scoreboard players get @s[tag=!no_knockback] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..3] store result entity @s[tag=!no_knockback] Motion[0] double 0.13 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..3] store result entity @s[tag=!no_knockback] Motion[2] double 0.13 run scoreboard players get @s[tag=!no_knockback] z
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..2] store result entity @s[tag=!no_knockback] Motion[0] double 0.25 run scoreboard players get @s[tag=!no_knockback] x
execute at @s[tag=!no_knockback] if entity @p[predicate=skills:spell_echo,distance=..2] store result entity @s[tag=!no_knockback] Motion[2] double 0.25 run scoreboard players get @s[tag=!no_knockback] z