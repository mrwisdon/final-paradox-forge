scoreboard players add @s cd 1

execute at @s run particle dust 0.89 0.71 0.945 1 ^ ^ ^1.5 0.1 0.1 0.1 0.01 2 normal
execute at @s run particle soul_fire_flame ^ ^ ^1.5 0 0 0 0.01 1 normal

execute at @s run particle dust 0.89 0.71 0.945 1 ^ ^ ^3 0.1 0.1 0.1 0.01 3 normal
execute at @s run particle soul_fire_flame ^ ^ ^3 0 0 0 0.01 1 normal

execute at @s run particle dust 0.89 0.71 0.945 1 ^ ^ ^5 0.1 0.1 0.1 0.01 5 normal
execute at @s run particle soul_fire_flame ^ ^ ^5 0 0 0 0.01 2 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:helmet/spell_echo_anim
kill @s[scores={cd=120..}]