tag @s add spell_echo_att

execute at @s run particle explosion ~3 ~ ~ 0.5 0.1 0.5 1 2 normal
execute at @s run particle explosion ~-3 ~ ~ 0.5 0.1 0.5 1 2 normal
execute at @s run particle explosion ~ ~ ~3 0.5 0.1 0.5 1 2 normal
execute at @s run particle explosion ~ ~ ~-3 0.5 0.1 0.5 1 2 normal
execute at @s run particle flash ~ ~ ~ 0.1 0.1 0.1 1 1 normal
execute at @s run particle dragon_breath ~ ~1 ~ 1 0.5 1 0.05 150 normal
execute at @s run particle enchanted_hit ~ ~1 ~ 1 0.5 1 0.05 250 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 1 0.8 1
execute at @s run playsound minecraft:entity.generic.explode master @a[distance=..30] ~ ~ ~ 0.3 1.3 0.3
execute at @s run playsound minecraft:entity.glow_squid.hurt master @a[distance=..30] ~ ~ ~ 0.5 1.3 0.5

execute at @s run summon marker ~ ~1 ~ {Tags:["spell_echo_anim"]}
execute as @e[type=marker,tag=spell_echo_anim] run function skills:helmet/spell_echo_anim

execute at @s run execute as @e[tag=r4r_ability_target,distance=..8] run function skills:helmet/spell_echo_hit

tag @s remove spell_echo_att