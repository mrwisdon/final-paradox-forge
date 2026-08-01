execute at @s run particle dust 1 1 1 1 ^ ^0.1 ^0.5 0.1 0.1 0.1 0.01 5 force
scoreboard players add @s cd 1
kill @s[scores={cd=8..}]