scoreboard players add @s cd 1

execute at @s run particle dust 0.082 0.039 0.145 1 ^ ^ ^0.8 0 0 0 0.01 1 normal

execute at @s run tp @s ~ ~0.005 ~ ~3 ~

execute at @s[scores={cd=..360}] run function skills:axe/shadow_grasp_anim
kill @s[scores={cd=360..}]