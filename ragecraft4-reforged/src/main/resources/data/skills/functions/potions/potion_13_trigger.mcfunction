execute at @s run particle smoke ~ ~0.1 ~ 1.5 0.2 1.5 0.01 150 force
execute at @s run particle squid_ink ~ ~0.5 ~ 0.5 0.5 0.5 0.1 30 force
execute at @s run particle electric_spark ~ ~0.3 ~ 0.8 0.5 0.8 0.1 200 force
execute at @s run particle dust 1 0.976 0.855 1 ~ ~0.5 ~ 1 1 1 0.5 400 force
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.5 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.5 0.2
execute at @s run playsound block.fire.extinguish master @a[distance=..20] ~ ~ ~ 0.25 1.5 0.25
execute at @s run playsound block.fire.extinguish master @a[distance=..10] ~ ~ ~ 0.15 1.5 0.15

execute at @s run summon lightning_bolt ~ ~1 ~

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..4] run function skills:potions/potion_13_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..4] run tellraw @a[scores={potion_selected=13}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]