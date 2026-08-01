scoreboard players remove @s arcane_mom 1
scoreboard players set @s[predicate=!skills:arcane_momentum] arcane_mom 0

effect give @s speed 1 1 true

execute at @s run particle warped_spore ~ ~0.2 ~ 0.6 0.3 0.6 0.01 5 normal
execute at @s run particle squid_ink ~ ~0.1 ~ 0.2 0.2 0.2 0.01 1 normal

execute at @s run execute as @e[tag=r4r_ability_target,distance=..7] at @s run function skills:chest/arcane_mom_mob