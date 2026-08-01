execute at @s run scoreboard players add @p[tag=spell_6_cast,scores={mana=..18}] mana 2
execute at @s run scoreboard players add @p[tag=spell_6_cast,scores={mana=19}] mana 1

execute at @s[tag=!hit_by_spell] run scoreboard players add @p[tag=spell_6_cast] chal_hit_spell 1
tag @s add hit_by_spell

#effect give @s[type=#minecraft:non_undead] instant_damage 1 1
#effect give @s[type=#minecraft:undead] instant_health 1 1

execute at @s run damage @s 12 magic by @p[tag=spell_6_cast]

execute at @s run scoreboard players add @p[tag=spell_6_cast,distance=..20] wind_blast_buff 1

execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 12
execute as @s[tag=!no_knockback] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 12
execute as @p[tag=spell_6_cast] store result score @s[tag=!no_knockback] x run data get entity @s[tag=!no_knockback] Pos[0] 12
execute as @p[tag=spell_6_cast] store result score @s[tag=!no_knockback] z run data get entity @s[tag=!no_knockback] Pos[2] 12
scoreboard players operation @s[tag=!no_knockback] x -= @p[tag=spell_6_cast] x
scoreboard players add @s[tag=!no_knockback] y 50
scoreboard players operation @s[tag=!no_knockback] z -= @p[tag=spell_6_cast] z
execute at @s[tag=!no_knockback] store result entity @s Motion[1] double 0.01 run scoreboard players get @s y
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..15] store result entity @s[tag=!no_knockback] Motion[0] double 0.02 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..15] store result entity @s[tag=!no_knockback] Motion[2] double 0.02 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..10] store result entity @s[tag=!no_knockback] Motion[0] double 0.03 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..10] store result entity @s[tag=!no_knockback] Motion[2] double 0.03 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..7] store result entity @s[tag=!no_knockback] Motion[0] double 0.04 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..7] store result entity @s[tag=!no_knockback] Motion[2] double 0.04 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..5] store result entity @s[tag=!no_knockback] Motion[0] double 0.06 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..5] store result entity @s[tag=!no_knockback] Motion[2] double 0.06 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..3] store result entity @s[tag=!no_knockback] Motion[0] double 0.09 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..3] store result entity @s[tag=!no_knockback] Motion[2] double 0.09 run scoreboard players get @s z
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..2] store result entity @s[tag=!no_knockback] Motion[0] double 0.15 run scoreboard players get @s x
execute at @s[tag=!no_knockback] if entity @p[tag=spell_6_cast,distance=..2] store result entity @s[tag=!no_knockback] Motion[2] double 0.15 run scoreboard players get @s z