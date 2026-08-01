scoreboard players add @s cd 1

execute at @s run particle dust 0.702 0.792 0.922 1 ^ ^ ^6.5 0.1 0.1 0.1 0.01 4 normal
execute at @s run particle block blue_ice ^ ^ ^5 0.1 0.1 0.1 0.01 2 normal

execute at @s run tp @s ~ ~0.01 ~ ~3 ~

execute at @s[scores={cd=..120}] run function skills:sword/trinity_ice_anim
kill @s[scores={cd=120..}]