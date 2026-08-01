execute at @s if entity @p[distance=..9,predicate=skills:mana_flare] run function skills:chest/mana_flare_effect
execute at @s run execute as @p[distance=..9,predicate=skills:mana_flare,scores={mana=..19}] run scoreboard players add @s mana 1

tag @s add mob_set_on_fire