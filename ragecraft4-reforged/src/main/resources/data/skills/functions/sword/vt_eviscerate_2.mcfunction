tag @s add vt_eviscerate_attack
scoreboard players set @s vt_eviscerate_timer 40
scoreboard players set @s vt_eviscerate_stage 2

execute at @s run particle sweep_attack ^ ^1.5 ^2.5 1.2 0.5 1.2 0.1 25 normal
execute at @s run particle block nether_wart_block ^ ^1.5 ^3 1.5 0.7 1.5 1 50 normal
execute at @s run particle dust 0.522 0.114 0.169 1 ^ ^1.5 ^3 1.2 0.5 1.2 0.1 100 normal
#execute at @s run particle dust 0.345 0.043 0.42 1 ^ ^1.5 ^3 1.2 0.5 1.2 0.1 50 normal
execute at @s run playsound block.frogspawn.place master @a[distance=..15] ~ ~ ~ 1 1 1
execute at @s run playsound minecraft:item.trident.throw master @a[distance=..15] ~ ~ ~ 0.7 0.5 0.7
execute at @s run playsound entity.zombie.infect master @a[distance=..15] ~ ~ ~ 0.7 1.5 0.7

execute at @s positioned ^ ^1 ^2 run execute as @e[tag=r4r_ability_target,tag=!no_target,distance=..3.5] run function skills:sword/vt_eviscerate_1_hit

tag @s remove vt_eviscerate_attack