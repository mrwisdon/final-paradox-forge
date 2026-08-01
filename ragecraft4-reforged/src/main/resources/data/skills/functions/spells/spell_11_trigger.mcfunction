execute at @s run kill @e[type=snowball,tag=spell_proj_11,distance=..5]

execute at @s run particle explosion ~ ~1 ~ 1.2 0.4 1.2 1 10 force
execute at @s run particle end_rod ~ ~0.2 ~ 2 1.4 2 0.05 150 force
execute at @s run particle dust 1 0.941 0.6 1 ~ ~0.1 ~ 1.8 0.8 1.8 0.1 1200 force
execute at @s run particle dust 1 0.976 0.839 1 ~ ~2 ~ 0.8 4.2 0.8 0.1 1200 force
execute at @s run playsound entity.evoker.prepare_attack master @a[distance=..30] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..30] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound entity.firework_rocket.twinkle_far master @a[distance=..30] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound entity.evoker.prepare_attack master @a[distance=..18] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..18] ~ ~ ~ 0.2 1.3 0.2
execute at @s run playsound entity.firework_rocket.twinkle_far master @a[distance=..18] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound entity.evoker.prepare_attack master @a[distance=..10] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..10] ~ ~ ~ 0.2 1.3 0.2
execute at @s run playsound entity.firework_rocket.twinkle_far master @a[distance=..10] ~ ~ ~ 0.2 0.8 0.2

execute at @s run summon snowball ~ ~1 ~ {Motion:[0.0,1.0,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~2 ~1 ~2 {Motion:[0.0,0.85,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~2 ~1 ~-2 {Motion:[0.0,0.85,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~-2 ~1 ~2 {Motion:[0.0,0.85,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~-2 ~1 ~-2 {Motion:[0.0,0.85,-0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~3.5 ~1 ~3.5 {Motion:[0.0,0.6,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~3.5 ~1 ~-3.5 {Motion:[0.0,0.6,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~-3.5 ~1 ~3.5 {Motion:[0.0,0.6,-0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~-3.5 ~1 ~-3.5 {Motion:[0.0,0.6,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~3 ~1 ~ {Motion:[0.0,0.7,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~-3 ~1 ~-3.5 {Motion:[0.0,0.7,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~ ~1 ~3 {Motion:[0.0,0.7,-0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}
execute at @s run summon snowball ~ ~1 ~-3 {Motion:[0.0,0.7,0.0],Tags:["consecration","marker_tick"],Item:{id:"minecraft:glowstone",Count:1b}}

execute at @s[tag=!spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..6] run function skills:spells/spell_11_hit
execute at @s[tag=spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..6] run function skills:spells/spell_11_hit2
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..6] run tellraw @a[scores={last_spell=11}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

execute at @s run summon marker ~ ~ ~ {Tags:["marker_tick","consecration"]}

kill @s