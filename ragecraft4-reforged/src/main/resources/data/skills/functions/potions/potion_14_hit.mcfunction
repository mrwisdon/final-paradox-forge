execute at @s[tag=elite] run function skills:potions/potion_14_hit_e
execute at @s[tag=!elite] run function skills:potions/potion_14_hit_r

execute at @s run scoreboard players add @p[scores={potion_selected=14,mana=..19}] mana 1