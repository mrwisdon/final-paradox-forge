scoreboard players remove @s[scores={cold_snap_target=1..}] cold_snap_target 1

data modify entity @s[scores={cold_snap_target=..0},tag=!no_ai] NoAI set value 0b
tag @s[scores={cold_snap_target=..0}] remove frozen
execute as @s[type=creeper,scores={cold_snap_target=..0}] store result entity @s Fuse float 1 run scoreboard players get @s fuse