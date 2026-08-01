execute at @s run damage @s 8 player_attack by @p[scores={delayed_att=1..}]

tag @s add hurt

execute at @s if entity @e[type=armor_stand,tag=strength_1,distance=..2] run function ragecraft4reforged:damage/damage3
execute at @s if entity @e[type=armor_stand,tag=strength_2,distance=..2] run function ragecraft4reforged:damage/damage6
execute at @s if entity @e[type=armor_stand,tag=strength_3,distance=..2] run function ragecraft4reforged:damage/damage9
execute at @s if entity @e[type=armor_stand,tag=strength_4,distance=..2] run function ragecraft4reforged:damage/damage12
execute at @s if entity @e[type=armor_stand,tag=strength_5,distance=..2] run function ragecraft4reforged:damage/damage15

execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..20] ~ ~ ~ 0.2 1.2 0.1
execute at @s run playsound item.trident.hit master @a[distance=..20] ~ ~ ~ 0.8 0.8 0.2

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
execute as @e[type=armor_stand,tag=axe_throw,limit=1,sort=nearest] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 5
execute as @e[type=armor_stand,tag=axe_throw,limit=1,sort=nearest] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 5
scoreboard players operation @s[tag=!no_knockback] x -= @e[type=armor_stand,tag=axe_throw,limit=1,sort=nearest] x
scoreboard players set @s[tag=!no_knockback] y 3
scoreboard players operation @s[tag=!no_knockback] z -= @e[type=armor_stand,tag=axe_throw,limit=1,sort=nearest] z
execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[0] double 0.1 run scoreboard players get @s[tag=!no_knockback] x
execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[1] double 0.1 run scoreboard players get @s[tag=!no_knockback] y
execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[2] double 0.1 run scoreboard players get @s[tag=!no_knockback] z

execute at @s run particle block nether_wart_block ~ ~1 ~ 0.3 0.5 0.3 0.05 20 normal
execute at @s run particle crit ~ ~1 ~ 0.3 0.5 0.3 0.05 40 normal
execute at @s run particle sweep_attack ~ ~1 ~ 0.3 0.3 0.3 0.2 3 normal

tag @s add axe_throw_done