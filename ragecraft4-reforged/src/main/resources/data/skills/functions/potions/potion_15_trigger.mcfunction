execute at @s run particle sculk_charge_pop ~ ~0.5 ~ 0.7 0.4 0.7 0.1 100 normal
execute at @s run particle cloud ~ ~0.5 ~ 0.8 0.3 0.8 0.05 25 normal
execute at @s run particle dust 0.675 0.953 0.847 1 ~ ~0.5 ~ 1.2 1 1.2 0.5 150 normal

execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.5 0.8 0.5
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound entity.llama.spit master @a[distance=..20] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound entity.llama.spit master @a[distance=..10] ~ ~ ~ 0.3 0.5 0.3

execute at @s run execute as @e[type=#minecraft:arthropod,tag=!spell_immune,distance=..4] run function skills:potions/potion_15_hit
execute at @s if entity @e[type=#minecraft:arthropod,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=21}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

execute at @s run execute as @p[scores={potion_selected=21},distance=..30] run function skills:potions/potion_15_buff