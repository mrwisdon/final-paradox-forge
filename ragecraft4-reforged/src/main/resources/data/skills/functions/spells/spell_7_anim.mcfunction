scoreboard players add @s cd 1

execute at @s run particle dust 0.141 0.082 0.231 1 ^ ^ ^3.6 0.1 0.1 0.1 0.01 1 normal
execute at @s run particle smoke ^ ^ ^3.6 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~0.01 ~ ~3 ~

execute at @s[scores={cd=..360}] run function skills:spells/spell_7_anim
kill @s[scores={cd=360..}]