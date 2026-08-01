execute at @s run particle block nether_wart_block ~ ~1 ~ 0.5 0.8 0.5 0.2 60 normal
execute at @s run particle dust 0.345 0.039 0.039 1 ~ ~1 ~ 0.3 0.6 0.3 0.2 150 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.6 0.6 0.4
execute at @s run playsound block.beehive.enter master @a[distance=..20] ~ ~ ~ 0.8 1 0.8

execute at @s run summon snowball ~0.3 ~0.5 ~0.3 {Motion:[0.15,0.3,0.15],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_quartz_ore",Count:1b}}
execute at @s run summon snowball ~-0.3 ~0.5 ~ {Motion:[-0.12,0.35,0.1],Tags:["blood","marker_tick"],Item:{id:"minecraft:netherrack",Count:1b}}
execute at @s run summon snowball ~ ~0.5 ~-0.3 {Motion:[0.07,0.25,-0.13],Tags:["blood","marker_tick"],Item:{id:"minecraft:netherrack",Count:1b}}
execute at @s run summon snowball ~-0.3 ~0.5 ~-0.3 {Motion:[-0.08,0.3,-0.17],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_quartz_ore",Count:1b}}
execute at @s run summon snowball ~0.3 ~0.5 ~-0.3 {Motion:[0.12,0.25,-0.08],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_quartz_ore",Count:1b}}

kill @s