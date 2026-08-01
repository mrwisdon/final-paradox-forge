execute at @s run particle splash ~ ~0.8 ~ 1.2 0.8 1.2 0.1 600 normal
execute at @s run particle dust 0.251 0.776 1.000 1 ~ ~0.5 ~ 1.2 0.8 1.2 0.1 150 normal
execute at @s run particle dust 0.106 0.475 0.631 1 ~ ~0.2 ~ 0.3 0.3 0.3 0.1 200 normal
execute at @s run playsound entity.dolphin.splash master @a[distance=..15] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound entity.dolphin.splash master @a[distance=..8] ~ ~ ~ 0.2 1.2 0.2

execute at @s run kill @e[type=armor_stand,distance=..4,tag=reptilian_abom,scores={cd=1..}]
execute at @s run kill @e[type=armor_stand,distance=..4,tag=metamorph_baby_3]
execute at @s run kill @e[type=marker,distance=..4,tag=crypt_keeper_pool]
execute at @s run kill @e[type=marker,distance=..4,tag=phantom_bomb_x]
execute at @s run kill @e[type=armor_stand,distance=..4,tag=obsidian_tear]

execute at @s run execute as @p[distance=..4] run function skills:potions/potion_2_hit