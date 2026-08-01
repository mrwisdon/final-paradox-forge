scoreboard players add @s cd 1

execute at @s run particle dust 1 1 0.898 1 ^ ^ ^3.6 0.1 0.1 0.1 0.01 1 normal
execute at @s run particle electric_spark ^ ^ ^2.6 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:pickaxe/overcharge_anim
kill @s[scores={cd=120..}]