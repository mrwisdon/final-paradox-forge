scoreboard players remove @s frost_ray_cd 1

execute at @s positioned ^0.4 ^ ^1.2 run tp @e[type=armor_stand,tag=frost_ray_anim] ~ ~ ~

effect give @s[scores={frost_ray_cd=20..}] slowness 1 2 true

execute at @s[scores={frost_ray_cd=139}] run function skills:spells/spell_10_start

execute at @s[scores={frost_ray_cd=..135}] run playsound entity.glow_squid.squirt master @a[distance=..15] ~ ~ ~ 0.1 1.4 0.1
execute at @s[scores={frost_ray_cd=..135}] run playsound entity.squid.squirt master @a[distance=..15] ~ ~ ~ 0.1 1.2 0.1
execute at @s[scores={frost_ray_cd=..115}] run playsound block.amethyst_cluster.break master @a[distance=..15] ~ ~ ~ 0.1 1.2 0.1

execute at @s[scores={frost_ray_cd=..135},predicate=!skills:spell_power] run function skills:spells/spell_10_1
execute at @s[scores={frost_ray_cd=..135},predicate=skills:spell_power] run function skills:spells/spell_10_2

tag @s remove frost_ray_end

scoreboard players add @s[tag=frost_ray_mana,scores={mana=..19}] mana 1
tag @s remove frost_ray_mana

execute as @s[predicate=!skills:spell_10] run function skills:spells/spell_10_end
execute as @s[scores={frost_ray_cd=0}] run function skills:spells/spell_10_end