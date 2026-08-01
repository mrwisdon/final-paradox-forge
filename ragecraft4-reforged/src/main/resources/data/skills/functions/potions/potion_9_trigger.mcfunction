execute at @s run particle squid_ink ~ ~0.3 ~ 0.3 0.3 0.3 0.1 12 normal
execute at @s run particle block nether_wart_block ~ ~0.3 ~ 1.2 0.5 1.2 0.2 300 normal
execute at @s run particle dust 0.239 0.051 0.051 1 ~ ~0.5 ~ 1 1 1 0.5 250 normal
execute at @s run particle dust 0.345 0.039 0.039 1 ~ ~0.4 ~ 0.3 0.6 0.3 0.2 250 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.5 0.3
execute at @s run playsound block.beehive.enter master @a[distance=..20] ~ ~ ~ 0.5 1 0.5
execute at @s run playsound block.beehive.enter master @a[distance=..10] ~ ~ ~ 0.3 1 0.3

execute at @s run summon snowball ~0.3 ~0.3 ~0.3 {Motion:[0.1,0.4,0.1],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_wart_block",Count:1b}}
execute at @s run summon snowball ~-0.3 ~0.3 ~ {Motion:[-0.1,0.45,0.05],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_wart_block",Count:1b}}
execute at @s run summon snowball ~ ~0.3 ~-0.3 {Motion:[0.02,0.35,-0.1],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_wart_block",Count:1b}}
execute at @s run summon snowball ~-0.3 ~0.3 ~-0.3 {Motion:[-0.05,0.4,-0.12],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_wart_block",Count:1b}}
execute at @s run summon snowball ~0.3 ~0.3 ~-0.3 {Motion:[0.1,0.35,-0.05],Tags:["blood","marker_tick"],Item:{id:"minecraft:nether_wart_block",Count:1b}}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_9_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=9}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]