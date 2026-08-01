execute at @s run particle dust 0.627 0.624 0.624 1 ^ ^0.1 ^0.5 0 0 0 0.01 5 normal
execute at @s run particle crit ^ ^0.3 ^0.5 0 0 0 0.01 5 normal
scoreboard players add @s cd 1
kill @s[scores={cd=7..}]