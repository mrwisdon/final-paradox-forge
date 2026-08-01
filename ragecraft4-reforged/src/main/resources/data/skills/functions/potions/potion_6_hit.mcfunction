execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=6}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s wither 9 2 false
execute at @s run scoreboard players add @p[scores={potion_selected=6,mana=..19}] mana 1