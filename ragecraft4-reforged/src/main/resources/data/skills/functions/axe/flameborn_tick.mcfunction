execute at @s if score #rc4tick tick10 matches 5 run function skills:axe/flameborn_tick_10

effect give @s fire_resistance 1 0 true
effect give @s speed 1 1 true

execute at @s run particle flame ~ ~1 ~ 3 1 3 0.02 3 normal
execute at @s run particle falling_lava ~ ~0.2 ~ 0.3 0.2 0.3 0.01 3 normal

scoreboard players remove @s[scores={flameborn_duration=1..}] flameborn_duration 1