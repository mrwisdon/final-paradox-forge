execute at @s[type=#minecraft:hostile_full_ice,nbt=!{IsBaby:1b}] unless entity @s[type=guardian] run summon armor_stand ~ ~-1 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_bot","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[type=#minecraft:hostile_full_ice,nbt=!{IsBaby:1b}] if entity @s[type=guardian] run summon armor_stand ~ ~-1 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_bot","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[type=#minecraft:hostile_full_ice,nbt=!{IsBaby:1b}] run summon armor_stand ~ ~-0.5 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_top","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[type=#minecraft:hostile_full_ice,nbt={IsBaby:1b}] run summon armor_stand ~ ~-0.5 ~ {Small:1b,NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_top","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[type=spider] run summon armor_stand ~ ~-1 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_bot","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[type=cave_spider] run summon armor_stand ~ ~-1 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["cold_snap_ice","cold_snap_bot","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}

effect give @s slowness 7 6 false
scoreboard players set @s cold_snap_target 140

tag @s[nbt={NoAI:1b},tag=!no_ai_checked] add no_ai
tag @s add no_ai_checked
data modify entity @s[tag=!no_ai,nbt=!{NoAI:1b}] NoAI set value 1b

execute as @s store result score @s fuse run data get entity @s Fuse 1
data modify entity @s[type=creeper] Fuse set value 500

tag @s add mob_tick
tag @s add custom
tag @s add frozen

execute at @s run particle block blue_ice ~ ~1 ~ 0.6 0.6 0.6 0.4 60 normal
execute at @s run playsound block.glass.break master @a[distance=..15] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..15] ~ ~ ~ 1 0.8 1