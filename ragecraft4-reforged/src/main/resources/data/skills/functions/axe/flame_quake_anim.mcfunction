scoreboard players add @s cd 1

execute at @s positioned ~ ~-0.4 ~ run particle dust 0.259 0.082 0.063 1 ^ ^ ^3.6 0.1 0.1 0.1 0.01 2 normal
execute at @s positioned ~ ~-0.4 ~ run particle flame ^ ^ ^3.6 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~0.012 ~ ~2 ~

execute at @s[scores={cd=..360}] run function skills:axe/flame_quake_anim
kill @s[scores={cd=360..}]