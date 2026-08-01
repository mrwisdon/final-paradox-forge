scoreboard players add @s chal_mana_spent 20

effect give @s absorption 8 0 false
effect give @s fire_resistance 8 0 false

tag @s add magma_shield_attack

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run particle lava ~ ~0.5 ~ 1.5 1 1.5 0.2 70 normal
execute at @s run particle flame ~ ~0.5 ~ 1.5 1 1.5 0.2 150 normal
execute at @s run particle explosion ~ ~0.5 ~ 0.7 0.1 0.7 0.1 8 normal

execute at @s run playsound minecraft:item.trident.throw master @a[distance=..20] ~ ~ ~ 0.5 0.7 0.5
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..10] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..20] ~ ~ ~ 0.6 1.2 0.6
execute at @s run playsound minecraft:entity.evoker.cast_spell master @a[distance=..10] ~ ~ ~ 0.3 1.2 0.3
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..20] ~ ~ ~ 0.6 0.9 0.6
execute at @s run playsound entity.shulker_bullet.hit master @a[distance=..10] ~ ~ ~ 0.3 0.9 0.3
execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..20] ~ ~ ~ 0.3 0.7 0.3
execute at @s run playsound minecraft:entity.blaze.shoot master @a[distance=..10] ~ ~ ~ 0.15 1.2 0.15

execute at @s run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..7] run function skills:sword/magma_shield_hit

tag @s remove magma_shield_attack

execute at @s[predicate=general:biome_snowy_taiga] run function area:green_crystal/melt_4x3