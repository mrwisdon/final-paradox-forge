execute at @s run particle explosion ~ ~0.3 ~ 0.2 0.2 0.2 1 3 normal
execute at @s run particle minecraft:lava ~ ~0.8 ~ 0.3 0.3 0.3 0.2 20 normal
execute at @s run particle dust_color_transition 0.322 0.000 0.149 1 0.149 0.012 0.039 ~ ~0.3 ~ 0.7 1.2 0.7 0.01 300 normal
execute at @s run particle flame ~ ~0.3 ~ 0.5 0.8 0.5 0.1 80 normal
execute at @s run particle squid_ink ~ ~0.3 ~ 0.3 0.3 0.3 0.1 5 normal
execute at @s run particle falling_lava ~ ~0.1 ~ 1 0.5 1 0.2 125 normal
execute at @s run playsound entity.generic.explode master @a[distance=..20] ~ ~ ~ 0.15 1.1 0.15
execute at @s run playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.1 1.1 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.5 0.3

execute at @s run summon snowball ~0.1 ~0.2 ~0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.4,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:red_stained_glass",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.2 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.45,0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:red_stained_glass",Count:1b}}
execute at @s run summon snowball ~ ~0.2 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.02,0.35,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:red_stained_glass",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.2 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[-0.05,0.4,-0.12],Tags:["eruption","marker_tick"],Item:{id:"minecraft:red_stained_glass",Count:1b}}
execute at @s run summon snowball ~0.1 ~0.2 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.35,-0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:red_stained_glass",Count:1b}}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_3_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=3}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_3x2