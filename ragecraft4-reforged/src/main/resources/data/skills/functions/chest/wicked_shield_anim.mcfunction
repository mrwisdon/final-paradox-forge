scoreboard players add @s cd 1

execute at @s run particle block nether_quartz_ore ^ ^ ^1.3 0.1 0.1 0.1 0.1 1 normal
execute at @s run particle block bone_block ^ ^ ^1.7 0 0 0 0.1 1 normal
execute at @s run particle electric_spark ^ ^ ^1.7 0 0 0 0.1 2 normal

execute at @s run tp @s ~ ~ ~ ~6 ~

execute at @s[scores={cd=..60}] run function skills:chest/wicked_shield_anim
kill @s[scores={cd=60..}]