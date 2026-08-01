execute at @s run particle block blue_ice ~ ~1.5 ~ 0.3 0.4 0.3 0.2 1 force

execute at @s[tag=arctic_frozen_top] run function skills:bow/arctic_check_top
execute at @s[tag=arctic_frozen_bot] run function skills:bow/arctic_check_bot