scoreboard players add @s cd 1
execute at @s[scores={cd=1}] run data modify entity @s Rotation set from entity @p[predicate=skills:call_void] Rotation

execute at @s positioned ^ ^ ^-0.7 run particle smoke ~ ~0.9 ~ 0.1 0.1 0.1 0.01 5 force
execute at @s positioned ^ ^ ^-0.7 run particle dust 0.031 0 0.114 1 ~ ~0.9 ~ 0.1 0.1 0.1 0.01 10 force

execute at @s if entity @e[tag=r4r_ability_target,tag=!no_target,distance=..2] run function skills:offhand/call_void_trigger

execute at @s[scores={cd=4..}] facing entity @e[tag=r4r_ability_target,tag=!no_target,distance=..25,limit=1,sort=nearest] feet run tp ^ ^ ^
execute at @s positioned ^ ^ ^0.25 if block ~ ~0.6 ~ #minecraft:nonsolid run tp ^ ^ ^
execute at @s positioned ^ ^ ^0.25 unless block ~ ~0.6 ~ #minecraft:nonsolid run function skills:offhand/call_void_trigger

execute at @s[scores={cd=150..}] run function skills:offhand/call_void_trigger
