execute at @s run particle explosion ~ ~0.2 ~ 0.1 0.1 0.1 1 1 normal
execute at @s run particle minecraft:lava ~ ~0.6 ~ 0.3 0.3 0.3 0.2 30 normal
execute at @s run particle dust_color_transition 0.322 0.000 0.149 1 0.149 0.012 0.039 ~ ~0.2 ~ 0.7 1.2 0.7 0.01 300 normal
execute at @s run particle flame ~ ~0.2 ~ 0.5 1 0.5 0.1 120 normal
execute at @s run particle squid_ink ~ ~1.5 ~ 0.3 3 0.3 0.01 50 normal
execute at @s run particle squid_ink ~ ~0.2 ~ 1.5 0.2 1.5 0.01 30 normal
execute at @s run particle falling_lava ~ ~0.2 ~ 1.3 0.5 1.3 0.2 400 normal
execute at @s run playsound minecraft:item.totem.use master @a[distance=..20] ~ ~ ~ 0.15 1.2 0.15
execute at @s run playsound minecraft:item.totem.use master @a[distance=..10] ~ ~ ~ 0.2 1.2 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.5 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.5 0.2

execute at @s run summon marker ~ ~ ~ {Tags:["potion_10_anim"]}
execute as @e[type=marker,tag=potion_10_anim] run function skills:potions/potion_10_anim

execute at @s run summon armor_stand ~ ~ ~ {Silent:1b,Invulnerable:1b,Small:1b,Invisible:1b,Tags:["marker_tick","dreadfire_potion"]}

execute at @s run summon snowball ~0.1 ~0.3 ~0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.4,0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.3 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.45,0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~ ~0.3 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.02,0.35,-0.1],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.3 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[-0.05,0.4,-0.12],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~0.1 ~0.3 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.35,-0.05],Tags:["eruption","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] run function skills:potions/potion_10_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..5] run tellraw @a[scores={potion_selected=10}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_3x2