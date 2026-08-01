execute at @s run kill @e[type=snowball,tag=spell_proj_5,distance=..1]

execute at @s run particle explosion ~ ~1 ~ 0.2 0.2 0.2 1 1 normal
execute at @s run particle totem_of_undying ~ ~0.3 ~ 0.6 0.8 0.6 0.2 50 normal
execute at @s run particle sneeze ~ ~0.3 ~ 0.6 0.8 0.6 0.02 100 normal
execute at @s run particle squid_ink ~ ~0.3 ~ 0.2 0.5 0.2 0.2 20 normal
execute at @s run particle dust 0.224 0.306 0.169 1 ~ ~0.3 ~ 0.5 1.2 0.5 0.2 300 normal
execute at @s run particle dust 0.125 0.188 0.098 1 ~ ~0.3 ~ 0.6 0.6 0.6 0.2 150 normal
execute at @s run playsound block.frogspawn.place master @a[distance=..30] ~ ~ ~ 0.4 0.8 0.4
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..30] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound block.frogspawn.place master @a[distance=..18] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..18] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound block.frogspawn.place master @a[distance=..10] ~ ~ ~ 0.3 0.8 0.3
execute at @s run playsound minecraft:entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.1 0.8 0.1

execute at @s if entity @e[tag=r4r_ability_target,tag=!spell_immune,distance=..3] run function skills:spells/spell_5_target
execute at @s if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..3] run tellraw @a[scores={last_spell=5}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

kill @s