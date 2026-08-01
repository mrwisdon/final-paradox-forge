effect give @s[type=#minecraft:undead] instant_health 1 0 true
effect give @s[type=#minecraft:non_undead] instant_damage 1 0 true
execute as @s run function ragecraft4reforged:damage/damage3
execute as @s run function skills:misc/setonfire_7s

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 3
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 3
execute as @e[distance=..6,type=armor_stand,tag=explosive_trap] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 3
execute as @e[distance=..6,type=armor_stand,tag=explosive_trap] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 3
scoreboard players operation @s[tag=!no_knockback] x -= @e[distance=..6,type=armor_stand,tag=explosive_trap] x
scoreboard players add @s[tag=!no_knockback] y 25
scoreboard players operation @s[tag=!no_knockback] z -= @e[distance=..6,type=armor_stand,tag=explosive_trap] z
execute if entity @e[distance=..6,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[0] double 0.01 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @e[distance=..6,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[1] double 0.01 run scoreboard players get @s[tag=!no_knockback] y
execute if entity @e[distance=..6,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @e[distance=..4,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @e[distance=..4,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @e[distance=..3,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @e[distance=..3,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @e[distance=..2,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[0] double 0.07 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @e[distance=..2,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[2] double 0.07 run scoreboard players get @s[tag=!no_knockback] z
execute if entity @e[distance=..1,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[0] double 0.14 run scoreboard players get @s[tag=!no_knockback] x
execute if entity @e[distance=..1,type=armor_stand,tag=explosive_trap] store result entity @s[tag=!no_knockback] Motion[2] double 0.14 run scoreboard players get @s[tag=!no_knockback] z