scoreboard players set @s shadow_spikes_cd 0

execute at @s run playsound minecraft:entity.wither.break_block master @a[distance=..16] ~ ~ ~ 0.4 1.2 0.4
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..16] ~ ~ ~ 0.3 1.3 0.3
execute at @s run playsound block.pumpkin.carve master @a[distance=..16] ~ ~ ~ 1 1 1
execute at @s run playsound entity.zombie.infect master @a[distance=..16] ~ ~ ~ 1 1 1

execute at @s run particle squid_ink ~ ~0.8 ~ 1 0.4 1 0.3 35 normal
execute at @s run particle reverse_portal ~ ~0.5 ~ 1 0.4 1 0.1 200 normal

execute at @s run summon snowball ~0.5 ~0.5 ~0.5 {Tags:["obsidian_shards","marker_tick"],Motion:[0.5,0.2,0.5],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~0.2 ~0.5 ~0.7 {Tags:["obsidian_shards","marker_tick"],Motion:[0.2,0.35,0.7],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~-0.3 ~0.5 ~0.8 {Tags:["obsidian_shards","marker_tick"],Motion:[-0.3,0.3,0.8],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~0.6 ~0.5 ~-0.3 {Tags:["obsidian_shards","marker_tick"],Motion:[0.6,0.25,-0.3],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~-0.2 ~0.5 ~0.5 {Tags:["obsidian_shards","marker_tick"],Motion:[-0.2,0.2,0.5],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~0.4 ~0.5 ~-0.5 {Tags:["obsidian_shards","marker_tick"],Motion:[0.4,0.15,-0.5],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~-0.35 ~0.5 ~-0.4 {Tags:["obsidian_shards","marker_tick"],Motion:[-0.35,0.3,-0.4],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~-0.4 ~0.5 ~-0.6 {Tags:["obsidian_shards","marker_tick"],Motion:[-0.4,0.25,-0.6],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~-0.65 ~0.5 ~-0.1 {Tags:["obsidian_shards","marker_tick"],Motion:[-0.65,0.3,-0.1],Item:{id:"minecraft:black_concrete",Count:1b}}
execute at @s run summon snowball ~0.6 ~0.5 ~ {Tags:["obsidian_shards","marker_tick"],Motion:[0.6,0.35,0.0],Item:{id:"minecraft:black_concrete",Count:1b}}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..6] run function skills:offhand/shadow_spikes_hit