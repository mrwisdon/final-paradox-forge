execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=4}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s wither 15 1 false
effect give @s slowness 15 0 false
execute at @s run scoreboard players add @p[scores={potion_selected=4,mana=..19}] mana 1