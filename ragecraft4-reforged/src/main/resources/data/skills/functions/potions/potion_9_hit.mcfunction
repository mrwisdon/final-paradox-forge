execute at @s[tag=!hit_by_potion] run scoreboard players add @p[scores={potion_selected=9}] chal_hit_potion 1
tag @s add hit_by_potion

effect give @s weakness 30 0 false
effect give @s slowness 30 0 false

execute at @s run scoreboard players add @p[scores={potion_selected=9,mana=..19}] mana 1

scoreboard players set @s unholy_blood_time 600
tag @s add mob_tick
tag @s add unholy_blood