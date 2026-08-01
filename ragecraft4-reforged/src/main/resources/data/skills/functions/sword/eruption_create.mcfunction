scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

kill @e[type=armor_stand,tag=eruption]
summon armor_stand ^ ^1 ^2 {Silent:1b,Invulnerable:1b,Small:1b,Invisible:1b,NoBasePlate:1b,Tags:["marker_tick","eruption"]}

execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^2 run function area:green_crystal/melt_3x2