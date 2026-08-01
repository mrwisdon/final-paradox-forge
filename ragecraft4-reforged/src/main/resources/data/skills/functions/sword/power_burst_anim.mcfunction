scoreboard players add @s cd 1

#execute at @s run particle wax_off ^ ^ ^0.5 0 0 0 0.1 1 normal
execute at @s run particle wax_off ^ ^0.7 ^0.8 0 0 0 0.1 1 normal
#execute at @s run particle wax_off ^ ^1.4 ^0.5 0 0 0 0.1 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:sword/power_burst_anim
kill @s[scores={cd=120..}]