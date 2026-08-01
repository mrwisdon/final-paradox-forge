execute at @s unless block ~ ~-0.1 ~ #minecraft:nonsolid run tag @s add eruption_active
scoreboard players add @s[tag=eruption_active] eruption_time 1

execute at @s[scores={eruption_time=1}] run function skills:sword/eruption_start

execute at @s[scores={eruption_time=1..}] run particle lava ~ ~0.5 ~ 0.3 0.2 0.3 0.01 1 normal
execute at @s[scores={eruption_time=1..}] run particle flame ~ ~0.5 ~ 0.2 0.5 0.2 0.01 3 normal
execute at @s[scores={eruption_time=1..}] run particle falling_lava ~ ~0.5 ~ 0.5 0.2 0.5 0.01 3 normal
execute at @s[scores={eruption_time=1..}] run particle dust 0.090 0.027 0.000 1 ~ ~0.2 ~ 0.3 0.3 0.3 0.1 10 normal

execute at @s[scores={eruption_time=5..}] if score #rc4tick tick20 matches 1 run function skills:sword/eruption_damage

execute at @s[tag=eruption_active,scores={eruption_time=20}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.1,0.3,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=30}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.3,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=50}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.1,0.25,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=65}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.0,0.4,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=90}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.0,0.3,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=90}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.0,0.3,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=100}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.05,0.3,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=130}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.4,0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=150}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.15,0.25,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=160}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.1,0.3,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=175}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.05,0.25,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s[tag=eruption_active,scores={eruption_time=190}] run summon snowball ~ ~0.5 ~ {Fire:500,HasVisualFire:1b,Motion:[0.1,0.25,0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}

kill @s[scores={eruption_time=200..}]