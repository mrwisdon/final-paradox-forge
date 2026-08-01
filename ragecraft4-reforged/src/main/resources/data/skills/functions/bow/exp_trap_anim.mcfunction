scoreboard players add @s cd 1

execute at @s run particle dust 0.467 0.161 0.141 1 ^ ^ ^1.3 0.1 0.1 0.1 0.01 1 normal
execute at @s run particle flame ^ ^ ^0.9 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:bow/exp_trap_anim
kill @s[scores={cd=120..}]