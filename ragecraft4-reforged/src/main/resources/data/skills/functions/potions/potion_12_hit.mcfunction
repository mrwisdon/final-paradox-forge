execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=12}] chal_hit_potion 1
tag @s add hit_by_potion

execute at @s run scoreboard players add @p[scores={potion_selected=12},distance=..20] sapping_potion_buff 1

execute at @s if entity @p[scores={potion_selected=12},distance=..20] run summon marker ~ ~1 ~ {Tags:["marker_tick","sapping_potion"]}

effect give @s weakness 15 1 false
execute at @s run scoreboard players add @p[scores={potion_selected=12,mana=..19},distance=..20] mana 1