execute at @s run tp @s ~ ~ ~ ~5 ~

execute at @s positioned ~ ~0.5 ~ run particle falling_obsidian_tear ^ ^ ^1.4 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.941 0.271 0.416 1 ^ ^ ^1.6 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle falling_obsidian_tear ^ ^ ^-1.4 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.941 0.271 0.416 1 ^ ^ ^-1.6 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle falling_obsidian_tear ^1.4 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.941 0.271 0.416 1 ^1.6 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle falling_obsidian_tear ^-1.4 ^ ^ 0 0 0 0.01 1 normal
execute at @s positioned ~ ~0.5 ~ run particle dust 0.941 0.271 0.416 1 ^-1.6 ^ ^ 0 0 0 0.01 1 normal

scoreboard players add @s cd 1
execute at @s[scores={cd=5}] run function skills:bow/vt_healing_crystal_buff
scoreboard players set @s[scores={cd=20..}] cd 0

execute at @s[scores={cd=0}] run particle heart ~ ~1 ~ 0.3 0.6 0.3 0.2 1 normal
execute at @s[scores={cd=4}] run particle heart ~ ~1 ~ 0.3 0.6 0.3 0.2 1 normal
execute at @s[scores={cd=8}] run particle heart ~ ~1 ~ 0.3 0.6 0.3 0.2 1 normal
execute at @s[scores={cd=12}] run particle heart ~ ~1 ~ 0.3 0.6 0.3 0.2 1 normal
execute at @s[scores={cd=16}] run particle heart ~ ~1 ~ 0.3 0.6 0.3 0.2 1 normal
execute at @s run particle crimson_spore ~ ~1 ~ 0.6 0.4 0.6 0.02 2 normal
execute at @s run particle portal ~ ~1 ~ 0.5 0.3 0.5 0.02 3 normal
execute at @s run particle dust 1.000 0.549 0.745 1 ~ ~1 ~ 0.1 0.4 0.1 0.1 3 normal
