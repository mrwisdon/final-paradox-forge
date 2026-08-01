execute at @s run particle dust 0.710 0.875 1.000 1 ~ ~0.5 ~ 1.2 0.8 1.2 0.3 300 force
execute at @s run particle dust 0.447 0.69 0.875 1 ~ ~0.5 ~ 0.5 0.5 0.5 0.3 80 force
execute at @s run particle enchanted_hit ~ ~0.3 ~ 1 0.6 1 0.5 150 force
execute at @s run particle spit ~ ~0.3 ~ 0.3 0.3 0.3 0.1 10 force
execute at @s run playsound entity.squid.squirt master @a[distance=..20] ~ ~ ~ 0.3 1.5 0.3
execute at @s run playsound entity.squid.squirt master @a[distance=..10] ~ ~ ~ 0.15 1.5 0.15
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.7 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.7 0.2

execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[0.5,0.2,0.5],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[0.3,0.25,0.6],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[-0.5,0.25,0.4],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[0.45,0.3,-0.2],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[-0.1,0.2,0.3],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[0.5,0.15,0.0],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[-0.4,0.3,-0.4],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[-0.2,0.2,-0.6],Item:{id:"minecraft:packed_ice",Count:1b}}
execute at @s run summon snowball ~ ~0.1 ~ {Tags:["ice","marker_tick"],Motion:[-0.4,0.3,-0.1],Item:{id:"minecraft:packed_ice",Count:1b}}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_5_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=5}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]