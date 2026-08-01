execute at @s run particle dust 0.02 0.016 0.02 1 ^ ^0.1 ^0.5 0 0 0 0.01 5 force
execute at @s run particle smoke ^ ^0.3 ^0.5 0 0 0 0.01 3 force
scoreboard players add @s cd 1
kill @s[scores={cd=7..}]