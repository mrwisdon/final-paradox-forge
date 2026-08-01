execute at @s run kill @e[type=snowball,tag=spell_proj_12,distance=..5]

execute at @s run particle explosion ~ ~1 ~ 0.1 0.1 0.1 1 1 force
execute at @s run particle squid_ink ~ ~1.5 ~ 0.8 2.5 0.8 0.05 150 force
execute at @s run particle dust 0 0.004 0.122 1 ~ ~0.1 ~ 1 0.8 1 0.1 800 force
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..25] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..25] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..15] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..15] ~ ~ ~ 0.2 0.8 0.2
execute at @s run playsound minecraft:entity.dragon_fireball.explode master @a[distance=..8] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound minecraft:entity.phantom.bite master @a[distance=..8] ~ ~ ~ 0.2 0.8 0.2

execute at @s[tag=!spell_power] positioned ~ ~-1 ~ run execute as @e[tag=r4r_ability_target,tag=!spell_immune,limit=1,sort=nearest,distance=..2] run function skills:spells/spell_12_hit
execute at @s[tag=spell_power] positioned ~ ~-1 ~ run execute as @e[tag=r4r_ability_target,tag=!spell_immune,limit=1,sort=nearest,distance=..2] run function skills:spells/spell_12_hit2
execute at @s positioned ~ ~-1 ~ if entity @e[tag=r4r_ability_target,tag=spell_immune,distance=..2] run tellraw @a[scores={last_spell=12}] [{"translate":"skills.functions.potions.potion_10_trigger.1","color":"red","bold":true,"italic":false},{"translate":"skills.functions.potions.potion_10_trigger.2","color":"#FF662E","bold":false,"italic":false}]

kill @s