execute at @s run particle smoke ~ ~0.3 ~ 0.8 0.4 0.8 0.2 60 normal
execute at @s run particle sculk_soul ~ ~0.3 ~ 0.7 1.2 0.7 0.1 150 normal
execute at @s run particle squid_ink ~ ~0.1 ~ 0.5 0.5 0.5 0.3 100 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.7 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound entity.wither.hurt master @a[distance=..20] ~ ~ ~ 0.15 0.7 0.15
execute at @s run playsound entity.wither.hurt master @a[distance=..10] ~ ~ ~ 0.1 0.7 0.1

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_6_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=6}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]