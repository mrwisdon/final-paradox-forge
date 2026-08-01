#effect give @s[type=#minecraft:undead] instant_health 1 0 true
#effect give @s[type=#minecraft:non_undead] instant_damage 1 0 true

execute at @s[nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage6
damage @s[tag=!no_target,nbt=!{HurtTime:10s}] 6 player_attack by @p[tag=arson_attack]
execute as @s run function skills:misc/setonfire_7s

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 6
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 6
execute as @p[tag=arson_attack] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 6
execute as @p[tag=arson_attack] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 6
scoreboard players operation @s[tag=!no_knockback] x -= @p[tag=arson_attack] x
scoreboard players add @s[tag=!no_knockback] y 18
scoreboard players operation @s[tag=!no_knockback] z -= @p[tag=arson_attack] z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..15] store result entity @s[tag=!no_knockback] Motion[0] double 0.005 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..15] store result entity @s[tag=!no_knockback] Motion[1] double 0.01 run scoreboard players get @s y
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..15] store result entity @s[tag=!no_knockback] Motion[2] double 0.005 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..9] store result entity @s[tag=!no_knockback] Motion[0] double 0.01 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..9] store result entity @s[tag=!no_knockback] Motion[2] double 0.01 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..7] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..7] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..5] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..5] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..3] store result entity @s[tag=!no_knockback] Motion[0] double 0.8 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..3] store result entity @s[tag=!no_knockback] Motion[2] double 0.8 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..2] store result entity @s[tag=!no_knockback] Motion[0] double 0.13 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=arson_attack,distance=..2] store result entity @s[tag=!no_knockback] Motion[2] double 0.13 run scoreboard players get @s z

tag @s add arson_hit