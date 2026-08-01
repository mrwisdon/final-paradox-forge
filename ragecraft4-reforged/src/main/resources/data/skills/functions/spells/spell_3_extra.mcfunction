execute at @s run particle dust 0.816 0.957 1 1 ~ ~ ~ 0.1 0.1 0.1 0.2 2 force
execute at @s run execute as @e[tag=r4r_ability_target,distance=..2] run effect give @s slowness 4 2 false
scoreboard players add @s cd 1
kill @s[scores={cd=5}]