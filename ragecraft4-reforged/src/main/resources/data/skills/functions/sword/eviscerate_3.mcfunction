tag @s add eviscerate_attack
scoreboard players set @s eviscerate_timer 1
scoreboard players set @s eviscerate_stage 0

execute at @s run particle crit ^ ^1.5 ^2.5 2 1 2 0.1 150 normal
execute at @s run particle sweep_attack ^ ^1.5 ^2.5 2.2 1.2 2.2 0.1 80 normal
execute at @s run particle block nether_wart_block ^ ^1.5 ^3 2.8 1.2 2.8 1 120 normal
execute at @s run particle dust 0.561 0.000 0.000 1 ^ ^1.5 ^3 1.8 0.6 1.8 0.1 300 normal
execute at @s run playsound entity.warden.attack_impact master @a[distance=..15] ~ ~ ~ 1 0.6 1
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 1 1
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..15] ~ ~ ~ 0.7 0.5 0.7
execute at @s run playsound entity.zombie.infect master @a[distance=..15] ~ ~ ~ 0.7 1.5 0.7

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5.5] run function skills:sword/eviscerate_2_hit

tag @s remove eviscerate_attack