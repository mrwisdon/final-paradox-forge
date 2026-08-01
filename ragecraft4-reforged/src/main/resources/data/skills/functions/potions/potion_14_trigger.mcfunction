execute at @s run particle dust 0.824 0.89 0.941 1 ~ ~0.5 ~ 1.2 0.8 1.2 0.3 200 normal
execute at @s run particle dust 0.6 0.729 0.827 1 ~ ~0.5 ~ 0.5 0.5 0.5 0.3 80 normal
execute at @s run particle block blue_ice ~ ~0.5 ~ 1.2 0.8 1.2 0.5 100 normal
execute at @s run particle item_snowball ~ ~0.5 ~ 1.5 0.8 1.5 0.2 200 normal

execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..22] ~ ~ ~ 0.5 1.2 0.5
execute at @s run playsound block.glass.break master @a[distance=..22] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound entity.glow_squid.death master @a[distance=..22] ~ ~ ~ 0.2 1.7 0.2
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound block.glass.break master @a[distance=..10] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound entity.glow_squid.death master @a[distance=..10] ~ ~ ~ 0.1 1.7 0.1

execute at @s run summon marker ~ ~ ~ {Tags:["potion_14_anim"]}
execute as @e[type=marker,tag=potion_14_anim] run function skills:potions/potion_14_anim

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..3.5] at @s unless entity @s[tag=frozen] run function skills:potions/potion_14_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..3.5] run tellraw @a[scores={potion_selected=14}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]