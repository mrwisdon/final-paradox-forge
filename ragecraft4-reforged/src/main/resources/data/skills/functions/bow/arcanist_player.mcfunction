scoreboard players remove @s arcanist_timer 1
scoreboard players set @s[scores={arcanist_timer=400..500}] arcanist_timer 0

execute at @s[scores={arcanist_timer=1..200},predicate=skills:holding_bow] run particle dust 0.8 0.6 0.827 1 ^-0.5 ^1.2 ^0.4 0.25 0.25 0.25 0.2 1 normal