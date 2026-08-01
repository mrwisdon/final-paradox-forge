execute at @s run particle smoke ~ ~0.2 ~ 0.7 1.2 0.7 0.05 150 normal
execute at @s run particle sculk_charge_pop ~ ~0.2 ~ 0.8 0.4 0.8 0.1 80 normal
execute at @s run particle squid_ink ~ ~0.2 ~ 0.3 0.3 0.3 0.1 10 normal
execute at @s run particle totem_of_undying ~ ~0.5 ~ 0.8 0.3 0.8 0.3 100 normal
execute at @s run particle dust 0.196 0.267 0.153 1 ~ ~0.2 ~ 1.3 1 1.3 0.5 250 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.5 0.5 0.5
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.5 0.3
execute at @s run playsound entity.dolphin.splash master @a[distance=..20] ~ ~ ~ 0.2 1.2 0.2
execute at @s run playsound entity.dolphin.splash master @a[distance=..10] ~ ~ ~ 0.1 1.2 0.1

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_4_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=4}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]