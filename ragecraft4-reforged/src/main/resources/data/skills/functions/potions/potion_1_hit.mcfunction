execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=1}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s weakness 15 0 false
effect give @s slowness 15 2 false
execute at @s run scoreboard players add @p[scores={potion_selected=1,mana=..19}] mana 1