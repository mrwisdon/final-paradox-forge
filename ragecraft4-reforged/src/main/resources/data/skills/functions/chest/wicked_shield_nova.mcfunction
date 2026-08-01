tag @s add wicked_shield_attack

execute positioned ^ ^0.5 ^2.5 run summon marker ~ ~0.7 ~ {Tags:["wicked_shield_center"]}
execute as @e[type=marker,tag=wicked_shield_center] run function skills:chest/wicked_shield_anim

execute positioned ^ ^0.5 ^2.5 run particle explosion ~ ~0.4 ~ 0 0 0 0.1 1 normal

playsound minecraft:entity.lightning_bolt.impact master @a[distance=..18] ~ ~ ~ 0.15 1.3 0.15
playsound minecraft:entity.zoglin.step master @a[distance=..18] ~ ~ ~ 1 0.8 1
playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 1 1.3 1

execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:0b}]}] positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,distance=..3.5] run function skills:chest/wicked_shield_hit_1
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:1b}]}] positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,distance=..3.5] run function skills:chest/wicked_shield_hit_2
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:2b}]}] positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,distance=..3.5] run function skills:chest/wicked_shield_hit_3
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:3b}]}] positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,distance=..3.5] run function skills:chest/wicked_shield_hit_4
execute at @s[nbt={ActiveEffects:[{Id:5,Amplifier:4b}]}] positioned ^ ^ ^2.5 run execute as @e[tag=r4r_ability_target,distance=..3.5] run function skills:chest/wicked_shield_hit_5

tag @s remove wicked_shield_attack