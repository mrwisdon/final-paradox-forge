execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=8}] chal_hit_potion 1
tag @s add hit_by_potion

execute at @s run effect give @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] wither 8 1 false