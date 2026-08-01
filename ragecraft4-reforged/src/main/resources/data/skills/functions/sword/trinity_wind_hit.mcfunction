execute as @s[tag=!no_target,nbt={HurtTime:10s}] run function ragecraft4reforged:damage/damage16
effect give @s[tag=!no_target,nbt=!{HurtTime:10s},type=#minecraft:undead] instant_health 1 1 true
effect give @s[tag=!no_target,nbt=!{HurtTime:10s},type=#minecraft:non_undead] instant_damage 1 1 true
execute as @s[tag=!no_target,nbt=!{HurtTime:10s}] run function ragecraft4reforged:damage/damage4

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 6
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 6
execute as @e[type=marker,tag=trinity_wind_m1] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 6
execute as @e[type=marker,tag=trinity_wind_m1] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 6
scoreboard players operation @s[tag=!no_knockback] x -= @e[type=marker,tag=trinity_wind_m1] x
scoreboard players add @s[tag=!no_knockback] y 22
scoreboard players operation @s[tag=!no_knockback] z -= @e[type=marker,tag=trinity_wind_m1] z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..20] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..20] store result entity @s[tag=!no_knockback] Motion[1] double 0.02 run scoreboard players get @s y
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..20] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..15] store result entity @s[tag=!no_knockback] Motion[0] double 0.03 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..15] store result entity @s[tag=!no_knockback] Motion[2] double 0.03 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..10] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..10] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..7] store result entity @s[tag=!no_knockback] Motion[0] double 0.08 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..7] store result entity @s[tag=!no_knockback] Motion[2] double 0.08 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..5] store result entity @s[tag=!no_knockback] Motion[0] double 0.12 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..5] store result entity @s[tag=!no_knockback] Motion[2] double 0.12 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..3] store result entity @s[tag=!no_knockback] Motion[0] double 0.2 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @e[type=marker,tag=trinity_wind_m1,distance=..3] store result entity @s[tag=!no_knockback] Motion[2] double 0.2 run scoreboard players get @s z

tag @s add trinity_wind_hit