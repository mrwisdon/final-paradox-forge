execute at @s run scoreboard players add @p[scores={last_spell=7,mana=19}] mana 1
execute at @s run scoreboard players add @p[scores={last_spell=7,mana=..18}] mana 2

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[scores={spell_cd=..4,last_spell=7}] chal_hit_spell 1
tag @s add hit_by_spell

execute at @s run damage @s 24 magic by @p[scores={spell_cd=..4,last_spell=7}]

effect give @s slowness 4 6 false
effect give @s ragecraft4reforged:decay 15 0 true

execute as @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 15
execute as @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] store result score @s[tag=!no_knockback] y run data get entity @s[tag=!no_knockback] Pos[1] 15
execute as @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 15
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 15
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] y run data get entity @s[tag=!no_knockback] Pos[1] 15
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 15

scoreboard players operation @s[tag=!no_knockback] x -= @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] x
scoreboard players operation @s[tag=!no_knockback] y -= @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] y
scoreboard players operation @s[tag=!no_knockback] z -= @e[type=armor_stand,tag=spell_marker_7,limit=1,sort=nearest] z

execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[0] double -0.02 run scoreboard players get @s[tag=!no_knockback] x
execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[1] double -0.02 run scoreboard players get @s[tag=!no_knockback] y
execute as @s[tag=!no_knockback] store result entity @s[tag=!no_knockback] Motion[2] double -0.02 run scoreboard players get @s[tag=!no_knockback] z
