execute at @s if entity @e[tag=r4r_ability_target,tag=elite,distance=..10] run effect give @s strength 1 3 true
execute at @s unless entity @e[tag=r4r_ability_target,tag=elite,distance=..10] run effect give @s[predicate=!general:biome_plains] speed 1 0 true
execute at @s unless entity @e[tag=r4r_ability_target,tag=elite,distance=..10] run effect give @s resistance 1 0 true