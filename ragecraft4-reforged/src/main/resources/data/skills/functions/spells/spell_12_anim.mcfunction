scoreboard players add @s cd 1

execute at @s run particle portal ^ ^0.1 ^1.4 0.05 0.05 0.05 0.01 3 normal
execute at @s run particle falling_obsidian_tear ^ ^0.1 ^1.1 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~ ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:spells/spell_12_anim
kill @s[scores={cd=120..}]