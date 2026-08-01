execute at @s run kill @e[type=snowball,tag=spell_proj_7,distance=..5]

execute at @s run particle dust 0.184 0.008 0.333 1 ~ ~0.5 ~ 2 1.2 2 0.01 1500 force
execute at @s run particle squid_ink ~ ~0.5 ~ 1.2 0.8 1.2 0.1 150 force
execute at @s run particle squid_ink ~ ~2 ~ 0.2 5 0.2 0.1 150 force
execute at @s run particle portal ~ ~0.5 ~ 2.5 0.5 2.5 1 900 force
execute at @s run playsound minecraft:item.totem.use master @a[distance=..30] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 0.4 0.5 0.4
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..30] ~ ~ ~ 0.15 1.4 0.15
execute at @s run playsound minecraft:item.totem.use master @a[distance=..18] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.2 0.5 0.2
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..18] ~ ~ ~ 0.1 1.4 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..10] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.2 0.5 0.2
execute at @s run playsound entity.warden.sonic_boom master @a[distance=..10] ~ ~ ~ 0.1 1.4 0.1

execute at @s run summon marker ~ ~ ~ {Tags:["spell_7_anim"]}
execute as @e[type=marker,tag=spell_7_anim] run function skills:spells/spell_7_anim

execute at @s[tag=!spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..7] run function skills:spells/spell_7_hit
execute at @s[tag=spell_power] run execute as @e[tag=r4r_ability_target,tag=!spell_immune,distance=..7] run function skills:spells/spell_7_hit2
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..7] run tellraw @a[scores={last_spell=7}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

kill @s