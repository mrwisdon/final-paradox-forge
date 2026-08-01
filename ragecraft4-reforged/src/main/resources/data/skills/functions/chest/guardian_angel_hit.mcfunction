execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 3
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 3
execute as @p[distance=..7,predicate=skills:guardian_angel] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 3
execute as @p[distance=..7,predicate=skills:guardian_angel] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 3
scoreboard players operation @s[tag=!no_knockback] x -= @p[distance=..9,predicate=skills:guardian_angel] x
scoreboard players add @s[tag=!no_knockback] y 15
scoreboard players operation @s[tag=!no_knockback] z -= @p[distance=..9,predicate=skills:guardian_angel] z
execute if entity @p[distance=..7,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.03 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..7,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[1] double 0.03 run scoreboard players get @s[tag=!no_knockback] y
execute if entity @p[distance=..7,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.03 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..5,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.05 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..5,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.05 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..4,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.08 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..4,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.08 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..3,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.12 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..3,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.12 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..2,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.2 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..2,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.2 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @p[distance=..1,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[0] double 0.4 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @p[distance=..1,predicate=skills:guardian_angel] store result entity @s[tag=!no_knockback] Motion[2] double 0.4 run scoreboard players get @s[tag=!no_knockback] z