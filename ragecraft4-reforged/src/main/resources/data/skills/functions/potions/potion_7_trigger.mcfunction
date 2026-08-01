execute at @s run particle smoke ~ ~0.3 ~ 0.7 1.2 0.7 0.1 150 normal
execute at @s run particle witch ~ ~0.3 ~ 1 0.4 1 0.1 100 normal
execute at @s run particle squid_ink ~ ~2 ~ 0.4 2 0.4 0.01 20 normal
execute at @s run particle dust 0.114 0.035 0.192 1 ~ ~0.3 ~ 1.4 1 1.7 0.5 500 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.5 0.3
execute at @s run playsound minecraft:item.totem.use master @a[distance=..20] ~ ~ ~ 0.15 1.2 0.15
execute at @s run playsound minecraft:item.totem.use master @a[distance=..10] ~ ~ ~ 0.1 1.2 0.1

execute at @s run summon marker ~ ~ ~ {Tags:["potion_7_anim"]}
execute as @e[type=marker,tag=potion_7_anim] run function skills:potions/potion_7_anim

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_7_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=7}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]