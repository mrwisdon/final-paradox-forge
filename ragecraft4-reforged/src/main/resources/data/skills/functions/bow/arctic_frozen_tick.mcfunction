scoreboard players remove @s[scores={arctic_target=1..}] arctic_target 1

data modify entity @s[scores={arctic_target=..0}] NoAI set value 0b
execute as @s[type=creeper,scores={arctic_target=..0}] store result entity @s Fuse float 1 run scoreboard players get @s fuse