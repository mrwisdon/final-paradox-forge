execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=11}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s levitation 6 1 false
scoreboard players set @s cyclone_time 120
tag @s add mob_tick
execute at @s run scoreboard players add @p[scores={potion_selected=11,mana=..19}] mana 1