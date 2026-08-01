execute as @s run function skills:misc/setonfire_7s

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
execute as @p[tag=magma_shield_attack] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @p[tag=magma_shield_attack] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
scoreboard players operation @s[tag=!no_knockback] x -= @p[tag=magma_shield_attack] x
scoreboard players add @s[tag=!no_knockback] y 15
scoreboard players operation @s[tag=!no_knockback] z -= @p[tag=magma_shield_attack] z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..7] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..7] store result entity @s[tag=!no_knockback] Motion[1] double 0.02 run scoreboard players get @s y
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..7] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..5] store result entity @s[tag=!no_knockback] Motion[0] double 0.03 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..5] store result entity @s[tag=!no_knockback] Motion[2] double 0.03 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..4] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..4] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..3] store result entity @s[tag=!no_knockback] Motion[0] double 0.08 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..3] store result entity @s[tag=!no_knockback] Motion[2] double 0.08 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..2] store result entity @s[tag=!no_knockback] Motion[0] double 0.12 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..2] store result entity @s[tag=!no_knockback] Motion[2] double 0.12 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..1] store result entity @s[tag=!no_knockback] Motion[0] double 0.2 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=magma_shield_attack,distance=..1] store result entity @s[tag=!no_knockback] Motion[2] double 0.2 run scoreboard players get @s z