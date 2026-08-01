tag @s add overcharge_attack

execute at @s run playsound entity.puffer_fish.blow_out master @a[distance=..15] ~ ~ ~ 0.6 0.8 0.6
execute at @s run playsound block.fire.extinguish master @a[distance=..15] ~ ~ ~ 0.6 1.2 0.6
execute at @s run playsound minecraft:entity.lightning_bolt.impact master @a[distance=..15] ~ ~ ~ 0.3 1.3 0.3

execute at @s run particle flash ^ ^1 ^2.5 0.1 0.1 0.1 0.5 1 normal
execute at @s run particle electric_spark ^ ^1 ^2.5 1.5 0.8 1.5 0.03 150 normal
execute at @s run particle electric_spark ^ ^1 ^2.5 0.6 0.5 0.6 0.03 200 normal
execute at @s run particle dust 0.988 1.000 0.851 1 ^ ^1 ^2.5 1.5 0.8 1.5 0.1 200 normal
execute at @s run particle dust 0.988 1.000 0.851 1 ^ ^1 ^2.5 0.6 0.5 0.6 0.1 100 normal

execute at @s run summon marker ^ ^1 ^3 {Tags:["overcharge_anim"]}
execute as @e[type=marker,tag=overcharge_anim] run function skills:pickaxe/overcharge_anim

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..4] run function skills:pickaxe/overcharge_hit

scoreboard players set @s overcharge_time 1

tag @s remove overcharge_attack