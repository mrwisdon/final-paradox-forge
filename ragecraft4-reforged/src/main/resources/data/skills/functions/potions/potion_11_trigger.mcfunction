execute at @s run particle spit ~ ~0.5 ~ 0.7 1.7 0.7 0.1 150 normal
execute at @s run particle squid_ink ~ ~0.5 ~ 0.4 0.3 0.4 0.1 12 normal
execute at @s run particle dust 0.831 0.831 0.831 1 ~ ~0.5 ~ 1.5 0.7 1.5 0.5 250 normal
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.4 0.5 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..9] ~ ~ ~ 0.25 0.5 0.25
execute at @s run playsound minecraft:entity.squid.squirt master @a[distance=..18] ~ ~ ~ 0.3 0.6 0.3
execute at @s run playsound minecraft:entity.squid.squirt master @a[distance=..9] ~ ~ ~ 0.15 0.6 0.15

execute at @s run summon marker ~ ~ ~ {Tags:["marker_tick","cyclone_anim"]}

execute at @s run execute as @e[tag=r4r_ability_target,tag=!spell_immune,tag=!elite,distance=..5] run function skills:potions/potion_11_hit
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..5] run tellraw @a[scores={potion_selected=11}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]