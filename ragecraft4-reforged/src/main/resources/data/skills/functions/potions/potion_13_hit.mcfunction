execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=13}] chal_hit_potion 1
tag @s add hit_by_potion

scoreboard players set @s bottled_lightning 600
tag @s add mob_tick

execute at @s run scoreboard players add @p[scores={potion_selected=13,mana=..19}] mana 1