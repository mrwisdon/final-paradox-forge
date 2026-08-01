tag @s add vt_eviscerate_attack
scoreboard players set @s vt_eviscerate_timer 1
scoreboard players set @s vt_eviscerate_stage 0

execute at @s run particle crit ^ ^1.5 ^2.5 2 1 2 0.1 250 normal
execute at @s run particle sweep_attack ^ ^1.5 ^2.5 2.2 1.2 2.2 0.1 120 normal
execute at @s run particle block nether_wart_block ^ ^1.5 ^3 2.8 1.2 2.8 1 120 normal
execute at @s run particle dust 0.522 0.114 0.169 1 ^ ^1.5 ^3 1.8 0.6 1.8 0.1 350 normal
#execute at @s run particle dust 0.345 0.043 0.42 1 ^ ^1.5 ^3 1.8 0.6 1.8 0.1 200 normal
execute at @s run playsound entity.warden.attack_impact master @a[distance=..15] ~ ~ ~ 1 0.6 1
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 1 1
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..15] ~ ~ ~ 0.7 0.5 0.7
execute at @s run playsound entity.zombie.infect master @a[distance=..15] ~ ~ ~ 0.7 1.5 0.7

execute at @s positioned ^ ^1 ^3 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..5.5] run function skills:sword/vt_eviscerate_2_hit

tag @s remove vt_eviscerate_attack