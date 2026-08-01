scoreboard players add @s chal_mana_spent 20

scoreboard players remove @s mana 20
effect give @s[predicate=skills:mana_shield] absorption 8 1 true
effect give @s[predicate=skills:determination] strength 6 1 true
effect give @s[predicate=skills:divine_bulwark] resistance 6 1 true
scoreboard players set @s[predicate=skills:blood_pact] blood_pact_cd 15
scoreboard players set @s[predicate=skills:a_arcanist] arcanist_timer 200
effect give @s[predicate=skills:vt_domination] strength 6 3 true
effect give @s[predicate=skills:vt_domination] absorption 6 0 true

execute at @s run summon marker ~ ~-0.6 ~ {Tags:["l_warp_anim"]}
execute as @e[type=marker,tag=l_warp_anim] run function skills:axe/l_warp_anim

execute at @s run playsound minecraft:entity.illusioner.mirror_move master @a[distance=..20] ~ ~ ~ 1 1.2 1
execute at @s run playsound entity.shulker.teleport master @a[distance=..15] ~ ~ ~ 0.8 0.8 0.8
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..20] ~ ~ ~ 0.3 1.2 0.3
execute at @s run particle dust 0.988 0.984 0.725 1 ~ ~1 ~ 0.9 0.8 0.9 0.05 300 force
execute at @s run particle electric_spark ~ ~1 ~ 1.3 0.8 1.3 0.01 500 force
execute at @s run particle flash ~ ~1 ~ 1.5 0.5 1.5 0.5 8 force
execute at @s run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5] run function skills:axe/l_warp_hit