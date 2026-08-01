execute at @s run particle dust 0.800 0.925 1.000 1 ~ ~0.6 ~ 2.5 0.4 2.5 0.1 600 normal
execute at @s run particle block blue_ice ~ ~0.6 ~ 2 0.4 2 0.5 400 normal
execute at @s run particle item_snowball ~ ~0.6 ~ 2.5 0.4 2.5 0.2 600 normal

execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..22] ~ ~ ~ 0.8 1.2 0.8
execute at @s run playsound block.glass.break master @a[distance=..22] ~ ~ ~ 0.2 0.7 0.2
execute at @s run playsound entity.glow_squid.death master @a[distance=..22] ~ ~ ~ 0.3 1.7 0.3
execute at @s run playsound entity.generic.explode master @a[distance=..22] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.5 1.2 0.5
execute at @s run playsound block.glass.break master @a[distance=..10] ~ ~ ~ 0.1 0.7 0.1
execute at @s run playsound entity.glow_squid.death master @a[distance=..10] ~ ~ ~ 0.15 1.7 0.15
execute at @s run playsound entity.generic.explode master @a[distance=..10] ~ ~ ~ 0.15 1.2 0.15

execute at @s run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..8] run function skills:sword/trinity_ice_hit

execute at @s run summon marker ~ ~0.5 ~ {Tags:["trinity_ice_anim"]}
execute as @e[type=marker,tag=trinity_ice_anim] run function skills:sword/trinity_ice_anim

item modify entity @s[predicate=skills:model_5] weapon.mainhand skills:model_1
item modify entity @s[predicate=skills:model_2] weapon.mainhand skills:model_1
item modify entity @s[predicate=skills:model_15] weapon.mainhand skills:model_11
item modify entity @s[predicate=skills:model_12] weapon.mainhand skills:model_11
item modify entity @s[predicate=skills:model_25] weapon.mainhand skills:model_21
item modify entity @s[predicate=skills:model_22] weapon.mainhand skills:model_21
item modify entity @s[predicate=skills:model_35] weapon.mainhand skills:model_31
item modify entity @s[predicate=skills:model_32] weapon.mainhand skills:model_31
item modify entity @s[predicate=skills:model_45] weapon.mainhand skills:model_41
item modify entity @s[predicate=skills:model_42] weapon.mainhand skills:model_41
item modify entity @s[predicate=skills:model_55] weapon.mainhand skills:model_51
item modify entity @s[predicate=skills:model_52] weapon.mainhand skills:model_51

tag @s remove trinity_attack