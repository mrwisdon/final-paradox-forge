scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run particle explosion ^ ^1 ^3 1 0.2 1 0.02 5 normal
execute at @s run particle campfire_cosy_smoke ^ ^1 ^3 1 0.5 1 0.03 50 normal
execute at @s run particle lava ^ ^1 ^3 1.5 0.6 1.5 0.3 50 normal
#execute at @s run particle flame ^ ^1 ^3 1.2 0.5 1.2 0.2 150 normal
execute at @s run particle falling_lava ^ ^1 ^3 1.5 0.6 1.5 0.3 600 normal
execute at @s run playsound entity.generic.explode master @a[distance=..18] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound block.end_gateway.spawn master @a[distance=..18] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..18] ~ ~ ~ 0.1 0.8 0.1
execute at @s run playsound entity.generic.explode master @a[distance=..9] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound block.end_gateway.spawn master @a[distance=..9] ~ ~ ~ 0.1 1.2 0.1
execute at @s run playsound minecraft:item.totem.use master @a[distance=..9] ~ ~ ~ 0.1 0.8 0.1

execute at @s run summon marker ^ ^1 ^3 {Tags:["flame_quake_anim"]}
execute as @e[type=marker,tag=flame_quake_anim] run function skills:axe/flame_quake_anim

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5] run function skills:axe/flame_quake_hit
execute at @s[predicate=general:biome_snowy_taiga] positioned ^ ^0.5 ^3 run function area:green_crystal/melt_4x3