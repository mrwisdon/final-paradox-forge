execute at @s run particle block blue_ice ~ ~1 ~ 0.3 0.3 0.3 0.5 10 force
execute at @s run particle item_snowball ~ ~1 ~ 0.3 0.3 0.3 0.2 10 force
effect give @s[nbt=!{NoAI:1b}] slowness 10 2 false

execute as @s[tag=!elite] run function skills:bow/arctic_frozen