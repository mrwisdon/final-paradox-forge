scoreboard players set @s arctic_target 200
data modify entity @s NoAI set value 1b

effect give @s slowness 10 6 false

execute as @s[type=creeper] store result score @s fuse run data get entity @s Fuse 1
data modify entity @s[type=creeper] Fuse set value 500

tag @s add mob_tick

execute at @s run particle block blue_ice ~ ~1 ~ 0.6 0.6 0.6 0.4 60 force
execute at @s run playsound block.glass.break master @a[distance=..25] ~ ~ ~ 0.3 0.7 0.2

execute at @s[nbt=!{IsBaby:1b}] run summon armor_stand ~ ~-1 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["arctic_frozen","arctic_frozen_bot","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[nbt=!{IsBaby:1b}] run summon armor_stand ~ ~-0.5 ~ {NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["arctic_frozen","arctic_frozen_top","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}
execute at @s[nbt={IsBaby:1b}] run summon armor_stand ~ ~-0.5 ~ {Small:1b,NoGravity:1b,Silent:1b,Invulnerable:1b,ShowArms:0b,Invisible:1b,NoBasePlate:1b,Tags:["arctic_frozen","arctic_frozen_top","marker_tick"],ArmorItems:[{},{},{},{id:"minecraft:blue_ice",Count:1b}]}