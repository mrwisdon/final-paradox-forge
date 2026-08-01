execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=5}] chal_hit_potion 1
tag @s add hit_by_potion

execute at @s run scoreboard players add @p[scores={potion_selected=5,mana=..19}] mana 1

effect give @s slowness 12 2 false
scoreboard players set @s frostburn_debuff 240
tag @s add mob_tick