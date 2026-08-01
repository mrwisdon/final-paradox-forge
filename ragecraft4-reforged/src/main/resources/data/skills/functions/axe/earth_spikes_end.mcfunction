playsound minecraft:entity.iron_golem.damage master @a[distance=..20] ~ ~ ~ 0.3 0.8 0.3
playsound minecraft:entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.4 0.8 0.4

fill ~ ~ ~ ~ ~2 ~ air replace pointed_dripstone

particle campfire_cosy_smoke ~ ~0.2 ~ 0.3 0.3 0.3 0.01 20 normal
particle block dripstone_block ~ ~0.5 ~ 0.7 0.5 0.7 0.1 250 normal

execute at @s run execute as @e[tag=r4r_ability_target,tag=!earth_spikes_hit,distance=..3] at @s run function skills:axe/earth_spikes_hit

kill @e[type=item,distance=..10,nbt={Item:{id:"minecraft:pointed_dripstone"}}]

kill @s