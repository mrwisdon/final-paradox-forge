execute at @s run particle portal ~ ~0.3 ~ 0.8 0.8 0.8 0.1 300 normal
execute at @s run particle squid_ink ~ ~0.3 ~ 0.3 0.3 0.3 0.1 20 normal
execute at @s run particle witch ~ ~0.3 ~ 1 0.3 1 0.02 150 normal
execute at @s run particle dust 0.522 0.42 0.655 1 ~ ~0.5 ~ 1.5 1 1.5 0.5 300 normal
execute at @s run particle dust 0.243 0.118 0.408 1 ~ ~0.5 ~ 0.3 1 0.3 0.5 200 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.5 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.25 0.5 0.25
execute at @s run playsound entity.squid.squirt master @a[distance=..20] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound entity.squid.squirt master @a[distance=..10] ~ ~ ~ 0.2 0.7 0.2

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_12_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=12}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]