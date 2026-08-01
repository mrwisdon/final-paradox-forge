scoreboard players add @s cd 1

execute at @s run particle dust 0.475 0.686 0.51 1 ^ ^ ^0.8 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~0.005 ~ ~3 ~

execute at @s[scores={cd=..360}] run function skills:spells/spell_4_anim
kill @s[scores={cd=360..}]