execute at @s run kill @e[type=snowball,tag=spell_proj_3,distance=..5]

execute at @s run particle explosion ~ ~0.3 ~ 0.8 0.3 0.8 1 5 force
execute at @s run particle block frosted_ice ~ ~0.2 ~ 1.2 0.5 1.2 0.05 300 force
execute at @s run particle snowflake ~ ~0.2 ~ 1.6 0.3 1.6 0.2 150 force
execute at @s run particle dust 0.816 0.957 1 1 ~ ~0.1 ~ 1.5 0.8 1.5 0.2 700 force
execute at @s run particle dust 0.349 0.827 0.973 1 ~ ~0.1 ~ 0.6 0.9 0.6 0.2 400 force

execute at @s run playsound entity.glow_squid.squirt master @a[distance=..30] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound block.glass.break master @a[distance=..30] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..30] ~ ~ ~ 0.8 1.2 0.8

execute at @s run playsound entity.glow_squid.squirt master @a[distance=..18] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound block.glass.break master @a[distance=..18] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..18] ~ ~ ~ 0.5 1.2 0.5

execute at @s run playsound entity.glow_squid.squirt master @a[distance=..10] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound block.glass.break master @a[distance=..10] ~ ~ ~ 0.2 0.8 0.1
execute at @s run playsound block.amethyst_cluster.break master @a[distance=..10] ~ ~ ~ 0.5 1.2 0.5

execute at @s run summon snowball ~0.1 ~0.8 ~0.1 {Motion:[0.5,0.4,0.5],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.7 ~ {Motion:[-0.55,0.45,0.3],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.8 ~-0.1 {Motion:[0.1,0.35,-0.5],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.9 ~-0.1 {Motion:[-0.3,0.4,-0.6],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~0.1 ~0.8 ~-0.1 {Motion:[0.5,0.35,-0.25],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~0.25 ~1 ~0.15 {Motion:[0.8,0.5,1.0],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~-0.2 ~0.8 ~0.05 {Motion:[-1.0,0.5,0.3],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~0.3 ~0.8 ~-0.2 {Motion:[0.3,0.55,-0.9],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~-0.15 ~1.1 ~-0.2 {Motion:[-0.5,0.6,-1.1],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~0.05 ~0.9 ~-0.25 {Motion:[0.7,0.55,-1.0],Tags:["frostbolt","marker_tick"],Item:{id:"minecraft:packed_ice",Count:1b}}

execute at @s[tag=!spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] run function skills:spells/spell_3_hit
execute at @s[tag=spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] run function skills:spells/spell_3_hit2
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..5] run tellraw @a[scores={last_spell=3}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

kill @s