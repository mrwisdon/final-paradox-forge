# Place the complete B5 arena interior (85x62x57, incl. ceiling) with the command
# source standing at the arena center (originally -1107 49 1426).
place template finalparadox:arenas/b5/tile_0_0_0 ~-64 ~-7 ~-28
place template finalparadox:arenas/b5/tile_1_0_0 ~-16 ~-7 ~-28
place template finalparadox:arenas/b5/tile_0_0_1 ~-64 ~-7 ~20
place template finalparadox:arenas/b5/tile_1_0_1 ~-16 ~-7 ~20
place template finalparadox:arenas/b5/tile_0_1_0 ~-64 ~41 ~-28
place template finalparadox:arenas/b5/tile_1_1_0 ~-16 ~41 ~-28
place template finalparadox:arenas/b5/tile_0_1_1 ~-64 ~41 ~20
place template finalparadox:arenas/b5/tile_1_1_1 ~-16 ~41 ~20
tellraw @s {"text":"B5 arena placed. You are standing at the arena center; the combat platform is right below you.","color":"green"}
