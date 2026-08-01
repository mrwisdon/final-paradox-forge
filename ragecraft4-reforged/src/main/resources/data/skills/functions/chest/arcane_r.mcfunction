scoreboard players add @s chal_mana_spent 2

scoreboard players remove @s mana 2

execute at @s run particle explosion ^ ^1.5 ^2.5 0.1 0.1 0.1 0.8 1 normal
execute at @s run particle enchanted_hit ^ ^1.5 ^2.5 0.6 0.6 0.6 0.5 150 normal
execute at @s run particle dust 0.494 0.176 1 1 ^ ^1.5 ^2.5 0.5 0.5 0.5 0.5 50 normal
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..15] ~ ~ ~ 0.1 1.3 0.1
execute at @s run playsound entity.glow_squid.death master @a[distance=..15] ~ ~ ~ 0.4 1.4 0.4

tag @s add arcane_r_attack
execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,distance=..4] run function skills:chest/arcane_r_hit
tag @s remove arcane_r_attack