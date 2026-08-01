execute at @s run particle block blue_ice ~ ~1.5 ~ 0.3 0.4 0.3 0.2 1 normal
execute at @s[tag=cold_snap_top] run function skills:axe/cold_snap_check_top
execute at @s[tag=cold_snap_bot] run function skills:axe/cold_snap_check_bot