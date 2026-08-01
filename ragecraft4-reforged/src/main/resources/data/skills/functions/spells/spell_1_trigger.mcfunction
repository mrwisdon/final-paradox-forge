execute at @s run kill @e[type=snowball,tag=spell_proj_1,distance=..5]

execute at @s run particle explosion ~ ~1 ~ 0.8 0.3 0.8 1 5 force
execute at @s run particle minecraft:lava ~ ~1 ~ 1 0.5 1 0.2 120 force
execute at @s run particle dust 0.561 0.133 0.024 1 ~ ~1 ~ 1.8 0.7 1.8 0.01 800 force
execute at @s run particle flame ~ ~1 ~ 0.5 1.8 0.5 0.1 250 force
execute at @s run particle squid_ink ~ ~1 ~ 1 0.8 1 0.1 50 force
execute at @s run particle falling_lava ~ ~1 ~ 2 0.5 2 0.2 350 force
execute at @s run playsound entity.generic.explode master @a[distance=..30] ~ ~ ~ 0.2 1.1 0.2
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound entity.generic.explode master @a[distance=..18] ~ ~ ~ 0.15 1.1 0.15
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.3 0.5 0.3
execute at @s run playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.1 1.1 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.5 0.2

execute at @s run summon snowball ~0.1 ~0.8 ~0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.4,0.1],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.7 ~ {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.45,0.05],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~ ~0.8 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.02,0.35,-0.1],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.1 ~0.9 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[-0.05,0.4,-0.12],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~0.1 ~0.8 ~-0.1 {Fire:500,HasVisualFire:1b,Motion:[0.1,0.35,-0.05],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~0.25 ~1 ~0.15 {Fire:500,HasVisualFire:1b,Motion:[0.15,0.5,0.2],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.2 ~0.8 ~0.05 {Fire:500,HasVisualFire:1b,Motion:[-0.2,0.5,0.05],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~0.3 ~0.8 ~-0.2 {Fire:500,HasVisualFire:1b,Motion:[0.05,0.55,-0.18],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~-0.15 ~1.1 ~-0.2 {Fire:500,HasVisualFire:1b,Motion:[-0.1,0.6,-0.25],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}
execute at @s run summon snowball ~0.05 ~0.9 ~-0.25 {Fire:500,HasVisualFire:1b,Motion:[0.15,0.55,-0.2],Tags:["fireball","marker_tick"],Item:{id:"minecraft:magma_block",Count:1b}}

execute at @s[tag=!spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] run function skills:spells/spell_1_hit
execute at @s[tag=spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..5] run function skills:spells/spell_1_hit2
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..5] run tellraw @a[scores={last_spell=1}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_4x3

kill @s