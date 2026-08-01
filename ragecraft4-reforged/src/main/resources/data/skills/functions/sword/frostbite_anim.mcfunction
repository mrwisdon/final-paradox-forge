scoreboard players add @s cd 1

execute at @s run particle electric_spark ^ ^0.8 ^1 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:sword/frostbite_anim
kill @s[scores={cd=120..}]