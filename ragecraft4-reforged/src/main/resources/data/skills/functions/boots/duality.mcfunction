effect clear @s[scores={duality_cd=12..}] speed
effect give @s[scores={duality_cd=12..}] haste 6 1 true

scoreboard players remove @s[scores={duality_cd=1..}] duality_cd 1

effect give @s[predicate=!general:biome_plains,scores={duality_cd=..0}] speed 2 0 true